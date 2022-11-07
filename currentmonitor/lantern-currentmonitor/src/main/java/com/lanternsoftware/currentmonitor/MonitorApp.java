package com.lanternsoftware.currentmonitor;

import com.lanternsoftware.currentmonitor.bluetooth.BleCharacteristicListener;
import com.lanternsoftware.currentmonitor.led.LEDFlasher;
import com.lanternsoftware.currentmonitor.util.NetworkMonitor;
import com.lanternsoftware.currentmonitor.wifi.WifiConfig;
import com.lanternsoftware.datamodel.currentmonitor.Breaker;
import com.lanternsoftware.datamodel.currentmonitor.BreakerConfig;
import com.lanternsoftware.datamodel.currentmonitor.BreakerGroup;
import com.lanternsoftware.datamodel.currentmonitor.BreakerHub;
import com.lanternsoftware.datamodel.currentmonitor.BreakerPower;
import com.lanternsoftware.datamodel.currentmonitor.BreakerPowerMinute;
import com.lanternsoftware.datamodel.currentmonitor.HubCommand;
import com.lanternsoftware.datamodel.currentmonitor.HubCommands;
import com.lanternsoftware.datamodel.currentmonitor.HubConfigCharacteristic;
import com.lanternsoftware.datamodel.currentmonitor.HubConfigService;
import com.lanternsoftware.datamodel.currentmonitor.HubPowerMinute;
import com.lanternsoftware.datamodel.currentmonitor.NetworkStatus;
import com.lanternsoftware.datamodel.currentmonitor.hub.HubSample;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.ResourceLoader;
import com.lanternsoftware.util.ZipUtils;
import com.lanternsoftware.util.concurrency.ConcurrencyUtils;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.http.HttpPool;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class MonitorApp {
	private static final Logger LOG = LoggerFactory.getLogger(MonitorApp.class);
	private static final String WORKING_DIR = "/opt/currentmonitor/";
	private static String authCode;
	private static MonitorConfig config;
	private static BreakerConfig breakerConfig;
	private static String host;
	private static HttpPool pool;
	private static LEDFlasher flasher = null;
	private static final AtomicBoolean running = new AtomicBoolean(true);
	private static final CurrentMonitor monitor = new CurrentMonitor();
	private static final List<BreakerPower> readings = new ArrayList<>();
	private static String version;
	private static final PowerListener logger = new PowerListener() {
		@Override
		public void onPowerEvent(BreakerPower _power) {
			if (!config.isDebug()) {
				_power.setHubVersion(version);
				if (breakerConfig != null)
					_power.setAccountId(breakerConfig.getAccountId());
				synchronized (readings) {
					readings.add(_power);
				}
			} else
				LOG.info("Panel{} - Space{} Power: {}W", _power.getPanel(), Breaker.toSpaceDisplay(_power.getSpace()), String.format("%.3f", _power.getPower()));
		}

		@Override
		public void onSampleEvent(HubSample _sample) {
			post(DaoSerializer.toZipBson(_sample), "sample");
		}
	};
	private static final BleCharacteristicListener bluetoothListener = new BleCharacteristicListener() {
		@Override
		public void write(String _name, byte[] _value) {
			HubConfigCharacteristic ch = NullUtils.toEnum(HubConfigCharacteristic.class, _name);
			LOG.info("Char Received, Name: {} Value: {}", _name, _value);
			monitor.submit(()->{
				synchronized (monitor) {
					switch (ch) {
						case Host:
							if ((_value.length > 0)) {
								config.setHost(NullUtils.terminateWith(NullUtils.toString(_value), "/") + "currentmonitor/");
								ResourceLoader.writeFile(WORKING_DIR + "config.json", DaoSerializer.toJson(config));
							}
							break;
						case HubIndex:
							if ((_value.length > 0)) {
								config.setHub(_value[0]);
								ResourceLoader.writeFile(WORKING_DIR + "config.json", DaoSerializer.toJson(config));
							}
							break;
						case AuthCode:
							String value = NullUtils.toString(_value);
							if (NullUtils.isNotEmpty(value)) {
								authCode = value;
								config.setAuthCode(value);
								ResourceLoader.writeFile(WORKING_DIR + "config.json", DaoSerializer.toJson(config));
							}
							break;
						case WifiCredentials:
							String ssid = HubConfigService.decryptWifiSSID(_value);
							String pwd = HubConfigService.decryptWifiPassword(_value);
							if (NullUtils.isNotEmpty(ssid) && NullUtils.isNotEmpty(pwd))
								WifiConfig.setCredentials(ssid, pwd);
							break;
						case Flash:
							if ((CollectionUtils.length(_value) == 0) || (_value[0] == 0)) {
								if (flasher != null) {
									flasher.stop();
									flasher = null;
								} else
									LEDFlasher.setLEDOn(false);
							} else {
								if (flasher == null) {
									flasher = new LEDFlasher();
									monitor.submit(flasher);
								}
							}
							break;
						case Restart:
							LOG.info("Restarting Current Monitor...");
							try {
								Runtime.getRuntime().exec(new String[]{"systemctl","restart","currentmonitor"});
							} catch (IOException _e) {
								LOG.error("Exception occurred while trying to restart", _e);
							}
							break;
						case Reboot:
							LOG.info("Rebooting Pi...");
							try {
								Runtime.getRuntime().exec(new String[]{"reboot","now"});
							} catch (IOException _e) {
								LOG.error("Exception occurred while trying to reboot", _e);
							}
							break;
						case Shutdown:
							LOG.info("Shutting down Pi...");
							try {
								Runtime.getRuntime().exec(new String[]{"shutdown","now"});
							} catch (IOException _e) {
								LOG.error("Exception occurred while trying to shutdown", _e);
							}
							break;
						case Update:
							monitor.submit(new UpdateChecker());
							break;
						case ReloadConfig:
							HttpGet get = new HttpGet(host + "config");
							get.addHeader("auth_code", authCode);
							BreakerConfig newConfig = DaoSerializer.parse(pool.executeToString(get), BreakerConfig.class);
							if (newConfig != null) {
								breakerConfig = newConfig;
								List<Breaker> breakers = breakerConfig.getBreakersForHub(config.getHub());
								BreakerHub hub = breakerConfig.getHub(config.getHub());
								if (hub != null)
									monitor.monitorPower(hub, breakers, 1000, logger);
							}
							break;
					}
				}
			});
		}

		@Override
		public byte[] read(String _name) {
			HubConfigCharacteristic ch = NullUtils.toEnum(HubConfigCharacteristic.class, _name);
			if (HubConfigCharacteristic.HubIndex == ch)
				return new byte[]{(byte)(config == null?0:config.getHub())};
			if (HubConfigCharacteristic.AccountId == ch)
				return ByteBuffer.allocate(4).putInt(breakerConfig == null?0:breakerConfig.getAccountId()).array();
			if (HubConfigCharacteristic.NetworkState == ch)
				return new byte[]{NetworkMonitor.getNetworkStatus().toMask()};
			if (HubConfigCharacteristic.NetworkDetails == ch) {
				NetworkStatus status = NetworkMonitor.getNetworkStatus();
				DaoEntity meta = (host == null)?null:DaoSerializer.fromZipBson(pool.executeToByteArray(new HttpGet(host + "update/version")));
				status.setPingSuccessful(CollectionUtils.isNotEmpty(meta));
				return DaoSerializer.toZipBson(status);
			}
			if (HubConfigCharacteristic.Log == ch) {
				String[] log = NullUtils.cleanSplit(ResourceLoader.loadFileAsString(WORKING_DIR + "log/log.txt"), "\n");
				if (log.length > 15)
					log = Arrays.copyOfRange(log, log.length-15, log.length);
				return ZipUtils.zip(NullUtils.toByteArray(CollectionUtils.delimit(Arrays.asList(log), "\n")));
			}
			if (HubConfigCharacteristic.Version == ch)
				return NullUtils.toByteArray(version);
			return null;
		}
	};
	private static BluetoothConfig bluetoothConfig;
	private static MqttPoster mqttPoster;

	public static void main(String[] args) {
		try {
			Runtime.getRuntime().exec(new String[]{"systemctl","restart","dbus"});
			ConcurrencyUtils.sleep(500);
		} catch (IOException _e) {
			LOG.error("Exception occurred while trying to restart", _e);
		}
		version = getVersionNumber();
		config = DaoSerializer.parse(ResourceLoader.loadFileAsString(WORKING_DIR + "config.json"), MonitorConfig.class);
		if (config == null) {
			config = new MonitorConfig();
			ResourceLoader.writeFile(WORKING_DIR + "config.json", DaoSerializer.toJson(config));
		}
		pool = HttpPool.builder().withValidateSSLCertificates(!config.isAcceptSelfSignedCertificates()).build();
		if (NullUtils.isNotEmpty(config.getHost()))
			host = NullUtils.terminateWith(config.getHost(), "/");
		monitor.setDebug(config.isDebug());
		monitor.setPostSamples(config.isPostSamples());
		LEDFlasher.setLEDOn(false);
		if (NullUtils.isNotEmpty(config.getAuthCode()))
			authCode = config.getAuthCode();
		else if (NullUtils.isNotEmpty(host) && NullUtils.isNotEmpty(config.getUsername()) && NullUtils.isNotEmpty(config.getPassword())) {
			HttpGet auth = new HttpGet(host + "auth");
			HttpPool.addBasicAuthHeader(auth, config.getUsername(), config.getPassword());
			authCode = DaoSerializer.getString(DaoSerializer.parse(pool.executeToString(auth)), "auth_code");
		}
		if (NullUtils.isNotEmpty(config.getMqttBrokerUrl()))
			mqttPoster = new MqttPoster(config);
		if (NullUtils.isNotEmpty(host) && NullUtils.isNotEmpty(authCode)) {
			int configAttempts = 0;
			while (configAttempts < 5) {
				HttpGet get = new HttpGet(host + "config");
				get.addHeader("auth_code", authCode);
				breakerConfig = DaoSerializer.parse(pool.executeToString(get), BreakerConfig.class);
				if ((breakerConfig != null) || (mqttPoster != null))
					break;
				LOG.error("Failed to load breaker config.  Retrying in 5 seconds...");
				ConcurrencyUtils.sleep(5000);
				configAttempts++;
			}
		}
		bluetoothConfig = new BluetoothConfig("Lantern Hub", bluetoothListener);
		bluetoothConfig.start();
		if ((mqttPoster != null) && (breakerConfig == null)) {
			LOG.info("Hub not configured by a Lantern Power Monitor server, defaulting to MQTT mode only");
			BreakerHub hub = new BreakerHub();
			hub.setHub(config.getHub());
			hub.setVoltageCalibrationFactor(config.getFinalVoltageCalibrationFactor());
			hub.setPortCalibrationFactor(config.getMqttPortCalibrationFactor());
			hub.setFrequency(config.getMqttFrequency());
			breakerConfig = new BreakerConfig();
			breakerConfig.setBreakerHubs(CollectionUtils.asArrayList(hub));
			int groupId = 0;
			breakerConfig.setBreakerGroups(new ArrayList<>());
			for (Breaker b : CollectionUtils.makeNotNull(config.getMqttBreakers())) {
				BreakerGroup g = new BreakerGroup();
				g.setName(b.getName());
				g.setBreakers(CollectionUtils.asArrayList(b));
				g.setId(String.valueOf(++groupId));
				g.setAccountId(-1);
				breakerConfig.getBreakerGroups().add(g);
			}
		}
		if (breakerConfig != null) {
			LOG.info("Breaker Config loaded");
			BreakerHub hub = breakerConfig.getHub(config.getHub());
			if (hub != null) {
				if (config.isNeedsCalibration()) {
					try {
						CalibrationResult cal = monitor.calibrateVoltage(hub.getVoltageCalibrationFactor());
						if (cal != null) {
							hub.setVoltageCalibrationFactor(cal.getVoltageCalibrationFactor());
							hub.setFrequency(cal.getFrequency());
							config.setNeedsCalibration(false);
							ResourceLoader.writeFile(WORKING_DIR + "config.json", DaoSerializer.toJson(config));
							post(DaoSerializer.toZipBson(breakerConfig), "config");
						}
					}
					catch (Throwable t) {
						LOG.error("Exception trying to read from voltage pin", t);
					}
				}
				List<Breaker> breakers = breakerConfig.getBreakersForHub(config.getHub());
				monitor.monitorPower(hub, breakers, 1000, logger);
			}
			monitor.submit(new PowerPoster());
		}
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			synchronized (running) {
				running.set(false);
			}
			bluetoothConfig.stop();
			monitor.stop();
			pool.shutdown();
		}, "Monitor Shutdown"));
		try {
			monitor.wait();
		} catch (InterruptedException _e) {
			LOG.error("Interrupted, shutting down", _e);
		}
	}

	private static final class PowerPoster implements Runnable {
		private final long firstPost;
		private long lastPost;
		private int lastMinute;
		private final Map<Integer, Float[]> breakers = new HashMap<>();

		public PowerPoster() {
			firstPost = (new Date().getTime()/1000)*1000;
			lastPost = new Date().getTime();
			lastMinute = (int)(new Date().getTime()/60000);
		}

		@Override
		public void run() {
			try {
				while (true) {
					synchronized (running) {
						if (!running.get())
							break;
					}
					DaoEntity post = null;
					DaoEntity minutePost = null;
					int curMinute = (int) (new Date().getTime() / 60000);
					List<BreakerPower> mqttReadings = new ArrayList<>();
					synchronized (readings) {
						if (!readings.isEmpty()) {
							mqttReadings.addAll(readings);
							post = new DaoEntity("readings", DaoSerializer.toDaoEntities(readings));
							post.put("hub", config.getHub());
							if (curMinute != lastMinute) {
								HubPowerMinute minute = new HubPowerMinute();
								minute.setAccountId(breakerConfig.getAccountId());
								minute.setHub(config.getHub());
								minute.setMinute(lastMinute);
								minute.setBreakers(CollectionUtils.transform(breakers.entrySet(), _e -> {
									BreakerPowerMinute breaker = new BreakerPowerMinute();
									breaker.setPanel(Breaker.toPanel(_e.getKey()));
									breaker.setSpace(Breaker.toSpace(_e.getKey()));
									breaker.setReadings(CollectionUtils.asArrayList(_e.getValue()));
									return breaker;
								}));
								breakers.clear();
								minutePost = DaoSerializer.toDaoEntity(minute);
								lastMinute = curMinute;
							}
							for (BreakerPower power : readings) {
								Float[] breakerReadings = breakers.computeIfAbsent(Breaker.toId(power.getPanel(), power.getSpace()), _i -> new Float[60]);
								breakerReadings[(int) ((power.getReadTime().getTime() / 1000)%60)] = (float) power.getPower();
							}
							readings.clear();
						}
					}
					if (NullUtils.isNotEmpty(host)) {
						if (minutePost != null) {
							byte[] payload = DaoSerializer.toZipBson(minutePost);
							if (!post(payload, "power/hub")) {
								LOG.info("Failed Posting HubPowerMinute, writing cache");
								ResourceLoader.writeFile(WORKING_DIR + "cache/" + UUID.randomUUID() + ".min", payload);
							}
						}
						if (post != null) {
							byte[] payload = DaoSerializer.toZipBson(post);
							PostResponse<HubCommands> resp = post(payload, "power/batch", HubCommands.class);
							if (resp.success) {
								File[] files = new File(WORKING_DIR + "cache").listFiles();
								if (files != null) {
									for (File file : files) {
										payload = ResourceLoader.loadFile(file.getAbsolutePath());
										if (post(payload, "power/hub"))
											file.delete();
										else
											break;
									}
								}
								if (resp.t != null) {
									for (HubCommand command : resp.t.getCommands()) {
										bluetoothListener.write(command.getCharacteristic().name(), command.getData());
									}
								}
							}
						}
					}
					if (mqttPoster != null)
						monitor.submit(() -> mqttPoster.postPower(mqttReadings));
					long now = new Date().getTime();
					long duration = (now - firstPost)%1000;
					if (now - lastPost < 1000) {
						ConcurrencyUtils.sleep(1000 - duration);
					}
					lastPost = now;
				}
			}
			catch (Throwable t) {
				LOG.error("Exception in PowerPoster", t);
			}
		}
	}

	private static boolean post(byte[] _payload, String _path) {
		return post(_payload, _path, Boolean.class).success;
	}

	private static <T> PostResponse<T> post(byte[] _payload, String _path, Class<T> _class) {
		if (NullUtils.isEmpty(host))
			return new PostResponse<>(false, null);
		HttpPost post = new HttpPost(host + _path);
		post.addHeader("auth_code", authCode);
		post.setEntity(new ByteArrayEntity(_payload, ContentType.APPLICATION_OCTET_STREAM));
		InputStream is = null;
		CloseableHttpResponse resp = pool.execute(post);
		try {
			if ((resp != null) && (resp.getStatusLine() != null) && (resp.getStatusLine().getStatusCode() == 200)) {
				T t = null;
				HttpEntity entity = resp.getEntity();
				if (entity != null) {
					is = entity.getContent();
					byte[] payload = IOUtils.toByteArray(is);
					if (CollectionUtils.length(payload) > 0)
						t = DaoSerializer.fromZipBson(payload, _class);
				}
				return new PostResponse<>(true, t);
			}
		}
		catch (Exception _e) {
			LOG.error("Failed to make http request to " + post.getURI().toString(), _e);
		}
		finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(resp);
		}
		return new PostResponse<>(false, null);
	}

	private static class PostResponse<T> {
		public final boolean success;
		public final T t;

		public PostResponse(boolean _success, T _t) {
			success = _success;
			t = _t;
		}
	}

	private static final class UpdateChecker implements Runnable {
		@Override
		public void run() {
			if (NullUtils.isNotEmpty(host)) {
				DaoEntity meta = DaoSerializer.fromZipBson(pool.executeToByteArray(new HttpGet(host + "update/version")));
				String newVersion = DaoSerializer.getString(meta, "version");
				if (NullUtils.isNotEqual(newVersion, version)) {
					LOG.info("New version found, {}, downloading...", newVersion);
					byte[] jar = pool.executeToByteArray(new HttpGet(host + "update"));
					if (CollectionUtils.length(jar) == DaoSerializer.getInteger(meta, "size") && NullUtils.isEqual(DigestUtils.md5Hex(jar), DaoSerializer.getString(meta, "checksum"))) {
						LOG.info("Update downloaded, writing jar and restarting...");
						ResourceLoader.writeFile(WORKING_DIR + "lantern-currentmonitor.jar", jar);
						synchronized (running) {
							running.set(false);
						}
						monitor.stopMonitoring();
						bluetoothConfig.stop();
						pool.shutdown();
						try {
							Runtime.getRuntime().exec(new String[]{"systemctl","restart","currentmonitor"});
						} catch (IOException _e) {
							LOG.error("Exception occurred while trying to restart", _e);
						}
					}
				}
			}
		}
	}

	public static String getVersionNumber() {
		try {
			Enumeration<URL> resources = MonitorApp.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
			while (resources.hasMoreElements()) {
				InputStream is = null;
				try {
					is = resources.nextElement().openStream();
					Manifest manifest = new Manifest(is);
					Attributes attr = manifest.getMainAttributes();
					if (NullUtils.isEqual(attr.getValue("Specification-Title"), "Lantern Power Monitor")) {
						String version = attr.getValue("Specification-Version");
						LOG.info("Current Version: {}", version);
						return version;
					}
				}
				finally {
					IOUtils.closeQuietly(is);
				}
			}
		}
		catch (Exception _e) {
			LOG.error("Failed to get current version number", _e);
		}
		return "";
	}
}

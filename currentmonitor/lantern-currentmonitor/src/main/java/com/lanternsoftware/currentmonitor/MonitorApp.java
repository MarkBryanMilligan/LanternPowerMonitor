package com.lanternsoftware.currentmonitor;

import com.lanternsoftware.currentmonitor.bluetooth.BleCharacteristicListener;
import com.lanternsoftware.currentmonitor.led.LEDFlasher;
import com.lanternsoftware.currentmonitor.util.NetworkMonitor;
import com.lanternsoftware.currentmonitor.wifi.WifiConfig;
import com.lanternsoftware.datamodel.currentmonitor.Breaker;
import com.lanternsoftware.datamodel.currentmonitor.BreakerConfig;
import com.lanternsoftware.datamodel.currentmonitor.BreakerHub;
import com.lanternsoftware.datamodel.currentmonitor.BreakerPower;
import com.lanternsoftware.datamodel.currentmonitor.BreakerPowerMinute;
import com.lanternsoftware.datamodel.currentmonitor.HubConfigCharacteristic;
import com.lanternsoftware.datamodel.currentmonitor.HubConfigService;
import com.lanternsoftware.datamodel.currentmonitor.HubPowerMinute;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.ResourceLoader;
import com.lanternsoftware.util.concurrency.ConcurrencyUtils;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.http.HttpPool;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
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
	private static Date lastUpdateCheck = new Date();
	private static HttpPool pool;
	private static LEDFlasher flasher = null;
	private static final AtomicBoolean running = new AtomicBoolean(true);
	private static final CurrentMonitor monitor = new CurrentMonitor();
	private static final List<BreakerPower> readings = new ArrayList<>();
	private static final String version = getVersionNumber();
	private static final PowerListener logger = _p -> {
		if (!config.isDebug()) {
			_p.setHubVersion(version);
			if (breakerConfig != null)
				_p.setAccountId(breakerConfig.getAccountId());
			synchronized (readings) {
				readings.add(_p);
			}
		} else
			LOG.info("Panel{} - Space{} Power: {}W", _p.getPanel(), Breaker.toSpaceDisplay(_p.getSpace()), String.format("%.3f", _p.getPower()));
	};

	public static void main(String[] args) {
		config = DaoSerializer.parse(ResourceLoader.loadFileAsString(WORKING_DIR + "config.json"), MonitorConfig.class);
		if (config == null) {
			LOG.error("Failed to load config file from {}", WORKING_DIR + "config.json");
			return;
		}
		pool = new HttpPool(10, 10, config.getSocketTimeout(), config.getConnectTimeout(), config.getSocketTimeout());
		host = NullUtils.terminateWith(config.getHost(), "/");
		monitor.setDebug(config.isDebug());
		monitor.start();
		LEDFlasher.setLEDOn(false);
		final BluetoothConfig bluetoothConfig = new BluetoothConfig("Lantern Hub", new BleCharacteristicListener() {
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
				return null;
			}
		});
		bluetoothConfig.start();
		if (NullUtils.isNotEmpty(config.getAuthCode()))
			authCode = config.getAuthCode();
		else {
			HttpGet auth = new HttpGet(host + "auth");
			HttpPool.addBasicAuthHeader(auth, config.getUsername(), config.getPassword());
			authCode = DaoSerializer.getString(DaoSerializer.parse(pool.executeToString(auth)), "auth_code");
		}
		while (true) {
			HttpGet get = new HttpGet(host + "config");
			get.addHeader("auth_code", authCode);
			breakerConfig = DaoSerializer.parse(pool.executeToString(get), BreakerConfig.class);
			if (breakerConfig != null)
				break;
			LOG.error("Failed to load breaker config.  Retrying in 5 seconds...");
			ConcurrencyUtils.sleep(5000);
		}
		LOG.info("Breaker Config loaded");
		BreakerHub hub = breakerConfig.getHub(config.getHub());
		if (hub != null) {
			if (config.isNeedsCalibration() && (config.getAutoCalibrationVoltage() != 0.0)) {
				double newCal = monitor.calibrateVoltage(hub.getVoltageCalibrationFactor(), config.getAutoCalibrationVoltage());
				if (newCal != 0.0) {
					hub.setVoltageCalibrationFactor(newCal);
					config.setNeedsCalibration(false);
					ResourceLoader.writeFile(WORKING_DIR + "config.json", DaoSerializer.toJson(config));
					post(DaoSerializer.toZipBson(breakerConfig), "config");
				}
			}
			List<Breaker> breakers = breakerConfig.getBreakersForHub(config.getHub());
			LOG.info("Monitoring {} breakers for hub {}", CollectionUtils.size(breakers), hub.getHub());
			if (CollectionUtils.size(breakers) > 0)
				monitor.monitorPower(hub, breakers, 1000, logger);
		}
		monitor.submit(new PowerPoster());
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			synchronized (running) {
				running.set(false);
			}
			bluetoothConfig.stop();
			monitor.stop();
			pool.shutdown();
		}, "Monitor Shutdown"));
		Console c = System.console();
		BufferedReader reader = (c == null)?new BufferedReader(new InputStreamReader(System.in)):null;
		while (running.get()) {
			try {
				String command = c != null ? c.readLine() : reader.readLine();
				if (NullUtils.isEqual("exit", command))
					break;
			}
			catch (Exception _e) {
				LOG.error("Exception while reading from console input", _e);
				break;
			}
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
					synchronized (readings) {
						if (!readings.isEmpty()) {
							post = new DaoEntity("readings", DaoSerializer.toDaoEntities(readings));
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
					if (minutePost != null) {
						byte[] payload = DaoSerializer.toZipBson(minutePost);
						if (!post(payload, "power/hub")) {
							LOG.info("Failed Posting HubPowerMinute, writing cache");
							ResourceLoader.writeFile(WORKING_DIR + "cache/" + UUID.randomUUID().toString() + ".min", payload);
						}
					}
					if (post != null) {
						byte[] payload = DaoSerializer.toZipBson(post);
						if (post(payload, "power/batch")) {
							File[] files = new File(WORKING_DIR + "cache").listFiles();
							if (files != null) {
								for (File file : files) {
									payload = ResourceLoader.loadFile(file.getAbsolutePath());
									if (post(payload, file.getName().endsWith("dat") ? "power/batch" : "power/hub"))
										file.delete();
									else
										break;
								}
							}
						}
					}
					if (DateUtils.diffInSeconds(new Date(), lastUpdateCheck) >= config.getUpdateInterval()) {
						lastUpdateCheck = new Date();
						monitor.submit(new UpdateChecker());
						monitor.submit(new CommandChecker());
					}
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

	private static void uploadLog() {
		LOG.info("Commanded to upload log file, preparing...");
		String log = ResourceLoader.loadFileAsString(WORKING_DIR + "log/log.txt");
		if (NullUtils.isNotEmpty(log)) {
			DaoEntity payload = new DaoEntity("command", "log").and("payload", log);
			post(DaoSerializer.toZipBson(payload), "command");
		}
	}

	private static boolean post(byte[] _payload, String _path) {
		HttpPost post = new HttpPost(host + _path);
		post.addHeader("auth_code", authCode);
		post.setEntity(new ByteArrayEntity(_payload, ContentType.APPLICATION_OCTET_STREAM));
		CloseableHttpResponse resp = pool.execute(post);
		try {
			return ((resp != null) && (resp.getStatusLine() != null) && (resp.getStatusLine().getStatusCode() == 200));
		} finally {
			IOUtils.closeQuietly(resp);
		}
	}


	private static final class UpdateChecker implements Runnable {
		@Override
		public void run() {
			DaoEntity meta = DaoSerializer.fromZipBson(pool.executeToByteArray(new HttpGet(host + "update/version")));
			String newVersion = DaoSerializer.getString(meta, "version");
			if (NullUtils.isNotEqual(newVersion, version)) {
				LOG.info("New version found, {}, downloading...", newVersion);
				byte[] jar = pool.executeToByteArray(new HttpGet(host + "update"));
				if (CollectionUtils.length(jar) == DaoSerializer.getInteger(meta, "size") && NullUtils.isEqual(DigestUtils.md5Hex(jar), DaoSerializer.getString(meta, "checksum"))) {
					LOG.info("Update downloaded, writing jar and restarting...");
					ResourceLoader.writeFile(WORKING_DIR + "lantern-currentmonitor.jar", jar);
					ConcurrencyUtils.sleep(10000);
					try {
						Runtime.getRuntime().exec(new String[]{"systemctl","restart","currentmonitor"});
					} catch (IOException _e) {
						LOG.error("Exception occurred while trying to restart", _e);
					}
				}
			}
		}
	}

	private static final class CommandChecker implements Runnable {
		@Override
		public void run() {
			HttpGet get = new HttpGet(host + "command");
			get.addHeader("auth_code", authCode);
			DaoEntity meta = DaoSerializer.fromZipBson(pool.executeToByteArray(get));
			for (String command : DaoSerializer.getList(meta, "commands", String.class)) {
				if (NullUtils.isEqual(command, "log")) {
					uploadLog();
				}
				else if (NullUtils.makeNotNull(command).startsWith("timeout")) {
					LOG.info("Updating timeouts...");
					String[] timeouts = NullUtils.cleanSplit(command, "-");
					if (CollectionUtils.size(timeouts) != 3)
						continue;
					config.setConnectTimeout(DaoSerializer.toInteger(timeouts[1]));
					config.setSocketTimeout(DaoSerializer.toInteger(timeouts[2]));
					HttpPool old = pool;
					pool = new HttpPool(10, 10, config.getSocketTimeout(), config.getConnectTimeout(), config.getSocketTimeout());
					old.shutdown();
					ResourceLoader.writeFile(WORKING_DIR+"config.json", DaoSerializer.toJson(config));
				}
				else if (NullUtils.isEqual(command, "extend_filesystem")) {
					LOG.info("Extending filesystem and rebooting");
					try {
						Runtime.getRuntime().exec(new String[]{"sudo","raspi-config","--expand-rootfs"});
						ConcurrencyUtils.sleep(5000);
						Runtime.getRuntime().exec(new String[]{"reboot","now"});
					} catch (IOException _e) {
						LOG.error("Exception occurred while trying to extend filesystem", _e);
					}

				}
				else if (NullUtils.isEqual(command, "restart")) {
					LOG.info("Restarting...");
					try {
						Runtime.getRuntime().exec(new String[]{"systemctl","restart","currentmonitor"});
					} catch (IOException _e) {
						LOG.error("Exception occurred while trying to restart", _e);
					}
				}
			}
		}
	}

	private static String getVersionNumber() {
		InputStream is = null;
		try {
			is = MonitorApp.class.getResourceAsStream("/META-INF/MANIFEST.MF");
			Manifest manifest = new Manifest(is);
			Attributes attr = manifest.getMainAttributes();
			String version = attr.getValue("Specification-Version");
			LOG.info("Current Version: {}", version);
			return version;
		}
		catch (Exception _e) {
			LOG.error("Failed to get current version number", _e);
			return "";
		}
		finally {
			IOUtils.closeQuietly(is);
		}
	}
}

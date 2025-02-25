package com.lanternsoftware.zwave.context;

import com.lanternsoftware.datamodel.rules.Event;
import com.lanternsoftware.datamodel.rules.EventType;
import com.lanternsoftware.datamodel.zwave.Switch;
import com.lanternsoftware.datamodel.zwave.SwitchSchedule;
import com.lanternsoftware.datamodel.zwave.SwitchTransition;
import com.lanternsoftware.datamodel.zwave.ThermostatMode;
import com.lanternsoftware.datamodel.zwave.ZWaveConfig;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.ResourceLoader;
import com.lanternsoftware.util.concurrency.ConcurrencyUtils;
import com.lanternsoftware.util.cryptography.AESTool;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.auth.AuthCode;
import com.lanternsoftware.util.dao.mongo.MongoConfig;
import com.lanternsoftware.util.external.LanternFiles;
import com.lanternsoftware.util.http.HttpPool;
import com.lanternsoftware.zwave.controller.Controller;
import com.lanternsoftware.zwave.dao.MongoZWaveDao;
import com.lanternsoftware.zwave.message.IMessageSubscriber;
import com.lanternsoftware.zwave.message.MessageEngine;
import com.lanternsoftware.zwave.message.impl.AddNodeToNetworkStartRequest;
import com.lanternsoftware.zwave.message.impl.AddNodeToNetworkStopRequest;
import com.lanternsoftware.zwave.message.impl.AssociationReportRequest;
import com.lanternsoftware.zwave.message.impl.BinarySwitchReportRequest;
import com.lanternsoftware.zwave.message.impl.BinarySwitchSetRequest;
import com.lanternsoftware.zwave.message.impl.CRC16EncapRequest;
import com.lanternsoftware.zwave.message.impl.MultilevelSensorGetRequest;
import com.lanternsoftware.zwave.message.impl.MultilevelSensorReportRequest;
import com.lanternsoftware.zwave.message.impl.MultilevelSwitchReportRequest;
import com.lanternsoftware.zwave.message.impl.MultilevelSwitchSetRequest;
import com.lanternsoftware.zwave.message.impl.NodeNeighborUpdateRequest;
import com.lanternsoftware.zwave.message.impl.RemoveNodeFromNetworkStartRequest;
import com.lanternsoftware.zwave.message.impl.RemoveNodeFromNetworkStopRequest;
import com.lanternsoftware.zwave.message.impl.ThermostatModeSetRequest;
import com.lanternsoftware.zwave.message.impl.ThermostatSetPointReportRequest;
import com.lanternsoftware.zwave.message.impl.ThermostatSetPointSetRequest;
import com.lanternsoftware.zwave.message.thermostat.ThermostatSetPointIndex;
import com.lanternsoftware.zwave.relay.RelayController;
import com.lanternsoftware.zwave.security.SecurityController;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ZWaveApp {
	public static final AESTool aes = AESTool.authTool();
	public static String authCode;

	private static final Logger logger = LoggerFactory.getLogger(ZWaveApp.class);

	private MongoZWaveDao dao;
	private ZWaveConfig config;
	private Controller controller;
	private RelayController relayController;
	private SecurityController securityController;
	private final Map<Integer, Switch> originalSwitches = new HashMap<>();
	private final Map<Integer, Switch> switches = new HashMap<>();
	private final Map<Integer, Switch> mySwitches = new HashMap<>();
	private final Map<Integer, List<Switch>> peers = new HashMap<>();
	private Timer timer;
	private HttpPool pool;
	private SwitchScheduleTask nextScheduleTask;
	private final Map<Integer, Double> sensors = new HashMap<>();
	private ExecutorService executor = null;

	public void start() {
		try {
			pool = new HttpPool(100, 20, 5000, 5000, 5000);
			config = DaoSerializer.parse(ResourceLoader.loadFile(LanternFiles.CONFIG_PATH + "config.json"), ZWaveConfig.class);
			executor = Executors.newFixedThreadPool(5);
			if (config == null) {
				dao = new MongoZWaveDao(MongoConfig.fromDisk(LanternFiles.CONFIG_PATH + "mongo.cfg"));
				config = dao.getConfig(1);
			}
			if (NullUtils.isNotEmpty(config.getCommPort())) {
				controller = new Controller();
				controller.start(config.getCommPort());
			}
			authCode = aes.encryptToBase64(DaoSerializer.toZipBson(new AuthCode(config.getAccountId(), null)));
			if (!config.isMaster()) {
				HttpGet get = new HttpGet(config.getMasterUrl() + "/config");
				get.setHeader("auth_code", authCode);
				ZWaveConfig switchConfig = DaoSerializer.parse(pool.executeToString(get), ZWaveConfig.class);
				if (switchConfig != null) {
					config.setSwitches(switchConfig.getSwitches());
					config.setRulesUrl(switchConfig.getRulesUrl());
				}
				else {
					logger.error("Failed to retrieve switch config from master controller");
					stop();
					return;
				}
			}
			timer = new Timer("ZWaveApp Timer");


////			for (int node = 3; node < 7; node++) {
//				session.doAction(new ConfigurationSetAction(node, (byte) 7, new byte[]{99}));
//				ConcurrencyUtils.sleep(100);
//				session.doAction(new ConfigurationSetAction(node, (byte) 8, new byte[]{0, (byte) 1}));
//				ConcurrencyUtils.sleep(100);
//				session.doAction(new ConfigurationSetAction(node, (byte) 9, new byte[]{99}));
//				ConcurrencyUtils.sleep(100);
//				session.doAction(new ConfigurationSetAction(node, (byte) 10, new byte[]{0, (byte) 1}));
//				ConcurrencyUtils.sleep(100);
//				session.doAction(new ConfigurationSetAction(node, (byte) 11, new byte[]{99}));
//				ConcurrencyUtils.sleep(100);
//				session.doAction(new ConfigurationSetAction(node, (byte) 12, new byte[]{0, (byte) 1}));
//				ConcurrencyUtils.sleep(100);
//			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		Map<String, List<Switch>> groups = new HashMap<>();
		for (Switch sw : config.getSwitches()) {
			switches.put(sw.getNodeId(), sw);
			originalSwitches.put(sw.getNodeId(), sw.duplicate());
			if (config.isMySwitch(sw))
				mySwitches.put(sw.getNodeId(), sw);
			CollectionUtils.addToMultiMap(sw.getRoom() + ":" + sw.getName(), sw, groups);
		}
		if (CollectionUtils.anyQualify(mySwitches.values(), Switch::isSourceUrlValid)) {
			timer.scheduleAtFixedRate(new ThermostatTask(), 0, 30000);
		}
		if (CollectionUtils.anyQualify(mySwitches.values(), _s->_s.isRelay() || _s.isRelayButton() || (_s.isSpaceHeaterThermostat() && _s.getGpioPin() != 0))) {
			relayController = new RelayController();
		}
		List<Switch> securitySwitches = CollectionUtils.filter(mySwitches.values(), Switch::isSecurity);
		if (!securitySwitches.isEmpty()) {
			securityController = new SecurityController();
			for (Switch s : securitySwitches) {
				s.setLevel(securityController.isOpen(s.getGpioPin())?1:0);
				logger.info("Monitoring security sensor " + s.getFullDisplay() + " on gpio pin " + s.getGpioPin());
				securityController.listen(s, (_nodeId, _open) -> {
					s.setLevel(_open?1:0);
					logger.info(s.getFullDisplay() + " is " + ((s.getLevel() == 0)?"closed":"open"));
					fireSwitchLevelEvent(s);
					persistConfig();
				});
			}
		}
		for (List<Switch> group : groups.values()) {
			for (Switch sw : group) {
				peers.put(sw.getNodeId(), CollectionUtils.filter(group, _sw -> _sw.getNodeId() != sw.getNodeId()));
			}
		}
		System.out.println("My Switches:\n" + DaoSerializer.toJson(DaoSerializer.toDaoEntities(mySwitches.values())));
		scheduleNextTransition();

		MessageEngine.subscribe(new IMessageSubscriber<MultilevelSensorReportRequest>() {
			@Override
			public Class<MultilevelSensorReportRequest> getHandledMessageClass() {
				return MultilevelSensorReportRequest.class;
			}

			@Override
			public void onMessage(MultilevelSensorReportRequest _message) {
				synchronized (sensors) {
					sensors.put((int) _message.getNodeId(), _message.getTemperatureCelsius());
					sensors.notify();
				}
			}
		});

		MessageEngine.subscribe(new IMessageSubscriber<ThermostatSetPointReportRequest>() {
			@Override
			public Class<ThermostatSetPointReportRequest> getHandledMessageClass() {
				return ThermostatSetPointReportRequest.class;
			}

			@Override
			public void onMessage(ThermostatSetPointReportRequest _message) {
				synchronized (switches) {
					Switch sw = switches.get((int) _message.getNodeId());
					if (sw != null) {
						if (NullUtils.isOneOf(_message.getIndex(), ThermostatSetPointIndex.HEATING, ThermostatSetPointIndex.COOLING)) {
							sw.setLevel((int) Math.round(_message.getTemperatureCelsius() * 1.8) + 32);
							fireSwitchLevelEvent(sw);
							persistConfig();
						}
					}
				}
			}
		});

		MessageEngine.subscribe(new IMessageSubscriber<MultilevelSwitchReportRequest>() {
			@Override
			public Class<MultilevelSwitchReportRequest> getHandledMessageClass() {
				return MultilevelSwitchReportRequest.class;
			}

			@Override
			public void onMessage(MultilevelSwitchReportRequest _message) {
				logger.info("Received MultilevelSwitchReportRequest");
				onSwitchLevelChange(_message.getNodeId(), _message.getLevel());
			}
		});

		MessageEngine.subscribe(new IMessageSubscriber<BinarySwitchReportRequest>() {
			@Override
			public Class<BinarySwitchReportRequest> getHandledMessageClass() {
				return BinarySwitchReportRequest.class;
			}

			@Override
			public void onMessage(BinarySwitchReportRequest _message) {
				logger.info("Received BinarySwitchReportRequest");
				onSwitchLevelChange(_message.getNodeId(), _message.getLevel());
			}
		});

		MessageEngine.subscribe(new IMessageSubscriber<AssociationReportRequest>() {
			@Override
			public Class<AssociationReportRequest> getHandledMessageClass() {
				return AssociationReportRequest.class;
			}

			@Override
			public void onMessage(AssociationReportRequest _message) {
				synchronized (switches) {
					logger.info("nodeId:{}, groupIdx:{}, maxAssociations:{}, numReportsToFollow:{}, payload:{}", _message.getNodeId(), _message.getGroupIdx(), _message.getMaxAssociations(), _message.getNumReportsToFollow(), NullUtils.toHex(_message.getPayload()));
				}
			}
		});

		MessageEngine.subscribe(new IMessageSubscriber<CRC16EncapRequest>() {
			@Override
			public Class<CRC16EncapRequest> getHandledMessageClass() {
				return CRC16EncapRequest.class;
			}

			@Override
			public void onMessage(CRC16EncapRequest _message) {
				logger.info("Received CRC16EncapRequest");
//				onSwitchLevelChange(_message.getNodeId(), _message.isOn()?0xFF:0);
			}
		});


		//		for (int node : CollectionUtils.asArrayList(18, 19, 23, 24, 2)) {
//			controller.send(new ConfigurationSetRequest((byte)node, (byte)7, new byte[]{1})); //only physical tap sends update to controller
//			ConcurrencyUtils.sleep(1000);
//			controller.send(new ConfigurationSetRequest((byte)node, (byte)23, new byte[]{2})); //color green
//			ConcurrencyUtils.sleep(1000);
//			controller.send(new ConfigurationSetRequest((byte)node, (byte)16, new byte[]{1})); //dimming speed
//			ConcurrencyUtils.sleep(1000);
//			controller.send(new ConfigurationSetRequest((byte)node, (byte)25, new byte[]{3})); //single tap to 100% on
//			ConcurrencyUtils.sleep(1000);
//			controller.send(new ConfigurationSetRequest((byte)node, (byte)15, new byte[]{1})); //enable physical paddle
//			ConcurrencyUtils.sleep(1000);
//			controller.send(new ConfigurationSetRequest((byte)node, (byte)20, new byte[]{2})); //report final level only when holding to dim
//			ConcurrencyUtils.sleep(1000);
//			controller.send(new ConfigurationSetRequest((byte)node, (byte)21, new byte[]{0})); //report on/off state
//			ConcurrencyUtils.sleep(1000);
//			controller.send(new ConfigurationSetRequest((byte)node, (byte)9, new byte[]{0})); //paddle immediate on
//			ConcurrencyUtils.sleep(1000);
//			controller.send(new ConfigurationSetRequest((byte)node, (byte)27, new byte[]{0})); //paddle immediate off
//			ConcurrencyUtils.sleep(1000);
//			controller.send(new ConfigurationSetRequest((byte)node, (byte)28, new byte[]{0})); //z-wave immediate on
//			ConcurrencyUtils.sleep(1000);
//			controller.send(new ConfigurationSetRequest((byte)node, (byte)29, new byte[]{0})); //z-wave immediate off
//			ConcurrencyUtils.sleep(1000);
//			controller.send(new ConfigurationSetRequest((byte)node, (byte)13, new byte[]{0})); //scene control disabled
//			ConcurrencyUtils.sleep(1000);
//			controller.send(new ConfigurationSetRequest((byte)node, (byte)26, new byte[]{0})); //programming disabled
//		}


//		controller.send(new MultilevelSwitchSetRequest((byte)2, 0xFF));

//		controller.send(new MultilevelSensorGetRequest((byte)11));
//		controller.send(new ThermostatSetPointGetRequest((byte)11, ThermostatSetPointIndex.HEATING));
//		controller.send(new ThermostatSetPointGetRequest((byte)11, ThermostatSetPointIndex.COOLING));
//		controller.send(new ThermostatSetPointGetRequest((byte)11, ThermostatSetPointIndex.HEATING_ECON));
//		controller.send(new ThermostatSetPointGetRequest((byte)11, ThermostatSetPointIndex.COOLING_ECON));
//		controller.send(new ThermostatSetPointSupportedGetRequest((byte)11));
//		controller.send(new ThermostatSetPointCapabilitiesGetRequest((byte)11));
//		controller.send(new ThermostatSetPointSetRequest((byte)11, ThermostatSetPointIndex.HEATING_ECON, 72));
//		controller.send(new ThermostatModeSetRequest((byte)11, ThermostatMode.HEAT));
//		controller.send(new ThermostatModeGetRequest((byte)11));


//		List<Switch> switches = CollectionUtils.filter(config.getSwitches(), _s->NullUtils.isOneOf(_s.getType(), SwitchType.DIMMER, SwitchType.BINARY) && _s.getNodeId() < 128);
//		controller.send(new AssociationRemoveRequest((byte)3, (byte)1, (byte)0x11));
//		for (int i = 1; i < 20; i++) {
//			logger.info("Removing associations for switch {}", sw.getNodeId());
//			for (Switch sw2 : switches) {
//				controller.send(new AssociationRemoveRequest((byte)sw.getNodeId(), (byte)3, (byte)sw2.getNodeId()));
//				ConcurrencyUtils.sleep(1000);
//			}
//			ConcurrencyUtils.sleep(1000);
//			controller.send(new AssociationGetRequest((byte)i));
//		}
		executor.submit(()->{
//			for (int i = 0; i < 9; i++) {
//				controller.send(new NodeNeighborUpdateRequest((byte) 7));
//				ConcurrencyUtils.sleep(20000);
//			}
//			List<Switch> switches = CollectionUtils.filter(config.getSwitches(), _s->_s.getNodeId() < 128);
//			switches.sort(Comparator.comparing(Switch::getNodeId));
//			for (Switch sw : switches) {
//				controller.send(new NodeNeighborUpdateRequest((byte) sw.getNodeId()));
//				ConcurrencyUtils.sleep(60000);
//			}
			controller.send(new NodeNeighborUpdateRequest((byte) 19));
			ConcurrencyUtils.sleep(60000);
			controller.send(new NodeNeighborUpdateRequest((byte) 21));
			ConcurrencyUtils.sleep(60000);
			controller.send(new NodeNeighborUpdateRequest((byte) 22));
			ConcurrencyUtils.sleep(60000);
		});
	}

	private void onSwitchLevelChange(int _nodeId, int _level) {
		synchronized (switches) {
			Switch sw = switches.get(_nodeId);
			if (sw != null) {
				logger.info("Received level change for node {} to level {} via z-wave", _nodeId, _level);
				if (_level == -1)
					_level = 255;
				int newLevel = sw.isMultilevel()?NullUtils.bound(_level, 0, 99):((_level == 0)?0:99);
				sw.setLevel(newLevel);
				fireSwitchLevelEvent(sw);
				for (Switch peer : CollectionUtils.makeNotNull(peers.get(_nodeId))) {
					byte nid = (byte) (peer.getNodeId()%1000);
					if (nid > 0) {
						logger.info("Mirror Event from {} node {} to {} node {} level {}", sw.isPrimary() ? "primary" : "secondary", _nodeId, peer.isPrimary() ? "primary" : "secondary", nid, newLevel);
						if (peer.isMultilevel()) {
							if (peer.getLevel() != sw.getLevel()) {
								peer.setLevel(newLevel);
								controller.send(new MultilevelSwitchSetRequest(nid, newLevel));
							}
						} else {
							newLevel = newLevel != 0 ? 0xff : 0;
							if (peer.getLevel() != sw.getLevel()) {
								peer.setLevel(newLevel);
								controller.send(new BinarySwitchSetRequest(nid, newLevel != 0));
							}
						}
					}
				}
				persistConfig();
			}
			else {
				logger.info("Received level change for unknown node {}", _nodeId);
			}
		}
	}

	private void scheduleNextTransition() {
		TimeZone tz = TimeZone.getTimeZone("America/Chicago");
		if (nextScheduleTask != null)
			nextScheduleTask.cancel();
		List<SwitchTransition> nextTransitions = CollectionUtils.getAllSmallest(CollectionUtils.aggregate(mySwitches.values(), _s->CollectionUtils.transform(_s.getSchedule(), _t->_t.getNextTransition(_s, tz))), Comparator.comparing(SwitchTransition::getTransitionTime));
		if (!CollectionUtils.isEmpty(nextTransitions)) {
			for (SwitchTransition tr : nextTransitions) {
				logger.info("Next transition scheduled for node {} to level {} at {}", tr.getSwitch().getNodeId(), tr.getLevel(), DateUtils.format("hh:mm:ssa", tz, tr.getTransitionTime()));
			}
			nextScheduleTask = new SwitchScheduleTask(nextTransitions);
			timer.schedule(nextScheduleTask, CollectionUtils.getFirst(nextTransitions).getTransitionTime());
		} else
			nextScheduleTask = null;
	}

	public int getAccountId() {
		return config == null ? 0 : config.getAccountId();
	}

	public void setSwitchLevel(int _nodeId, int _level) {
		setSwitchLevel(_nodeId, _level, true);
	}

	public void fireSwitchLevelEvent(Switch _sw) {
		if (NullUtils.isEmpty(config.getRulesUrl()) || _sw.isSuppressEvents())
			return;
		executor.submit(()->{
			Event event = new Event();
			event.setEventDescription(_sw.getFullDisplay() + " set to " + _sw.getLevel());
			event.setType(EventType.SWITCH_LEVEL);
			event.setTime(new Date());
			event.setValue(_sw.getLevel());
			event.setSourceId(String.valueOf(_sw.getNodeId()));
			event.setAccountId(config.getAccountId());
			logger.info("Sending event to rules server - " + event.getEventDescription());
			HttpPost post = new HttpPost(NullUtils.terminateWith(config.getRulesUrl(), "/") + "event");
			post.setHeader("auth_code", authCode);
			post.setEntity(new ByteArrayEntity(DaoSerializer.toZipBson(event)));
			pool.execute(post);
		});
	}

	public void setSwitchLevel(int _nodeId, int _level, boolean _updatePeers) {
		Switch sw = switches.get(_nodeId);
		if ((sw == null) || !sw.isPrimary())
			return;
		sw.setLevel(_level);
		if (config.isMySwitch(sw)) {
			fireSwitchLevelEvent(sw);
			if (sw.isSpaceHeaterThermostat()) {
				checkThermostat(sw);
			} else if (sw.isZWaveThermostat()) {
				controller.send(new ThermostatSetPointSetRequest((byte) sw.getNodeId(), sw.getThermostatMode() == ThermostatMode.COOL ? ThermostatSetPointIndex.COOLING : ThermostatSetPointIndex.HEATING, _level));
			} else if (sw.isRelay()) {
				relayController.setRelay(sw.getGpioPin(), sw.getLowLevel() > 0 ? sw.getLevel() == 0 : sw.getLevel() > 0);
			} else if (sw.isRelayButton()) {
				logger.info("Toggling relay " + sw.getFullDisplay() + " on");
				relayController.setRelay(sw.getGpioPin(), sw.getLowLevel() == 0);
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						logger.info("Toggling relay " + sw.getFullDisplay() + " off");
						relayController.setRelay(sw.getGpioPin(), sw.getLowLevel() > 0);
					}
				}, 250);
			} else if (!sw.isSecurity()){
				setGroupSwitchLevel(sw, _level);
			}
		}
		persistConfig(_updatePeers);
	}

	public void setThermostatMode(int _nodeId, ThermostatMode _mode) {
		Switch sw = switches.get(_nodeId);
		if ((sw == null) || !sw.isPrimary() || !sw.isZWaveThermostat() || !config.isMySwitch(sw))
			return;
		controller.send(new ThermostatModeSetRequest((byte) sw.getNodeId(), com.lanternsoftware.zwave.message.thermostat.ThermostatMode.fromByte(_mode.data)));
		sw.setThermostatMode(_mode);
		persistConfig();
	}

	public void setSwitchSchedule(int _nodeId, List<SwitchSchedule> _transitions) {
		Switch sw = switches.get(_nodeId);
		if ((sw == null) || !sw.isPrimary())
			return;
		sw.setSchedule(_transitions);
		persistConfig();
		scheduleNextTransition();
	}

	public void updateSwitch(Switch _sw) {
		logger.info("Received update for switch {} {} level {}", _sw.getNodeId(), _sw.getFullDisplay(), _sw.getLevel());
		Switch sw = CollectionUtils.filterOne(config.getSwitches(), _s->_s.getNodeId() == _sw.getNodeId());
		if (sw != null) {
			sw.setLevel( _sw.getLevel());
			sw.setHold( _sw.isHold());
		}
		setSwitchLevel(_sw.getNodeId(), _sw.getLevel(), false);
	}

	public void setSwitchHold(int _nodeId, boolean _hold) {
		Switch sw = switches.get(_nodeId);
		if ((sw == null) || !sw.isPrimary())
			return;
		sw.setHold(_hold);
		persistConfig();
	}

	private void persistConfig() {
		persistConfig(true);
	}

	private void persistConfig(boolean _updatePeers) {
		List<Switch> modified;
		synchronized (this) {
			modified = CollectionUtils.filter(switches.values(), _s->_s.isModified(originalSwitches.get(_s.getNodeId())));
			if (!modified.isEmpty()) {
				originalSwitches.clear();
				for (Switch s : switches.values()) {
					originalSwitches.put(s.getNodeId(), s.duplicate());
				}
				if (config.isMaster()) {
					if (dao != null)
						dao.putConfig(config);
					else
						ResourceLoader.writeFile(LanternFiles.CONFIG_PATH + "config.json", DaoSerializer.toJson(config));
				}
			}
		}
		if (_updatePeers) {
			Set<String> peers = CollectionUtils.transformToSet(switches.values(), Switch::getControllerUrl);
			peers.add(config.getMasterUrl());
			peers.remove(config.getUrl());
			for (String peer : peers) {
				for (Switch sw : modified) {
					executor.submit(()->{
						logger.info("Sending update for switch {} {} level {} to {}", sw.getNodeId(), sw.getFullDisplay(), sw.getLevel(), peer);
						HttpPost post = new HttpPost(peer + "/switch/" + sw.getNodeId());
						post.setHeader("auth_code", authCode);
						post.setEntity(new ByteArrayEntity(DaoSerializer.toZipBson(sw)));
						pool.execute(post);
					});
				}
			}
		}
	}

	public int getSwitchLevel(int _nodeId) {
		Switch sw = switches.get(_nodeId);
		return (sw != null) ? sw.getLevel() : 0;
	}

	public ZWaveConfig getConfig() {
		return config;
	}

	public void stop() {
		if (controller != null) {
			controller.stop();
			controller = null;
		}
		if (relayController != null) {
			relayController.shutdown();
			relayController = null;
		}
		if (securityController != null) {
			securityController.shutdown();
			securityController = null;
		}
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		if (pool != null) {
			pool.shutdown();
			pool = null;
		}
		if (executor != null) {
			executor.shutdown();
			executor = null;
		}
		if (dao != null) {
			dao.shutdown();
			dao = null;
		}
	}

	private void setGroupSwitchLevel(Switch _primary, int _level) {
		if ((_primary == null) || !config.isMySwitch(_primary) || (controller == null))
			return;
		List<Switch> nodes = CollectionUtils.asArrayList(_primary);
		nodes.addAll(CollectionUtils.filter(peers.get(_primary.getNodeId()), _p->!_p.isPrimary()));
		for (Switch node : nodes) {
			logger.info("Setting {}, Node {} to {}", node.getName(), node.getNodeId(), _level);
			byte nid = (byte) (node.getNodeId()%1000);
			if (nid > 0)
				controller.send(node.isMultilevel() ? new MultilevelSwitchSetRequest(nid, _level) : new BinarySwitchSetRequest(nid, _level != 0));
		}
	}

	private class ThermostatTask extends TimerTask {
		@Override
		public void run() {
			for (Switch sw : mySwitches.values()) {
				checkThermostat(sw);
			}
		}
	}

	private void checkThermostat(Switch _sw) {
		try {
			if (_sw.isSpaceHeaterThermostat()) {
				double tempF = getTemperatureCelsius(_sw) * 1.8 + 32;
				if (tempF > _sw.getLevel() + 0.4) {
					if (_sw.getGpioPin() > 0)
						relayController.setRelay(_sw.getGpioPin(), _sw.getLowLevel() > 0);
					else
						setGroupSwitchLevel(_sw, 0);
					logger.info("Turning {} {} off, temp is: {} set to: {}", _sw.getRoom(), _sw.getName(), tempF, _sw.getLevel());
				} else if (tempF < _sw.getLevel() - 0.4) {
					if (_sw.getGpioPin() > 0)
						relayController.setRelay(_sw.getGpioPin(), _sw.getLowLevel() == 0);
					else
						setGroupSwitchLevel(_sw, 255);
					logger.info("Turning {} {} on, temp is: {} set to: {}", _sw.getRoom(), _sw.getName(), tempF, _sw.getLevel());
				}
			}
		}
		catch (Throwable t) {
			logger.error("Failed to check temperature for thermostat {}", _sw.getName(), t);
		}
	}

	private class SwitchScheduleTask extends TimerTask {
		private final List<SwitchTransition> transitions;

		SwitchScheduleTask(List<SwitchTransition> _transitions) {
			transitions = _transitions;
		}

		@Override
		public void run() {
			for (SwitchTransition tr : transitions) {
				Switch sw = switches.get(tr.getSwitch().getNodeId());
				if (!sw.isHold()) {
					logger.info("Executing scheduled transition of node {} to level {}", sw.getNodeId(), tr.getLevel());
					Globals.app.setSwitchLevel(sw.getNodeId(), tr.getLevel());
				}
				else
					logger.info("Skipping scheduled transition of node {} to level {}, switch is on hold", sw.getNodeId(), tr.getLevel());
				ConcurrencyUtils.sleep(100);
			}
			nextScheduleTask = null;
			Globals.app.scheduleNextTransition();
		}
	}

	public int getCO2ppm(int _nodeId) {
		Switch sw = switches.get(_nodeId);
		if (sw == null)
			return 0;
		return DaoSerializer.getInteger(DaoSerializer.parse(pool.executeToString(new HttpGet(sw.getSourceUrl()))), "ppm");
	}

	public double getTemperatureCelsius(int _nodeId) {
		return getTemperatureCelsius(switches.get(_nodeId));
	}

	private double getTemperatureCelsius(Switch _sw) {
		if ((pool == null) || (_sw == null))
			return 0.0;
		if (_sw.isSourceUrlValid())
			return DaoSerializer.getDouble(DaoSerializer.parse(pool.executeToString(new HttpGet(_sw.getSourceUrl()))), "temp");
		else if (_sw.isZWaveThermostat() && config.isMySwitch(_sw)) {
			synchronized (sensors) {
				controller.send(new MultilevelSensorGetRequest((byte) _sw.getNodeId()));
				try {
					sensors.wait(3000);
				} catch (InterruptedException _e) {
					_e.printStackTrace();
				}
				Double temp = sensors.get(_sw.getNodeId());
				return (temp == null) ? 0.0 : temp;
			}
		}
		return 0.0;
	}

	public void addNodeToNetwork(boolean _enable, int _controllerIdx) {
		ZWaveConfig config = Globals.app.getConfig();
		if (_enable) {
			String controllerUrl = CollectionUtils.get(config.getControllers(), _controllerIdx);
			if (NullUtils.isEqual(controllerUrl, config.getUrl()) && (controller != null))
				controller.send(new AddNodeToNetworkStartRequest());
			else {
				HttpGet get = new HttpGet(controllerUrl + "/addNode/1/" + _controllerIdx);
				get.setHeader("auth_code", authCode);
				pool.execute(get);
			}
		}
		else {
			if (controller != null)
				controller.send(new AddNodeToNetworkStopRequest());
			List<String> controllers = config.getControllers();
			controllers.remove(config.getUrl());
			if (config.isMaster()) {
				for (String controllerUrl : controllers) {
					HttpGet get = new HttpGet(controllerUrl + "/addNode/0");
					get.setHeader("auth_code", authCode);
					pool.execute(get);
				}
			}
		}
	}

	public void removeNodeFromNetwork(boolean _enable, int _controllerIdx) {
		ZWaveConfig config = Globals.app.getConfig();
		if (_enable) {
			String controllerUrl = CollectionUtils.get(config.getControllers(), _controllerIdx);
			if (NullUtils.isEqual(controllerUrl, config.getUrl()) && (controller != null))
				controller.send(new RemoveNodeFromNetworkStartRequest());
			else {
				HttpGet get = new HttpGet(controllerUrl + "/removeNode/1/" + _controllerIdx);
				get.setHeader("auth_code", authCode);
				pool.execute(get);
			}
		}
		else {
			if (controller != null)
				controller.send(new RemoveNodeFromNetworkStopRequest());
			List<String> controllers = config.getControllers();
			controllers.remove(config.getUrl());
			if (config.isMaster()) {
				for (String controllerUrl : controllers) {
					HttpGet get = new HttpGet(controllerUrl + "/removeNode/0");
					get.setHeader("auth_code", authCode);
					pool.execute(get);
				}
			}
		}
	}
}

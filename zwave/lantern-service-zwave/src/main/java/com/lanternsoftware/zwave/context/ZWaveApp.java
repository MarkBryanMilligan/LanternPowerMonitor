package com.lanternsoftware.zwave.context;

import com.lanternsoftware.datamodel.zwave.Switch;
import com.lanternsoftware.datamodel.zwave.SwitchSchedule;
import com.lanternsoftware.datamodel.zwave.SwitchTransition;
import com.lanternsoftware.datamodel.zwave.ThermostatMode;
import com.lanternsoftware.datamodel.zwave.ZWaveConfig;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.LanternFiles;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.concurrency.ConcurrencyUtils;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.mongo.MongoConfig;
import com.lanternsoftware.util.http.HttpPool;
import com.lanternsoftware.zwave.controller.Controller;
import com.lanternsoftware.zwave.dao.MongoZWaveDao;
import com.lanternsoftware.zwave.message.IMessageSubscriber;
import com.lanternsoftware.zwave.message.MessageEngine;
import com.lanternsoftware.zwave.message.impl.BinarySwitchSetRequest;
import com.lanternsoftware.zwave.message.impl.MultilevelSensorGetRequest;
import com.lanternsoftware.zwave.message.impl.MultilevelSensorReportRequest;
import com.lanternsoftware.zwave.message.impl.MultilevelSwitchReportRequest;
import com.lanternsoftware.zwave.message.impl.MultilevelSwitchSetRequest;
import com.lanternsoftware.zwave.message.impl.ThermostatModeSetRequest;
import com.lanternsoftware.zwave.message.impl.ThermostatSetPointReportRequest;
import com.lanternsoftware.zwave.message.impl.ThermostatSetPointSetRequest;
import com.lanternsoftware.zwave.message.thermostat.ThermostatSetPointIndex;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class ZWaveApp {
	private static final Logger logger = LoggerFactory.getLogger(ZWaveApp.class);

	private MongoZWaveDao dao;
	private ZWaveConfig config;
	private Controller controller;
	private final Map<Integer, Switch> switches = new HashMap<>();
	private final Map<Integer, List<Integer>> peers = new HashMap<>();
	private Timer timer;
	private HttpPool pool;
	private SwitchScheduleTask nextScheduleTask;
	private final Map<Integer, Double> temperatures = new HashMap<>();
	private final Object ZWAVE_MUTEX = new Object();

	public void start() {
		try {
			dao = new  MongoZWaveDao(MongoConfig.fromDisk(LanternFiles.OPS_PATH + "mongo.cfg"));
			controller = new Controller();
			controller.start("COM4");
			timer = new Timer("ZWaveApp Timer");
			pool = new HttpPool(10, 10, 30000, 10000, 10000);

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
		config = dao.getConfig(1);
		Map<String, List<Integer>> groups = new HashMap<>();
		for (Switch sw : CollectionUtils.makeNotNull(config.getSwitches())) {
			switches.put(sw.getNodeId(), sw);
			CollectionUtils.addToMultiMap(sw.getRoom() + ":" + sw.getName(), sw.getNodeId(), groups);
		}
		if (CollectionUtils.filterOne(config.getSwitches(), Switch::isUrlThermostat) != null) {
			timer.scheduleAtFixedRate(new ThermostatTask(), 0, 30000);
		}
		for (List<Integer> group : groups.values()) {
			for (Integer node : group) {
				peers.put(node, CollectionUtils.filter(group, _i -> !_i.equals(node)));
			}
		}
		scheduleNextTransition();

		MessageEngine.subscribe(new IMessageSubscriber<MultilevelSensorReportRequest>() {
			@Override
			public Class<MultilevelSensorReportRequest> getHandledMessageClass() {
				return MultilevelSensorReportRequest.class;
			}

			@Override
			public void onMessage(MultilevelSensorReportRequest _message) {
				synchronized (temperatures) {
					temperatures.put((int) _message.getNodeId(), _message.getTemperatureCelsius());
					temperatures.notify();
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
				synchronized (switches) {
					Switch sw = switches.get((int) _message.getNodeId());
					if (sw != null) {
						sw.setLevel(_message.getLevel());
						for (Integer node : CollectionUtils.makeNotNull(peers.get((int) _message.getNodeId()))) {
							sw = switches.get(node);
							sw.setLevel(_message.getLevel());
							logger.info("Mirror Event from node {} to node {}", _message.getNodeId(), node);
							controller.send(new MultilevelSwitchSetRequest(node.byteValue(), _message.getLevel()));
						}
						persistConfig();
					}
				}
			}
		});

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
	}

	private void scheduleNextTransition() {
		TimeZone tz = TimeZone.getTimeZone("America/Chicago");
		if (nextScheduleTask != null)
			nextScheduleTask.cancel();
		List<SwitchTransition> nextTransitions = CollectionUtils.getAllSmallest(CollectionUtils.aggregate(switches.values(), _s->CollectionUtils.transform(_s.getSchedule(), _t->_t.getNextTransition(_s, tz))), Comparator.comparing(SwitchTransition::getTransitionTime));
		if (!CollectionUtils.isEmpty(nextTransitions)) {
			for (SwitchTransition tr : nextTransitions) {
				logger.info("Next transition scheduled for node {} to level {} at {}", tr.getSwitch().getNodeId(), tr.getLevel(), DateUtils.format("hh:mm:ssa", tz, tr.getTransitionTime()));
			}
			nextScheduleTask = new SwitchScheduleTask(nextTransitions);
			timer.schedule(nextScheduleTask, CollectionUtils.getFirst(nextTransitions).getTransitionTime());
		} else
			nextScheduleTask = null;
	}

	public void setSwitchLevel(int _nodeId, int _level) {
		Switch sw = switches.get(_nodeId);
		if ((sw == null) || !sw.isPrimary())
			return;
		sw.setLevel(_level);
		if (!sw.isThermostat()) {
			setGroupSwitchLevel(_nodeId, _level, sw.isMultilevel());
		} else if (sw.isZWaveThermostat()) {
			controller.send(new ThermostatSetPointSetRequest((byte) sw.getNodeId(), sw.getThermostatMode() == ThermostatMode.COOL ? ThermostatSetPointIndex.COOLING : ThermostatSetPointIndex.HEATING, _level));
		} else {
			if (timer != null)
				timer.schedule(new ThermostatTask(), 0);
			persistConfig();
		}
	}

	public void setThermostatMode(int _nodeId, ThermostatMode _mode) {
		Switch sw = switches.get(_nodeId);
		if ((sw == null) || !sw.isPrimary() || !sw.isZWaveThermostat())
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

	public void setSwitchHold(int _nodeId, boolean _hold) {
		Switch sw = switches.get(_nodeId);
		if ((sw == null) || !sw.isPrimary())
			return;
		sw.setHold(_hold);
		persistConfig();
	}

	private void persistConfig() {
		synchronized (this) {
			dao.putConfig(config);
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
		controller.stop();
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		if (pool != null) {
			pool.shutdown();
			pool = null;
		}
		if (dao != null) {
			dao.shutdown();
			dao = null;
		}
	}

	private void setGroupSwitchLevel(int _primary, int _level, boolean _multilevel) {
		List<Integer> nodes = CollectionUtils.asArrayList(_primary);
		nodes.addAll(CollectionUtils.makeNotNull(peers.get(_primary)));
		for (int node : nodes) {
			controller.send(_multilevel ? new MultilevelSwitchSetRequest((byte) node, _level) : new BinarySwitchSetRequest((byte) node, _level > 0));
		}
	}

	private class ThermostatTask extends TimerTask {
		@Override
		public void run() {
			for (Switch sw : switches.values()) {
				try {
					if (sw.isUrlThermostat() && !sw.isThermometer()) {
						double tempF = getTemperatureCelsius(sw) * 1.8 + 32;
						if (tempF > sw.getLevel() + 0.4) {
							setGroupSwitchLevel(sw.getNodeId(), 0, false);
							logger.info("Turning {} {} off, temp is: {} set to: {}", sw.getRoom(), sw.getName(), tempF + " set to: ", sw.getLevel());
						} else if (tempF < sw.getLevel() - 0.4) {
							setGroupSwitchLevel(sw.getNodeId(), (byte) 0xf, false);
							logger.info("Turning {} {} on, temp is: {} set to: {}", sw.getRoom(), sw.getName(), tempF + " set to: ", sw.getLevel());
						}
					}
				}
				catch (Throwable t) {
					logger.error("Failed to check temperature for thermostat {}", sw.getName());
				}
			}
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
				if (!tr.getSwitch().isHold()) {
					logger.info("Executing scheduled transition of node {} to level {}", tr.getSwitch().getNodeId(), tr.getLevel());
					Globals.app.setSwitchLevel(tr.getSwitch().getNodeId(), tr.getLevel());
				}
				else
					logger.info("Skipping scheduled transition of node {} to level {}, switch is on hold", tr.getSwitch().getNodeId(), tr.getLevel());
				ConcurrencyUtils.sleep(100);
			}
			nextScheduleTask = null;
			Globals.app.scheduleNextTransition();
		}
	}

	public double getTemperatureCelsius(int _nodeId) {
		return getTemperatureCelsius(switches.get(_nodeId));
	}

	private double getTemperatureCelsius(Switch _sw) {
		if ((pool == null) || (_sw == null) || !(_sw.isThermometer() || _sw.isThermostat()))
			return 0.0;
		if (_sw.isUrlThermostat())
			return DaoSerializer.getDouble(DaoSerializer.parse(pool.executeToString(new HttpGet(_sw.getThermostatSource()))), "temp");
		else if (_sw.isZWaveThermostat()) {
			synchronized (ZWAVE_MUTEX) {
				synchronized (temperatures) {
					controller.send(new MultilevelSensorGetRequest((byte) _sw.getNodeId()));
					try {
						temperatures.wait(5000);
					} catch (InterruptedException _e) {
						_e.printStackTrace();
					}
					Double temp = temperatures.get(_sw.getNodeId());
					return (temp == null) ? 0.0 : temp;
				}
			}
		}
		return 0.0;
	}
}

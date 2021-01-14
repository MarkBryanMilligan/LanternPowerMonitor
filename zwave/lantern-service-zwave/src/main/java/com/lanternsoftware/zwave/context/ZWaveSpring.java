package com.lanternsoftware.zwave.context;

public class ZWaveSpring {
/*	private ZWaveConfig config;
	private static ZWaveSession session;
	private static Map<Integer, Switch> switches = new HashMap<>();
	private static Map<Integer, List<Integer>> peers = new HashMap<>();
	private static Timer timer;
	private static HttpPool pool;
	private static SwitchScheduleTask nextScheduleTask;

	public void start() {
		try {
//			controller = new Controller();
//			controller.start("COM4");
			timer = new Timer("ZWaveApp Timer");
			pool = new HttpPool(10, 10, 30000, 10000, 10000);
			session = new LocalZwaveSession();
			session.connect();
			while (!session.isNetworkReady()) {
				System.out.println("Network not ready yet, sleeping");
				ConcurrencyUtils.sleep(1000);
			}
//			session.subscribe(new ZWaveEventListener());

//			for (ZWaveNode node : session.getDeviceManager().getNodes()) {
//				for (CommandClass cc : node.getCommandClasses()) {
//					System.out.println(node.getNodeId() + " " + cc.getClassCode() + " " + cc.getLabel());
//				}
//			}

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
		config = SerializationEngine.deserialize(ResourceLoader.loadFile(LanternFiles.OPS_PATH + "config.dat"), ZWaveConfig.class, SerializationEngine.SerializationType.JSON);
		Map<String, List<Integer>> groups = new HashMap<>();
		for (Switch sw : CollectionUtils.makeNotNull(config.getSwitches())) {
			switches.put(sw.getNodeId(), sw);
			CollectionUtils.addToMultiMap(sw.getRoom() + ":" + sw.getName(), sw.getNodeId(), groups);
		}
		if (CollectionUtils.filterOne(config.getSwitches(), _sw -> NullUtils.isNotEmpty(_sw.getThermostatSource())) != null) {
			timer.scheduleAtFixedRate(new ThermostatTask(), 0, 30000);
		}
		for (List<Integer> group : groups.values()) {
			for (Integer node : group) {
				peers.put(node, CollectionUtils.filter(group, _i -> !_i.equals(node)));
			}
		}
		scheduleNextTransition();
	}

	public void scheduleNextTransition() {
		TimeZone tz = TimeZone.getTimeZone("America/Chicago");
		if (nextScheduleTask != null)
			nextScheduleTask.cancel();
		Switch next = null;
		SwitchTransition transition = null;
		Date transitionDate = null;
		for (Switch sw : switches.values()) {
			for (SwitchTransition t : CollectionUtils.makeNotNull(sw.getSchedule())) {
				Date nextTransition = t.getNextTransition(tz);
				if ((transitionDate == null) || nextTransition.before(transitionDate)) {
					transitionDate = nextTransition;
					transition = t;
					next = sw;
				}
			}
		}
		if (transitionDate != null) {
			System.out.println("Next transition scheduled for node " + next.getNodeId() + " to level " + transition.getLevel() + " at " + DateUtils.format(tz, transitionDate, "hh:mm:ssa"));
			nextScheduleTask = new SwitchScheduleTask(next, transition);
			timer.schedule(nextScheduleTask, transitionDate);
		} else
			nextScheduleTask = null;
	}

	public void setSwitchLevel(int _nodeId, int _level) {
		Switch sw = switches.get(_nodeId);
		if ((sw == null) || !sw.isPrimary())
			return;
		sw.setLevel(_level);
		if (NullUtils.isEmpty(sw.getThermostatSource())) {
			doGroupSwitchAction(_nodeId, _level, sw.isMultilevel());
		} else {
			if (timer != null)
				timer.schedule(new ThermostatTask(), 0);
			persistConfig();
		}

	}

	public void setSwitchSchedule(int _nodeId, List<SwitchTransition> _transitions) {
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
			ResourceLoader.writeFile(LanternFiles.OPS_PATH + "config.dat", SerializationEngine.serialize(config, SerializationEngine.SerializationType.JSON));
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
		session.shutdown();
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		if (pool != null) {
			pool.shutdown();
			pool = null;
		}
	}

	/*
		public static class ZWaveEventListener implements EventHandler {
			@EventSubscribe
			public void receive(ZWaveEvent event) throws Exception {
				if (event instanceof ApplicationCommandEvent) {
					ApplicationCommandEvent ace = (ApplicationCommandEvent) event;
					if (ace.getCommandClass() == CommandClass.SWITCH_MULTILEVEL) {
						for (Integer node : CollectionUtils.makeNotNull(peers.get(ace.getNodeId()))) {
							Switch sw = switches.get(node);
							System.out.println("Mirror Event from node " + ((ApplicationCommandEvent) event).getNodeId() + " to node " + node);
	//						session.doAction(new SwitchAction(node, ace.getPayload()[1], sw == null || sw.isMultilevel()));
						}
					}
				}
			}

			@EventSubscribe
			public void handleSensorEvent(DeviceSensorEvent sensorEvent) {
			}
		}

	private void doGroupSwitchAction(int _primary, int _level, boolean _multilevel) {
		List<Integer> nodes = CollectionUtils.asArrayList(_primary);
		nodes.addAll(CollectionUtils.makeNotNull(peers.get(_primary)));
		for (int node : nodes) {
			try {
				session.doAction(new SwitchAction(node, _level, _multilevel));
			} catch (HomeAutomationException _e) {
				_e.printStackTrace();
			}
		}
	}

	private class ThermostatTask extends TimerTask {
		@Override
		public void run() {
			for (Switch sw : switches.values()) {
				if (NullUtils.isNotEmpty(sw.getThermostatSource())) {
					double tempF = getTemperatureCelsius(sw) * 1.8 + 32;
					if (tempF > sw.getLevel() + 0.4) {
						doGroupSwitchAction(sw.getNodeId(), 0, false);
						System.out.println("Turning " + sw.getRoom() + " " + sw.getName() + " off, temp is: " + tempF + " set to: " + sw.getLevel());
					} else if (tempF < sw.getLevel() - 0.4) {
						doGroupSwitchAction(sw.getNodeId(), (byte) 0xf, false);
						System.out.println("Turning " + sw.getRoom() + " " + sw.getName() + " on, temp is: " + tempF + " set to: " + sw.getLevel());
					}
				}
			}
		}
	}

	private class SwitchScheduleTask extends TimerTask {
		private final Switch sw;
		private final SwitchTransition transition;

		public SwitchScheduleTask(Switch _sw, SwitchTransition _transition) {
			sw = _sw;
			transition = _transition;
		}

		@Override
		public void run() {
			System.out.println("Executing scheduled transition of node " + sw.getNodeId() + " to level " + transition.getLevel());
			if (!sw.isHold()) {
				Globals.app.setSwitchLevel(sw.getNodeId(), transition.getLevel());
			}
			nextScheduleTask = null;
			Globals.app.scheduleNextTransition();
		}
	}

	public double getTemperatureCelsius(int _nodeId) {
		return getTemperatureCelsius(switches.get(_nodeId));
	}

	private static double getTemperatureCelsius(Switch _sw) {
		if ((pool == null) || (_sw == null) || NullUtils.isEmpty(_sw.getThermostatSource()))
			return 0.0;
		return BsonUtils.getDouble(BsonUtils.parse(pool.executeToString(new HttpGet(_sw.getThermostatSource()))), "temp");
	}*/
}

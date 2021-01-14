package com.lanternsoftware.zwave;

import com.lanternsoftware.dataaccess.currentmonitor.CurrentMonitorDao;
import com.lanternsoftware.dataaccess.currentmonitor.MongoCurrentMonitorDao;
import com.lanternsoftware.datamodel.zwave.Switch;
import com.lanternsoftware.datamodel.zwave.SwitchSchedule;
import com.lanternsoftware.datamodel.zwave.ZWaveConfig;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.LanternFiles;
import com.lanternsoftware.util.ResourceLoader;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.mongo.MongoConfig;
import com.lanternsoftware.zwave.dao.MongoZWaveDao;

public class CreateConfig {
	public static void main(String[] args) {
		MongoZWaveDao dao = new MongoZWaveDao(MongoConfig.fromDisk(LanternFiles.OPS_PATH + "mongo.cfg"));
		ZWaveConfig config = dao.getConfig(1);
//		ZWaveConfig cconfig = DaoSerializer.parse(ResourceLoader.loadFile(LanternFiles.OPS_PATH + "config - christmas lights.dat"), ZWaveConfig.class);
//		Switch c = CollectionUtils.filterOne(config.getSwitches(), _s->_s.getName().contains("hristm"));
//		CollectionUtils.filterMod(config.getSwitches(), _s->!_s.getRoom().equals("Treehouse"));
//		Switch treehouse = new Switch("Treehouse", "Interior", 14, true, true, null, 0);
//		Switch to = new Switch("Treehouse", "Floods", 15, true, true, null, 0);
//		Switch out = new Switch("Outside", "Repeater Outlet", 10, true, false, null, 0);
//		config.getSwitches().add(out);
		Switch c = CollectionUtils.filterOne(config.getSwitches(), _s->_s.getName().contains("Agitator"));
		c.setName("Septic Aerator");
		dao.putConfig(config);
//		if (c != null) {
//			c.setNodeId(8);
//			dao.putConfig(config);
//		}
//		ZWaveConfig config = DaoSerializer.parse(ResourceLoader.loadFile(LanternFiles.OPS_PATH + "config.dat"), ZWaveConfig.class);
//		config.setAccountId(1);
//		config.getSwitches().add(new Switch("Garage", "Septic Agitator", 12, 0, true, false, false, null, 0, Arrays.asList(new SwitchTransition(20))));
//		Switch thermo = CollectionUtils.filterOne(config.getSwitches(), _sw->_sw.getNodeId() == 0);
//		if (thermo != null)
//			thermo.setNodeId(100);
//		config.getSwitches().add(new Switch("Basement", "Temperature", 101, true, false, "https://basement.lanternsoftware.com/thermometer/temp", 0));

/*		ZWaveConfig config = new ZWaveConfig();
		List<Switch> switches = new ArrayList<>();
		switches.add(new Switch("Basement", "Main", 3, true, true, null, 0));
		switches.add(new Switch("Basement", "Main", 5, false, true, null, 0));
		switches.add(new Switch("Basement", "Bar", 4, true, true, null, 0));
		switches.add(new Switch("Basement", "Bar", 6, false, true, null, 0));
		switches.add(new Switch("Master Bedroom", "Heater", 7, true, true, "https://thermometer.lanternsoftware.com/thermometer/temp", 0));
		switches.add(new Switch("Bruce's Room", "Heater", 8, true, true, "https://bruce.lanternsoftware.com/thermometer/temp", 0));
		switches.add(new Switch("Master Bedroom", "Heater", 9, false, true, "", 0));
		Switch out = new Switch("Outside", "Christmas Lights", 10, true, false, "", 0);
		out.setSchedule(CollectionUtils.asArrayList(
				new SwitchTransition(Calendar.FRIDAY, 12, 42, 0, 0),
				new SwitchTransition(Calendar.FRIDAY, 12, 42, 10, 0xFF),
				new SwitchTransition(Calendar.FRIDAY, 12, 42, 20, 0),
				new SwitchTransition(Calendar.FRIDAY, 12, 42, 30, 0xFF),
				new SwitchTransition(Calendar.FRIDAY, 12, 42, 40, 0),
				new SwitchTransition(Calendar.FRIDAY, 12, 42, 50, 0xFF)));
		switches.add(out);
		config.setSwitches(switches);
 */
//		config.setSwitches(switches);
//		Switch sump = CollectionUtils.filterOne(config.getSwitches(), _c->_c.getNodeId() == 12);
//		SwitchSchedule transition = CollectionUtils.getFirst(sump.getSchedule());
//		transition.setLevel(0xFF);
//		transition.setMinutesPerHour(15);
//		dao.putConfig(config);
		dao.shutdown();
//		TimeZone tz = TimeZone.getTimeZone("America/Chicago");
//		Date next = transition.getNextTransition(tz);
//		System.out.println("Next Transition: " + DateUtils.format(tz, next, "hh:mm:ssa"));

//		ResourceLoader.writeFile(LanternFiles.OPS_PATH + "config.dat", DaoSerializer.toJson(config));
	}
}

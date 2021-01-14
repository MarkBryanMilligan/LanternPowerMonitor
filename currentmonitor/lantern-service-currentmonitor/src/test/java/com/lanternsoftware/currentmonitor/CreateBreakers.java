package com.lanternsoftware.currentmonitor;

import com.lanternsoftware.dataaccess.currentmonitor.CurrentMonitorDao;
import com.lanternsoftware.dataaccess.currentmonitor.MongoCurrentMonitorDao;
import com.lanternsoftware.datamodel.currentmonitor.Breaker;
import com.lanternsoftware.datamodel.currentmonitor.BreakerConfig;
import com.lanternsoftware.datamodel.currentmonitor.BreakerGroup;
import com.lanternsoftware.datamodel.currentmonitor.BreakerHub;
import com.lanternsoftware.datamodel.currentmonitor.BreakerPanel;
import com.lanternsoftware.datamodel.currentmonitor.BreakerPolarity;
import com.lanternsoftware.datamodel.currentmonitor.Meter;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.LanternFiles;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.ResourceLoader;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.mongo.MongoConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CreateBreakers {
	public static void main(String[] args) {
		CurrentMonitorDao dao = new MongoCurrentMonitorDao(MongoConfig.fromDisk(LanternFiles.OPS_PATH + "mongo.cfg"));

/*		Breaker bf1 = new Breaker("Solar A", 2, 20, 0, 1, 50, 1.6);
		bf1.setPolarity(BreakerPolarity.SOLAR);
		Breaker bf2 = new Breaker("Solar B", 2, 18, 0, 2, 50, 1.6);
		bf2.setPolarity(BreakerPolarity.SOLAR);
		Breaker bf3 = new Breaker("Septic Agitator", 2, 11, 0, 3, 20, 1.6);
		Breaker bf4 = new Breaker("Garage/Guest Baths", 2, 5, 0, 4, 20, 1.6);
		Breaker bf5 = new Breaker("Office", 2, 2, 0, 5, 20, 1.6);
		Breaker bf6 = new Breaker("Upstairs Furnace R-A", 1, 2, 0, 6, 50, 1.6);
		Breaker bf7 = new Breaker("Upstairs Furnace R-B", 1, 4, 0, 7, 50, 1.6);
		Breaker bf8 = new Breaker("Upstairs Furnace R-C", 1, 6, 0, 8, 30, 1.6);
		Breaker bf9 = new Breaker("Upstairs Furnace R-D", 1, 8, 0, 9, 30, 1.6);
		Breaker bf10 = new Breaker("Upstairs Furnace HP-A", 1, 1, 0, 10, 30, 1.6);
		Breaker bf11 = new Breaker("Upstairs Furnace HP-B", 1, 3, 0, 11, 30, 1.6);
		Breaker bf12 = new Breaker("Dryer A", 2, 8, 0, 12, 30, 1.6);
		Breaker bf13 = new Breaker("Dryer B", 2, 10, 0, 13, 30, 1.6);
		Breaker bf2_1 = new Breaker("Main Furnace R-A", 0, 6, 1, 1, 50, 1.6);
		Breaker bf2_2 = new Breaker("Main Furnace R-B", 0, 8, 1, 2, 50, 1.6);
		Breaker bf2_3 = new Breaker("Main Furnace R-C", 0, 2, 1, 3, 50, 1.6);
		Breaker bf2_4 = new Breaker("Main Furnace R-D", 0, 4, 1, 4, 50, 1.6);
		Breaker bf2_5 = new Breaker("Main Furnace HP-A", 0, 1, 1, 5, 30, 1.6);
		Breaker bf2_6 = new Breaker("Main Furnace HP-B", 0, 3, 1, 6, 30, 1.6);
		Breaker bf2_7 = new Breaker("Hot Water Heater A", 0, 5, 1, 7, 30, 1.6);
		Breaker bf2_8 = new Breaker("Hot Water Heater B", 0, 7, 1, 8, 30, 1.6);
		Breaker bf2_9 = new Breaker("Basement HP-A", 1, 13, 1, 9, 30, 1.6);
		Breaker bf2_10 = new Breaker("Basement HP-B", 1, 15, 1, 10, 30, 1.6);
		Breaker bf2_11 = new Breaker("Oven A", 2, 12, 1, 11, 50, 1.6);
		Breaker bf2_12 = new Breaker("Oven B", 2, 14, 1, 12, 50, 1.6);
		Breaker bf2_13 = new Breaker("Master Bathroom", 3, 9, 2, 5, 20, 1.6);
		Breaker bf2_14 = new Breaker("Refrigerator", 2, 6, 1, 14, 20, 1.6);
		bf2_14.setSpaceTandemB(6);
		Breaker bf2_15 = new Breaker("Master Bedroom", 2, 1, 1, 15, 20, 1.6);
		BreakerGroup basementHP = new BreakerGroup("6", "Heat Pump", Arrays.asList(bf2_9, bf2_10));
		BreakerGroup basementCC = new BreakerGroup("3", "Basement", Arrays.asList(basementHP), null);
		BreakerGroup mainHP = new BreakerGroup("5", "Heat Pump", Arrays.asList(bf2_5, bf2_6));
		BreakerGroup mainR = new BreakerGroup("4", "Air Handler/Resistive", Arrays.asList(bf2_1, bf2_2, bf2_3, bf2_4));
		BreakerGroup mainCC = new BreakerGroup("2", "Main Floor", Arrays.asList(mainHP, mainR), null);
		BreakerGroup upstairsHP = new BreakerGroup("34", "Heat Pump", Arrays.asList(bf10, bf11));
		BreakerGroup upstairsR = new BreakerGroup("35", "Air Handler/Resistive", Arrays.asList(bf6, bf7, bf8, bf9));
		BreakerGroup upstairsCC = new BreakerGroup("36", "Upstairs", Arrays.asList(upstairsHP, upstairsR), null);
		BreakerGroup cc = new BreakerGroup("1", "Climate Control", Arrays.asList(mainCC, upstairsCC, basementCC), null);
		BreakerGroup hotWater = new BreakerGroup("7", "Hot Water Heater", Arrays.asList(bf2_7, bf2_8));
		BreakerGroup oven = new BreakerGroup("8", "Oven/Cooktop", Arrays.asList(bf2_11, bf2_12));
		BreakerGroup masterBR = new BreakerGroup("9", "Master Bedroom", Arrays.asList(bf2_13, bf2_15));
		BreakerGroup fridge = new BreakerGroup("10", "Refrigerator", Arrays.asList(bf2_14));
		BreakerGroup solar = new BreakerGroup("11", "Solar", Arrays.asList(bf1, bf2));
		BreakerGroup septic = new BreakerGroup("13", "Septic Aerator", Arrays.asList(bf3));
		BreakerGroup garage = new BreakerGroup("14", "Garage/Guest Baths", Arrays.asList(bf4));
		BreakerGroup office = new BreakerGroup("15", "Office", Arrays.asList(bf5));
		BreakerGroup dryer = new BreakerGroup("37", "Dryer", Arrays.asList(bf12, bf13));
		Breaker bf3_1 = new Breaker("Hub 2 Port 1", 3, 1, 2, 1, 20, 1.6);
		Breaker bf3_2 = new Breaker("Hub 2 Port 2", 3, 3, 2, 2, 20, 1.6);
		Breaker bf3_3 = new Breaker("Hub 2 Port 3", 3, 5, 2, 3, 20, 1.6);
		Breaker bf3_4 = new Breaker("Hub 2 Port 4", 3, 7, 2, 4, 20, 1.6);
		Breaker bf3_6 = new Breaker("Hub 2 Port 6", 3, 11, 2, 6, 20, 1.6);
		Breaker bf3_7 = new Breaker("Hub 2 Port 7", 3, 13, 2, 7, 20, 1.6);
		Breaker bf3_8 = new Breaker("Hub 2 Port 8", 3, 15, 2, 8, 20, 1.6);
		Breaker bf3_9 = new Breaker("Hub 2 Port 9", 3, 2, 2, 9, 20, 1.6);
		Breaker bf3_10 = new Breaker("Hub 2 Port 10", 3, 4, 2, 10, 20, 1.6);
		Breaker bf3_12 = new Breaker("Hub 2 Port 12", 3, 6, 2, 12, 20, 1.6);
		Breaker bf3_13 = new Breaker("Hub 2 Port 13", 3, 8, 2, 13, 20, 1.6);
		Breaker bf3_14 = new Breaker("Hub 2 Port 14", 3, 10, 2, 14, 20, 1.6);
		BreakerGroup g1 = new BreakerGroup("17", "Living Room/Printer/Outdoor Lights", Arrays.asList(bf3_1));
		BreakerGroup g2 = new BreakerGroup("18", "Dishwasher/Disposal/Sink Lights", Arrays.asList(bf3_2));
		BreakerGroup g3 = new BreakerGroup("19", "Microwave", Arrays.asList(bf3_3));
		BreakerGroup g4 = new BreakerGroup("20", "Kitchen Outlets", Arrays.asList(bf3_4));
//		BreakerGroup g5 = new BreakerGroup("21", "Hub 2 Port 5", Arrays.asList(bf3_5));
		BreakerGroup g6 = new BreakerGroup("22", "Mini Fridge", Arrays.asList(bf3_6));
		BreakerGroup g7 = new BreakerGroup("23", "Basement Lights/Outlets", Arrays.asList(bf3_7));
		BreakerGroup g8 = new BreakerGroup("24", "Theatre", Arrays.asList(bf3_8));
		BreakerGroup g9 = new BreakerGroup("25", "Sunroom Fan/Outside Floods", Arrays.asList(bf3_9));
		BreakerGroup g10 = new BreakerGroup("26", "Radon/Deep Freezer", Arrays.asList(bf3_10));
//		BreakerGroup g11 = new BreakerGroup("27", "Hub 2 Port 11", Arrays.asList(bf3_11));
		BreakerGroup g12 = new BreakerGroup("28", "Kitchen Lights", Arrays.asList(bf3_12));
		BreakerGroup g13 = new BreakerGroup("29", "Router/Networking", Arrays.asList(bf3_13));
		BreakerGroup g14 = new BreakerGroup("30", "Half Bath/Utility/Garage Lights", Arrays.asList(bf3_14));
//		BreakerGroup g15 = new BreakerGroup("31", "Bar Outlets", Arrays.asList(bf3_15));
		BreakerGroup kitchen = new BreakerGroup("33", "Kitchen", Arrays.asList(oven, fridge, g2, g3, g4, g12), null);

		Breaker b4_1 = new Breaker("Hub 3 Port 1", 4, 1, 3, 1, 20, 1.6);
		Breaker b4_2 = new Breaker("Hub 3 Port 2", 4, 2, 3, 2, 30, 1.6);
		Breaker b4_3 = new Breaker("Hub 3 Port 3", 4, 3, 3, 3, 50, 1.6);
		Breaker b4_4 = new Breaker("Hub 3 Port 4", 4, 4, 3, 4, 20, 1.6);
		Breaker b4_5 = new Breaker("Hub 3 Port 5", 4, 5, 3, 5, 20, 1.6);
		Breaker b4_6 = new Breaker("Hub 3 Port 6", 4, 6, 3, 6, 20, 1.6);
		Breaker b4_7 = new Breaker("Hub 3 Port 7", 4, 7, 3, 7, 20, 1.6);
		Breaker b4_8 = new Breaker("Hub 3 Port 8", 4, 8, 3, 8, 20, 1.6);
		Breaker b4_9 = new Breaker("Hub 3 Port 9", 4, 9, 3, 9, 20, 1.6);
		Breaker b4_10 = new Breaker("Hub 3 Port 10", 4, 10, 3, 10, 20, 1.6);
		Breaker b4_11 = new Breaker("Hub 3 Port 11", 4, 11, 3, 11, 20, 1.6);
		Breaker b4_12 = new Breaker("Hub 3 Port 12", 4, 12, 3, 12, 20, 1.6);
		Breaker b4_13 = new Breaker("Hub 3 Port 13", 4, 13, 3, 13, 20, 1.6);
		Breaker b4_14 = new Breaker("Hub 3 Port 14", 4, 14, 3, 14, 20, 1.6);
		Breaker b4_15 = new Breaker("Hub 3 Port 15", 4, 15, 3, 15, 20, 1.6);
		BreakerGroup g4_1 = new BreakerGroup("41", "Hub 3 Port 1", Arrays.asList(b4_1));
		BreakerGroup g4_2 = new BreakerGroup("42", "Hub 3 Port 2", Arrays.asList(b4_2));
		BreakerGroup g4_3 = new BreakerGroup("43", "Hub 3 Port 3", Arrays.asList(b4_3));
		BreakerGroup g4_4 = new BreakerGroup("44", "Hub 3 Port 4", Arrays.asList(b4_4));
		BreakerGroup g4_5 = new BreakerGroup("45", "Hub 3 Port 5", Arrays.asList(b4_5));
		BreakerGroup g4_6 = new BreakerGroup("46", "Hub 3 Port 6", Arrays.asList(b4_6));
		BreakerGroup g4_7 = new BreakerGroup("47", "Hub 3 Port 7", Arrays.asList(b4_7));
		BreakerGroup g4_8 = new BreakerGroup("48", "Hub 3 Port 8", Arrays.asList(b4_8));
		BreakerGroup g4_9 = new BreakerGroup("49", "Hub 3 Port 9", Arrays.asList(b4_9));
		BreakerGroup g4_10 = new BreakerGroup("50", "Hub 3 Port 10", Arrays.asList(b4_10));
		BreakerGroup g4_11 = new BreakerGroup("51", "Hub 3 Port 11", Arrays.asList(b4_11));
		BreakerGroup g4_12 = new BreakerGroup("52", "Hub 3 Port 12", Arrays.asList(b4_12));
		BreakerGroup g4_13 = new BreakerGroup("53", "Hub 3 Port 13", Arrays.asList(b4_13));
		BreakerGroup g4_14 = new BreakerGroup("54", "Hub 3 Port 14", Arrays.asList(b4_14));
		BreakerGroup g4_15 = new BreakerGroup("55", "Hub 3 Port 15", Arrays.asList(b4_15));
		BreakerGroup debug = new BreakerGroup("40", "Debug Hub 3", Arrays.asList(g4_1, g4_2, g4_3), null);
//		BreakerGroup debug = new BreakerGroup("40", "Debug Hub 3", Arrays.asList(g4_1, g4_2, g4_3, g4_4, g4_5, g4_6, g4_7, g4_8, g4_9, g4_10, g4_11, g4_12, g4_13, g4_14, g4_15), null);
//		BreakerGroup debug = new BreakerGroup("40", "Debug Hub 3", Arrays.asList(b4_1));


		BreakerGroup house = new BreakerGroup("0", "*redacted*", Arrays.asList(solar, cc, hotWater, dryer, septic, masterBR, garage, office, kitchen, g1, g6, g7, g8, g9, g10, g13, g14, debug), null);
		BreakerConfig config = new BreakerConfig(Collections.singletonList(house));
		CollectionUtils.edit(config.getAllBreakerGroups(), _g->_g.setAccountId(1));
		CollectionUtils.edit(config.getAllBreakers(), _b->{
			_b.setAccountId(1);
			_b.setCalibrationFactor(1.0);
		});
		b4_1.setCalibrationFactor(1.215);
		b4_2.setCalibrationFactor(1.215);
		b4_3.setCalibrationFactor(1.215);
		config.setAccountId(1);
		BreakerHub hub0 = new BreakerHub();
		hub0.setHub(0);
		hub0.setVoltageCalibrationFactor(0.4587);
		hub0.setFrequency(60);
		BreakerHub hub1 = new BreakerHub();
		hub1.setHub(1);
		hub1.setVoltageCalibrationFactor(0.439);
		hub1.setFrequency(60);
		BreakerHub hub2 = new BreakerHub();
		hub2.setHub(2);
		hub2.setVoltageCalibrationFactor(0.3535);
		hub2.setFrequency(60);
		BreakerHub hub3 = new BreakerHub();
		hub3.setHub(3);
		hub3.setVoltageCalibrationFactor(0.419);
		hub3.setFrequency(60);
		config.setBreakerHubs(Arrays.asList(hub0, hub1, hub2, hub3));

		Meter heatMeter = new Meter();
		heatMeter.setAccountId(1);
		heatMeter.setIndex(0);
		heatMeter.setName("Heat");

		Meter mainMeter = new Meter();
		mainMeter.setAccountId(1);
		mainMeter.setIndex(1);
		mainMeter.setName("Main/Solar");

		config.setMeters(CollectionUtils.asArrayList(heatMeter, mainMeter));

		BreakerPanel heat1 = new BreakerPanel();
		heat1.setAccountId(1);
		heat1.setIndex(0);
		heat1.setMeter(heatMeter.getIndex());
		heat1.setName("Heat 1");
		heat1.setSpaces(20);

		BreakerPanel heat2 = new BreakerPanel();
		heat2.setAccountId(1);
		heat2.setIndex(1);
		heat2.setMeter(heatMeter.getIndex());
		heat2.setName("Heat 2");
		heat2.setSpaces(20);

		BreakerPanel main = new BreakerPanel();
		main.setAccountId(1);
		main.setIndex(2);
		main.setMeter(mainMeter.getIndex());
		main.setName("Main");
		main.setSpaces(20);

		BreakerPanel sub = new BreakerPanel();
		sub.setAccountId(1);
		sub.setIndex(3);
		sub.setMeter(mainMeter.getIndex());
		sub.setName("Sub-Panel");
		sub.setSpaces(20);

		config.setPanels(CollectionUtils.asArrayList(heat1, heat2, main, sub));

		Map<Integer, Integer> panelToMeter = CollectionUtils.transformToMap(config.getPanels(), BreakerPanel::getIndex, BreakerPanel::getMeter);
		CollectionUtils.edit(config.getAllBreakers(), _b->_b.setMeter(DaoSerializer.toInteger(panelToMeter.get(_b.getPanel()))));*/

		BreakerConfig config = dao.getConfig(1);
//		BreakerConfig config = DaoSerializer.parse(ResourceLoader.loadFileAsString(LanternFiles.OPS_PATH + "breakerconfig_backup_210107.json"), BreakerConfig.class);
//		ResourceLoader.writeFile(LanternFiles.OPS_PATH + "breakerconfig_backup_210107.json", DaoSerializer.toJson(config));
//		CollectionUtils.edit(config.getAllBreakerGroups(), _g->{
//			if (NullUtils.isEmpty(_g.getName())) {
//				Breaker b = CollectionUtils.getFirst(_g.getBreakers());
//				if (b != null)
//					_g.setName(String.format("Panel %d, %s", b.getPanel(), b.getName()));
//			}
//		});
//		for (BreakerGroup group : CollectionUtils.filter(config.getAllBreakerGroups(), _g->NullUtils.isEmpty(_g.getId()))) {
//			List<Integer> ids = CollectionUtils.transform(config.getAllBreakerGroupIds(), NullUtils::toInteger);
//			group.setId(String.valueOf(CollectionUtils.getLargest(ids) + 1));
//		}
//		BreakerGroup root = CollectionUtils.getFirst(config.getBreakerGroups());
//		root.getSubGroups().removeIf(_g->_g.getName().equals("Debug Hub 3"));
//		config.removeInvalidGroups();
//		for (BreakerGroup group : config.getAllBreakerGroups()) {
//			if (NullUtils.isEmpty(group.getId())) {
//				List<Integer> ids = CollectionUtils.transform(config.getAllBreakerGroupIds(), NullUtils::toInteger);
//				group.setId(String.valueOf(CollectionUtils.getLargest(ids) + 1));
//			}
//		}
		dao.putConfig(config);
		dao.shutdown();
	}
}

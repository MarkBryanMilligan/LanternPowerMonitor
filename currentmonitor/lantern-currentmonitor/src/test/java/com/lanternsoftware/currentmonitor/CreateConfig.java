package com.lanternsoftware.currentmonitor;


import com.lanternsoftware.util.LanternFiles;
import com.lanternsoftware.util.ResourceLoader;
import com.lanternsoftware.util.dao.DaoSerializer;

public class CreateConfig {
	public static void main(String[] args) {
//		MonitorConfig c = new MonitorConfig(0, "https://mark.lanternsoftware.com/currentmonitor");
		MonitorConfig c = new MonitorConfig(1, "https://mark.lanternsoftware.com/currentmonitor");
		c.setDebug(true);
		ResourceLoader.writeFile(LanternFiles.OPS_PATH + "hub1.json", DaoSerializer.toJson(c));
	}
}

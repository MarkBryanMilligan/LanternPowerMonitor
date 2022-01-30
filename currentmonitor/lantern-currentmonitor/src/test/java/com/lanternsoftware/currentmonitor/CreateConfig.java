package com.lanternsoftware.currentmonitor;


import com.lanternsoftware.datamodel.currentmonitor.Breaker;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.external.LanternFiles;
import com.lanternsoftware.util.ResourceLoader;
import com.lanternsoftware.util.dao.DaoSerializer;

public class CreateConfig {
	public static void main(String[] args) {
		MonitorConfig c = new MonitorConfig(1, "https://lanternsoftware.com/currentmonitor");
		c.setHost("");
		c.setDebug(false);
		c.setMqttBrokerUrl("http://192.168.1.80:1883");
		c.setMqttFrequency(60);
		c.setMqttPortCalibrationFactor(1.0);
		c.setMqttVoltageCalibrationFactor(1.0);
		Breaker b1 = new Breaker();
		b1.setPanel(0);
		b1.setSpace(1);
		b1.setHub(0);
		b1.setPort(1);
		b1.setSizeAmps(20);
		c.setMqttBreakers(CollectionUtils.asArrayList(b1));
		ResourceLoader.writeFile(LanternFiles.CONFIG_PATH + "mqtt1.json", DaoSerializer.toJson(c));
	}
}

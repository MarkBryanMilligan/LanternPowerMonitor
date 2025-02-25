package com.lanternsoftware.powermonitor;


import com.lanternsoftware.util.external.LanternFiles;
import com.lanternsoftware.util.dao.generator.DaoSerializerGenerator;

public class PowerMonitorAppSerializers {
	public static void main(String[] args) {
		DaoSerializerGenerator.generateSerializers(LanternFiles.SOURCE_CODE_PATH + "powermonitor", true, null);
	}
}

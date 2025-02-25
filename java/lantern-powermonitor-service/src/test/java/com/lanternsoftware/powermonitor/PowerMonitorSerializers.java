package com.lanternsoftware.powermonitor;


import com.lanternsoftware.util.dao.generator.DaoSerializerGenerator;
import com.lanternsoftware.util.external.LanternFiles;

public class PowerMonitorSerializers {
	public static void main(String[] args) {
		DaoSerializerGenerator.generateSerializers(LanternFiles.SOURCE_CODE_PATH + "powermonitor", true, null);
//		SwiftModelGenerator.generateModel(LanternFiles.SOURCE_CODE_PATH + "powermonitor", LanternFiles.SOURCE_CODE_PATH + "iOS");
	}
}

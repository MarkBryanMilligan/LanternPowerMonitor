package com.lanternsoftware.currentmonitor;


import com.lanternsoftware.util.external.LanternFiles;
import com.lanternsoftware.util.dao.generator.DaoSerializerGenerator;
import com.lanternsoftware.util.dao.generator.SwiftModelGenerator;

public class CurrentMonitorSerializers {
	public static void main(String[] args) {
		DaoSerializerGenerator.generateSerializers(LanternFiles.SOURCE_CODE_PATH + "currentmonitor", true, null);
		SwiftModelGenerator.generateModel(LanternFiles.SOURCE_CODE_PATH + "currentmonitor", LanternFiles.SOURCE_CODE_PATH + "iOS");
	}
}

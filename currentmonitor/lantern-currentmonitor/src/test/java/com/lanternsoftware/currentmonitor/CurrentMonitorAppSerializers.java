package com.lanternsoftware.currentmonitor;


import com.lanternsoftware.util.LanternFiles;
import com.lanternsoftware.util.dao.generator.DaoSerializerGenerator;

public class CurrentMonitorAppSerializers {
	public static void main(String[] args) {
		DaoSerializerGenerator.generateSerializers(LanternFiles.SOURCE_PATH + "currentmonitor", true, null);
	}
}

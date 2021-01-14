package com.lanternsoftware.zwave;


import com.lanternsoftware.util.LanternFiles;
import com.lanternsoftware.util.dao.generator.DaoSerializerGenerator;

public class GenerateSerializers {
	public static void main(String[] args) {
		DaoSerializerGenerator.generateSerializers(LanternFiles.SOURCE_PATH + "zwave", true, null);
	}
}

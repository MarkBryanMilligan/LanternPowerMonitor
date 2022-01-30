package com.lanternsoftware.zwave;


import com.lanternsoftware.util.external.LanternFiles;
import com.lanternsoftware.util.dao.generator.DaoSerializerGenerator;

public class GenerateSerializers {
	public static void main(String[] args) {
		DaoSerializerGenerator.generateSerializers(LanternFiles.SOURCE_CODE_PATH, true, null);
	}
}

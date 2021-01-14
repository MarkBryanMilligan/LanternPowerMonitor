package com.lanternsoftware.currentmonitor;

import com.lanternsoftware.util.LanternFiles;
import com.lanternsoftware.util.ResourceLoader;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.xml.XmlNode;
import com.lanternsoftware.util.xml.XmlParser;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

public class ReleaseCurrentMonitor {
	public static void main(String[] args) {
		XmlNode pom = XmlParser.loadXmlFile(LanternFiles.SOURCE_PATH + "currentmonitor" + File.separator + "lantern-currentmonitor"  + File.separator + "pom.xml");
		if (pom == null)
			return;
		XmlNode versionNode = pom.getChild(Collections.singletonList("version"));
		String version = versionNode.getContent();
		ProcessBuilder builder = new ProcessBuilder();
		builder.directory(new File(LanternFiles.SOURCE_PATH));
		builder.command("cmd.exe", "/c", "mvn clean install");
		builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
		try {
			Process process = builder.start();
			int exitCode = process.waitFor();
			assert exitCode == 0;
		} catch (Exception _e) {
			_e.printStackTrace();
		}
		byte[] jar = ResourceLoader.loadFile(LanternFiles.SOURCE_PATH + "currentmonitor" + File.separator + "lantern-currentmonitor" + File.separator + "target" + File.separator + "lantern-currentmonitor.jar");
		DaoEntity meta = new DaoEntity("version", version).and("size", jar.length).and("checksum", DigestUtils.md5Hex(jar));
		ResourceLoader.writeFile(LanternFiles.OPS_PATH + "release" + File.separator + "lantern-currentmonitor.jar", jar);
		ResourceLoader.writeFile(LanternFiles.OPS_PATH + "release" + File.separator + "version.json", DaoSerializer.toJson(meta));
	}
}

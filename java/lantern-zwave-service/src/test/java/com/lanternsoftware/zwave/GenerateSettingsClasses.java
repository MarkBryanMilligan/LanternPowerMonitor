package com.lanternsoftware.zwave;

import com.lanternsoftware.util.xml.XmlNode;
import com.lanternsoftware.util.xml.XmlParser;

import java.util.List;

public class GenerateSettingsClasses {
	public static void main(String[] args) {
		XmlNode xml = XmlParser.loadXmlFile("C:\\lantern\\opensource\\open-zwave-master\\config\\zooz\\zen77.xml");
		List<XmlNode> values = xml.getChildren("CommandClass", "Value");
		for (XmlNode value : values) {

		}
	}
}

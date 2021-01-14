package com.lanternsoftware.util.xml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Stack;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.lanternsoftware.util.NullUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlParser {
    protected static final Logger LOG = LoggerFactory.getLogger(XmlParser.class);
    
    public static XmlNode loadXmlFile(String _filePath) {
        FileInputStream is = null;
        try {
            is = new FileInputStream(_filePath);
            return parseXml(is);
        }
        catch (Exception _e) {
            LOG.error("Failed to load xml file", _e);
            return null;
        }
        finally {
            IOUtils.closeQuietly(is);
        }
    }

    public static XmlNode parseXml(InputStream _is) {
        XMLStreamReader reader = null;
        try {
            XmlNode node = null;
            StringBuilder content = null;
            Stack<XmlNode> stack = new Stack<XmlNode>();
            reader = XMLInputFactory.newInstance().createXMLStreamReader(_is);
            while (reader.hasNext()) {
                switch (reader.next()) {
                    case XMLStreamConstants.START_ELEMENT: {
                        node = new XmlNode();
                        content = new StringBuilder();
                        for (int i = 0; i < reader.getAttributeCount(); i++) {
                            node.getAttributes().put(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
                        }
                        stack.push(node);
                        break;
                    }
                    case XMLStreamConstants.CHARACTERS: {
                        content.append(NullUtils.makeNotNull(reader.getText()));
                        break;
                    }
                    case XMLStreamConstants.END_ELEMENT: {
                        node = stack.pop();
                        if (stack.empty())
                            return node;
                        stack.peek().addChild(reader.getLocalName(), node);
                        if (content != null)
                            node.setContent(content.toString().trim());
                        break;
                    }
                }
            }
        }
        catch (Exception _e) {
            LOG.error("Failed to parse XML", _e);
        }
        finally {
            try {
                reader.close();
            }
            catch (XMLStreamException _e) {
                LOG.error("Failed to close XML stream", _e);
            }
            IOUtils.closeQuietly(_is);
        }
        return null;
    }
}

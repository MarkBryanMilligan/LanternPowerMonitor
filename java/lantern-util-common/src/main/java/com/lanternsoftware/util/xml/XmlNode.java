package com.lanternsoftware.util.xml;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlNode
{
    private String content;
    private final Map<String, String> attributes = new HashMap<String, String>();
    private final Map<String, List<XmlNode>> children = new HashMap<String, List<XmlNode>>();

    public String getContent()
    {
        return content;
    }

    public void setContent(String _content)
    {
        content = _content;
    }

    public Map<String, String> getAttributes()
    {
        return attributes;
    }

    public Map<String, List<XmlNode>> getChildren()
    {
        return children;
    }

    public void addChild(String _name, XmlNode _node)
    {
        CollectionUtils.addToMultiMap(_name, _node, children);
    }

    public XmlNode getChildByName(String _name) {
        List<XmlNode> nodes = children.get(_name);
        if (CollectionUtils.isNotEmpty(nodes))
            return CollectionUtils.getFirst(nodes);
        for (List<XmlNode> childNodes : children.values()) {
            for (XmlNode child : CollectionUtils.makeNotNull(childNodes)) {
                XmlNode match = child.getChildByName(_name);
                if (match != null)
                    return match;
            }
        }
        return null;
    }

    public String getChildContentByName(String _name) {
        XmlNode child = getChildByName(_name);
        return child == null ? null : child.getContent();
    }

    public XmlNode getChild(String... _path) {
        return getChild(CollectionUtils.asArrayList(_path));
    }

    public XmlNode getChild(List<String> _path)
    {
        if (CollectionUtils.isEmpty(_path))
            return this;
        XmlNode node = CollectionUtils.getFirst(children.get(CollectionUtils.getFirst(_path)));
        if (node == null)
            return null;
        return node.getChild(_path.subList(1, _path.size()));
    }

    public List<XmlNode> getChildren(String... _path) {
        return getChildren(CollectionUtils.asArrayList(_path));
    }

    public List<XmlNode> getChildren(List<String> _path)
    {
        if (CollectionUtils.size(_path) == 1)
            return CollectionUtils.makeNotNull(children.get(_path.get(0)));
        List<XmlNode> nodes = new ArrayList<XmlNode>();
        for (XmlNode node : CollectionUtils.makeNotNull(children.get(CollectionUtils.getFirst(_path))))
        {
            nodes.addAll(node.getChildren(_path.subList(1, _path.size())));
        }
        return nodes;
    }


    public XmlNode getChild(List<String> _path, String _attributeName, String _attributeValue)
    {
        for (XmlNode node : getChildren(_path))
        {
            if (NullUtils.isEqual(node.getAttributes().get(_attributeName), _attributeValue))
                return node;
        }
        return null;
    }

    public String getChildContent(List<String> _path, String _attributeName, String _attributeValue)
    {
        XmlNode node = getChild(_path, _attributeName, _attributeValue);
        if (node == null)
            return null;
        return node.getContent();
    }

    public String getChildAttribute(List<String> _path, String _attributeName)
    {
        XmlNode child = getChild(_path);
        if (child == null)
            return null;
        return child.getAttributes().get(_attributeName);
    }


    public String getChildContent(List<String> _path)
    {
        XmlNode node = getChild(_path);
        if (node == null)
            return null;
        return node.getContent();
    }
}

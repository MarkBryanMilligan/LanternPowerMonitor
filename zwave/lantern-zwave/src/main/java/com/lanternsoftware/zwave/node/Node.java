package com.lanternsoftware.zwave.node;

import com.lanternsoftware.zwave.message.CommandClass;

import java.util.Set;

public class Node {
	private byte id;
	private Set<CommandClass> commandClasses;

	public Node() {
	}

	public Node(byte _id, Set<CommandClass> _commandClasses) {
		id = _id;
		commandClasses = _commandClasses;
	}

	public byte getId() {
		return id;
	}

	public void setId(byte _id) {
		id = _id;
	}

	public Set<CommandClass> getCommandClasses() {
		return commandClasses;
	}

	public void setCommandClasses(Set<CommandClass> _commandClasses) {
		commandClasses = _commandClasses;
	}
}

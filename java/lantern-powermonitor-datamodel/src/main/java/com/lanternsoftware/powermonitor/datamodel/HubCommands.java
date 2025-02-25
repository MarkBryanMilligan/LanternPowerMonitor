package com.lanternsoftware.powermonitor.datamodel;

import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.List;

@DBSerializable
public class HubCommands {
	private List<HubCommand> commands;

	public HubCommands() {
	}

	public HubCommands(List<HubCommand> _commands) {
		commands = _commands;
	}

	public List<HubCommand> getCommands() {
		return commands;
	}

	public void setCommands(List<HubCommand> _commands) {
		commands = _commands;
	}
}

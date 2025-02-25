package com.lanternsoftware.zwave.message;

import com.lanternsoftware.util.CollectionUtils;

import java.util.Arrays;
import java.util.Map;

public enum ControllerMessageType {
	None((byte)0x0,"None"),
	SerialApiGetInitData((byte)0x02,"SerialApiGetInitData"),									// Request initial information about devices in network
	SerialApiApplicationNodeInfo((byte)0x03,"SerialApiApplicationNodeInfo"),					// Set controller node information
	ApplicationCommandHandler((byte)0x04,"ApplicationCommandHandler"),						// Handle application command
	GetControllerCapabilities((byte)0x05,"GetControllerCapabilities"),						// Request controller capabilities (primary role, SUC/SIS availability)
	SerialApiSetTimeouts((byte)0x06,"SerialApiSetTimeouts"),									// Set Serial API timeouts
	GetCapabilities((byte)0x07,"GetCapabilities"),							                // Request Serial API capabilities from the controller
	SerialApiSoftReset((byte)0x08,"SerialApiSoftReset"),										// Soft reset. Restarts Z-Wave chip
	RfReceiveMode((byte)0x10,"RfReceiveMode"),												// Power down the RF section of the stick
	SetSleepMode((byte)0x11,"SetSleepMode"),													// Set the CPU into sleep mode
	SendNodeInfo((byte)0x12,"SendNodeInfo"),													// Send Node Information Frame of the stick
	SendData((byte)0x13,"SendData"),															// Send data.
	SendDataMulti((byte)0x14, "SendDataMulti"),
	GetVersion((byte)0x15,"GetVersion"),														// Request controller hardware version
	SendDataAbort((byte)0x16,"SendDataAbort"),												// Abort Send data.
	RfPowerLevelSet((byte)0x17,"RfPowerLevelSet"),											// Set RF Power level
	SendDataMeta((byte)0x18, "SendDataMeta"),
	GetRandom((byte)0x1c,"GetRandom"),														// ???
	MemoryGetId((byte)0x20,"MemoryGetId"),													// ???
	MemoryGetByte((byte)0x21,"MemoryGetByte"),												// Get a byte of memory.
	MemoryPutByte((byte)0x22, "MemoryPutByte"),
	ReadMemory((byte)0x23,"ReadMemory"),														// Read memory.
	WriteMemory((byte)0x24, "WriteMemory"),
	SetLearnNodeState((byte)0x40,"SetLearnNodeState"),    									// ???
	IdentifyNode((byte)0x41,"IdentifyNode"),    												// Get protocol info (baud rate, listening, etc.) for a given node
	SetDefault((byte)0x42,"SetDefault"),    													// Reset controller and node info to default (original) values
	NewController((byte)0x43,"NewController"),												// ???
	ReplicationCommandComplete((byte)0x44,"ReplicationCommandComplete"),						// Replication send data complete
	ReplicationSendData((byte)0x45,"ReplicationSendData"),									// Replication send data
	AssignReturnRoute((byte)0x46,"AssignReturnRoute"),										// Assign a return route from the specified node to the controller
	DeleteReturnRoute((byte)0x47,"DeleteReturnRoute"),										// Delete all return routes from the specified node
	RequestNodeNeighborUpdate((byte)0x48,"RequestNodeNeighborUpdate"),						// Ask the specified node to update its neighbors (then read them from the controller)
	ApplicationUpdate((byte)0x49,"ApplicationUpdate"),										// Get a list of supported (and controller) command classes
	AddNodeToNetwork((byte)0x4a,"AddNodeToNetwork"),											// Control the addnode (or addcontroller) process...start, stop, etc.
	RemoveNodeFromNetwork((byte)0x4b,"RemoveNodeFromNetwork"),								// Control the removenode (or removecontroller) process...start, stop, etc.
	CreateNewPrimary((byte)0x4c,"CreateNewPrimary"),											// Control the createnewprimary process...start, stop, etc.
	ControllerChange((byte)0x4d,"ControllerChange"),    										// Control the transferprimary process...start, stop, etc.
	SetLearnMode((byte)0x50,"SetLearnMode"),													// Put a controller into learn mode for replication/ receipt of configuration info
	AssignSucReturnRoute((byte)0x51,"AssignSucReturnRoute"),									// Assign a return route to the SUC
	EnableSuc((byte)0x52,"EnableSuc"),														// Make a controller a Static Update Controller
	RequestNetworkUpdate((byte)0x53,"RequestNetworkUpdate"),									// Network update for a SUC(?)
	SetSucNodeID((byte)0x54,"SetSucNodeID"),													// Identify a Static Update Controller node id
	DeleteSUCReturnRoute((byte)0x55,"DeleteSUCReturnRoute"),									// Remove return routes to the SUC
	GetSucNodeId((byte)0x56,"GetSucNodeId"),													// Try to retrieve a Static Update Controller node id (zero if no SUC present)
	SendSucId((byte)0x57, "SendSucId"),
	RequestNodeNeighborUpdateOptions((byte)0x5a,"RequestNodeNeighborUpdateOptions"),   		// Allow options for request node neighbor update
	RequestNodeInfo((byte)0x60,"RequestNodeInfo"),											// Get info (supported command classes) for the specified node
	RemoveFailedNodeID((byte)0x61,"RemoveFailedNodeID"),										// Mark a specified node id as failed
	IsFailedNodeID((byte)0x62,"IsFailedNodeID"),												// Check to see if a specified node has failed
	ReplaceFailedNode((byte)0x63,"ReplaceFailedNode"),										// Remove a failed node from the controller's list (?)
	GetRoutingInfo((byte)0x80,"GetRoutingInfo"),												// Get a specified node's neighbor information from the controller
	LockRoute((byte)0x90, "LockRoute"),
	SerialApiSlaveNodeInfo((byte)0xA0,"SerialApiSlaveNodeInfo"),								// Set application virtual slave node information
	ApplicationSlaveCommandHandler((byte)0xA1,"ApplicationSlaveCommandHandler"),				// Slave command handler
	SendSlaveNodeInfo((byte)0xA2,"ApplicationSlaveCommandHandler"),							// Send a slave node information frame
	SendSlaveData((byte)0xA3,"SendSlaveData"),												// Send data from slave
	SetSlaveLearnMode((byte)0xA4,"SetSlaveLearnMode"),										// Enter slave learn mode
	GetVirtualNodes((byte)0xA5,"GetVirtualNodes"),											// Return all virtual nodes
	IsVirtualNode((byte)0xA6,"IsVirtualNode"),												// Virtual node test
	WatchDogEnable((byte)0xB6, "WatchDogEnable"),
	WatchDogDisable((byte)0xB7, "WatchDogDisable"),
	WatchDogKick((byte)0xB6, "WatchDogKick"),
	RfPowerLevelGet((byte)0xBA,"RfPowerLevelSet"),											// Get RF Power level
	GetLibraryType((byte)0xBD, "GetLibraryType"),											// Gets the type of ZWave library on the stick
	SendTestFrame((byte)0xBE, "SendTestFrame"),												// Send a test frame to a node
	GetProtocolStatus((byte)0xBF, "GetProtocolStatus"),
	SetPromiscuousMode((byte)0xD0,"SetPromiscuousMode"), 									// Set controller into promiscuous mode to listen to all frames
	PromiscuousApplicationCommandHandler((byte)0xD1,"PromiscuousApplicationCommandHandler"),
	ALL((byte)0xFF, null);

	public final byte data;
	public final String label;

	ControllerMessageType(byte _data, String _label) {
		data = _data;
		label = _label;
	}

	private static Map<Byte, ControllerMessageType> types = CollectionUtils.transformToMap(Arrays.asList(values()), _type->_type.data);
	public static ControllerMessageType fromByte(byte _data) {
		ControllerMessageType type = types.get(_data);
		return type == null ? ControllerMessageType.None : type;
	}
}

package com.lanternsoftware.zwave.security;

public interface SecurityListener {
	void onStateChanged(int nodeId, boolean _open);
}

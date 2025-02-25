package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.zwave.message.CommandClass;
import com.lanternsoftware.zwave.message.ControllerMessageType;
import com.lanternsoftware.zwave.message.RequestMessage;

public class SendDataRequest extends RequestMessage {
	public enum TransmissionState {
		COMPLETE_OK((byte) 0x00, "Transmission complete and ACK received"),
		COMPLETE_NO_ACK((byte) 0x01, "Transmission complete, no ACK received"),
		COMPLETE_FAIL((byte) 0x02, "Transmission failed"),
		COMPLETE_NOT_IDLE((byte) 0x03, "Transmission failed, network busy"),
		COMPLETE_NOROUTE((byte) 0x04, "Tranmission complete, no return route");

		public byte key;
		public String label;

		TransmissionState(byte _key, String _label) {
			key = _key;
			label = _label;
		}

		public static TransmissionState fromKey(byte _key) {
			for (TransmissionState state : values()) {
				if (state.key == _key)
					return state;
			}
			return COMPLETE_FAIL;
		}
	}

	private TransmissionState state;
	private byte callbackId;

	public SendDataRequest() {
		super(ControllerMessageType.SendData, CommandClass.NO_OPERATION, (byte) 0);
	}

	@Override
	public void fromPayload(byte[] _payload) {
		if (CollectionUtils.length(_payload) > 5) {
			callbackId = _payload[4];
			state = TransmissionState.fromKey(_payload[5]);
		}
	}

	public TransmissionState getState() {
		return state;
	}

	public byte getCallbackId() {
		return callbackId;
	}

	@Override
	public String describe() {
		return name() + " callbackId: " + callbackId + " state: " + state.name();
	}
}

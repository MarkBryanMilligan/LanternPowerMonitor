package com.lanternsoftware.uirt;

import com.lanternsoftware.uirt.model.UIRTConfig;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public class UsbUirt {
	private static String off = "0000 006D 0000 00BF 0080 0040 0010 0030 0010 0030 0010 0010 0010 0010 0010 0010 0010 0030 0010 0010 0010 0010 0010 0030 0010 0030 0010 0010 0010 0030 0010 0010 0010 0010 0010 0030 0010 0030 0010 0010 0010 0030 0010 0030 0010 0010 0010 0010 0010 0030 0010 0010 0010 0010 0010 0030 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0030 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0030 0010 0010 0010 0010 0010 0030 0010 0010 0010 0030 0010 0010 0010 0030 0010 0010 0010 0030 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0030 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0030 0010 0030 0010 0010 0010 0030 0010 0010 0010 0010 0010 0030 0010 0010 0010 0030 0010 0010 0010 0030 0010 0030 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0030 0010 0010 0010 0010 0010 0030 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0030 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0030 0010 0030 0010 0010 0010 0010 0010 0010 0010 0010 0010 0030 0010 0010 0010 0010 0010 0030 0010 0010 0010 0010 0010 0030 0010 01AF 007F 0040 0010 0030 0010 0030 0010 0010 0010 0010 0010 0010 0010 0030 0010 0010 0010 0010 0010 0030 0010 0030 0010 0010 0010 0030 0010 0010 0010 0010 0010 0030 0010 0030 0010 0010 0010 0030 0010 0030 0010 0010 0010 0010 0010 0030 0010 0010 0010 0010 0010 0030 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0010 0F85";
	private UirtLibrary lib;
	private Pointer handle;

	public interface UirtLibrary extends Library {
		interface ReceiveProc extends StdCallLibrary.StdCallCallback {
			boolean callback(Pointer _event, Pointer _userData);
		}
		interface LearnProc extends StdCallLibrary.StdCallCallback {
			boolean callback(int progress, int _sigQuality, long carrierFreq, Pointer _userData);
		}

		Pointer UUIRTOpen();
		boolean UUIRTClose(Pointer _handle);
		boolean UUIRTGetDrvVersion(IntByReference _version);
		boolean UUIRTGetUUIRTConfig(Pointer _handle, UIRTConfig.ByReference _config);
		boolean UUIRTSetReceiveCallback(Pointer _handle, ReceiveProc _proc, Pointer _userData);
		boolean UUIRTSetRawReceiveCallback(Pointer _handle, ReceiveProc _proc, Pointer _userData);
		boolean UUIRTLearnIR(Pointer _handle, int codeFormat, PointerByReference irCode, LearnProc progressProc, Pointer _userData, IntByReference _abort, int _param1, Pointer reserved0, Pointer reserved1);
		boolean UUIRTTransmitIR(Pointer _handle, String _code, int _codeFormat, int repeatCount_, int _inactivityWaitTime, Pointer _event, Pointer reserved0, Pointer reserved1);
	}

	public void startup() {
		lib = Native.load("uuirtdrv", UirtLibrary.class, W32APIOptions.ASCII_OPTIONS);
		handle = lib.UUIRTOpen();
	}

	public void shutdown() {
		if (isStarted())
			lib.UUIRTClose(handle);
		lib = null;
	}

	public int getDriverVersion() {
		if (!isStarted())
			return 0;
		IntByReference version = new IntByReference();
		lib.UUIRTGetDrvVersion(version);
		return version.getValue();
	}

	public UIRTConfig getConfig() {
		if (!isStarted())
			return null;
		UIRTConfig.ByReference configRef = new UIRTConfig.ByReference();
		lib.UUIRTGetUUIRTConfig(handle, configRef);
		return configRef;
	}

	public void setReceiveCallback(UirtLibrary.ReceiveProc _proc) {
		if (isStarted())
			lib.UUIRTSetReceiveCallback(handle, _proc, null);
	}

	public void setRawReceiveCallback(UirtLibrary.ReceiveProc _proc) {
		if (isStarted())
			lib.UUIRTSetRawReceiveCallback(handle, _proc, null);
	}

	public String learnCode(UirtLibrary.LearnProc _proc) {
		PointerByReference codeRef = new PointerByReference();
		lib.UUIRTLearnIR(handle, 0, codeRef, _proc, null, null, 0, null, null);
		Pointer code = codeRef.getValue();
		return code == null ? null : code.getString(0);
	}

	public boolean transmitIR(boolean _pronto, int _repeatCnt, int _inactivityWaitTime, String _code) {
		PointerByReference result = new PointerByReference();
		return lib.UUIRTTransmitIR(handle, _code, _pronto?0x0010:0x0000, _repeatCnt, _inactivityWaitTime, null, null, null);
	}

	private boolean isStarted() {
		return (lib != null) && (handle != null);
	}

	public static void main(String[] args) {
		UsbUirt uirt = new UsbUirt();
		uirt.startup();
		uirt.transmitIR(true, 3, 0, off);
		uirt.shutdown();
	}
}

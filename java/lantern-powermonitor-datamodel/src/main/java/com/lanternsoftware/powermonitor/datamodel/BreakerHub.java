package com.lanternsoftware.powermonitor.datamodel;


import com.lanternsoftware.util.IIdentical;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.Objects;

@DBSerializable(autogen = false)
public class BreakerHub implements IIdentical<BreakerHub> {
	private int hub;
	private double voltageCalibrationFactor;
	private double portCalibrationFactor;
	private int phaseCnt;
	private int phaseOffsetNs;
	private int frequency;
	private String bluetoothMac;

	private int boardVersion;

	public int getHub() {
		return hub;
	}

	public void setHub(int _hub) {
		hub = _hub;
	}

	public double getRawVoltageCalibrationFactor() {
		return voltageCalibrationFactor;
	}

	public double getVoltageCalibrationFactor() {
		return voltageCalibrationFactor == 0.0?0.3445:voltageCalibrationFactor;
	}

	public void setVoltageCalibrationFactor(double _voltageCalibrationFactor) {
		voltageCalibrationFactor = _voltageCalibrationFactor;
	}

	public double getRawPortCalibrationFactor() {
		return portCalibrationFactor;
	}

	public double getPortCalibrationFactor() {
		return portCalibrationFactor == 0.0?1.20:portCalibrationFactor;
	}

	public void setPortCalibrationFactor(double _portCalibrationFactor) {
		portCalibrationFactor = _portCalibrationFactor;
	}

	public int getPhaseCnt() {
		return phaseCnt == 0 ? 2 : phaseCnt;
	}

	public void setPhaseCnt(int _phaseCnt) {
		phaseCnt = _phaseCnt;
	}

	public int getPhaseOffsetNs() {
		return phaseOffsetNs;
	}

	public void setPhaseOffsetNs(int _phaseOffsetNs) {
		phaseOffsetNs = _phaseOffsetNs;
	}

	public int getFrequency() {
		return frequency == 0 ? 60 : frequency;
	}

	public void setFrequency(int _frequency) {
		frequency = _frequency;
	}

	public String getBluetoothMac() {
		return bluetoothMac;
	}

	public void setBluetoothMac(String _bluetoothMac) {
		bluetoothMac = _bluetoothMac;
	}

	public int getBoardVersion() {
		return boardVersion == 0 ? 1 : boardVersion;
	}

	public void setBoardVersion(int _boardVersion) {
		boardVersion = _boardVersion;
	}

	@Override
	public boolean equals(Object _o) {
		if (this == _o) return true;
		if (_o == null || getClass() != _o.getClass()) return false;
		BreakerHub that = (BreakerHub) _o;
		return hub == that.hub;
	}

	@Override
	public boolean isIdentical(BreakerHub _o) {
		if (this == _o) return true;
		return hub == _o.hub && Double.compare(_o.voltageCalibrationFactor, voltageCalibrationFactor) == 0 && Double.compare(_o.portCalibrationFactor, portCalibrationFactor) == 0 && getPhaseCnt() == _o.getPhaseCnt() && getPhaseOffsetNs() == _o.getPhaseOffsetNs() && getFrequency() == _o.getFrequency() && Objects.equals(bluetoothMac, _o.bluetoothMac);
	}

	@Override
	public int hashCode() {
		return Objects.hash(hub);
	}
}

package com.lanternsoftware.datamodel.currentmonitor;


import com.lanternsoftware.util.IIdentical;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.Objects;

@DBSerializable()
public class Breaker implements IIdentical<Breaker> {
	private static final int TANDEM_BREAKER_MASK = 3072;
	private static final int SPACE_MASK = 1023;
	private static final int TANDEM_BREAKER_A_MASK = 1024;
	private static final int TANDEM_BREAKER_B_MASK = 2048;

	private int panel;
	private int space;
	private int meter;
	private int hub;
	private int port;
	private String name;
	private String description;
	private int sizeAmps;
	private double calibrationFactor;
	private double lowPassFilter;
	private BreakerPolarity polarity;
	private boolean doublePower;
	private BreakerType type;
	private boolean main;
	private transient String key;

	public Breaker() {
	}

	public Breaker(String _name, int _panel, int _space, int _hub, int _port, int _sizeAmps, double _lowPassFilter) {
		name = _name;
		panel = _panel;
		space = _space;
		hub = _hub;
		port = _port;
		sizeAmps = _sizeAmps;
		lowPassFilter = _lowPassFilter;
	}

	public int getPanel() {
		return panel;
	}

	public void setPanel(int _panel) {
		panel = _panel;
		key = null;
	}

	public int getSpace() {
		return space;
	}

	public void setSpace(int _space) {
		space = _space;
		key = null;
	}

	public int getMeter() {
		return meter;
	}

	public void setMeter(int _meter) {
		meter = _meter;
	}

	public String getSpaceDisplay() {
		return toSpaceDisplay(space);
	}

	public void setSpaceTandemA(int _space) {
		space = TANDEM_BREAKER_A_MASK | _space;
	}

	public void setSpaceTandemB(int _space) {
		space = TANDEM_BREAKER_B_MASK | _space;
	}

	public boolean isTandemBreaker() {
		return (TANDEM_BREAKER_MASK & space) != 0;
	}

	public boolean isTandemBreakerA() {
		return isTandemBreakerA(space);
	}

	public boolean isTandemBreakerB() {
		return isTandemBreakerB(space);
	}

	public String getName() {
		return name;
	}

	public void setName(String _name) {
		name = _name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String _description) {
		description = _description;
	}

	public int getHub() {
		return hub;
	}

	public void setHub(int _hub) {
		hub = _hub;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int _port) {
		port = _port;
	}

	public int getChip() {
		return portToChip(port);
	}

	public int getPin() {
		return portToPin(port);
	}

	public int getSizeAmps() {
		return sizeAmps;
	}

	public void setSizeAmps(int _sizeAmps) {
		sizeAmps = _sizeAmps;
	}

	public double getLowPassFilter() {
		return Math.abs(lowPassFilter) < 0.05 ? 1.6 : lowPassFilter;
	}

	public void setLowPassFilter(double _lowPassFilter) {
		lowPassFilter = _lowPassFilter;
	}

	public BreakerPolarity getPolarity() {
		return polarity == null ? BreakerPolarity.NORMAL : polarity;
	}

	public void setPolarity(BreakerPolarity _polarity) {
		polarity = _polarity;
	}

	public boolean isDoublePower() {
		return doublePower;
	}

	public void setDoublePower(boolean _doublePower) {
		doublePower = _doublePower;
	}

	public double getCalibrationFactor() {
		return calibrationFactor == 0.0 ? 1.0 : calibrationFactor;
	}

	public void setCalibrationFactor(double _calibrationFactor) {
		calibrationFactor = _calibrationFactor;
	}

	public BreakerType getType() {
		if (type == null) {
			if (isTandemBreaker())
				return BreakerType.SINGLE_POLE_TANDEM;
			return BreakerType.SINGLE_POLE;
		}
		return type;
	}

	public void setType(BreakerType _type) {
		type = _type;
	}

	public boolean isMain() {
		return main;
	}

	public void setMain(boolean _main) {
		main = _main;
	}

	public double getFinalCalibrationFactor() {
		return getCalibrationFactor() * getSizeAmps() / 380.0;
	}

	public String getKey() {
		if (key == null)
			key = key(panel, space);
		return key;
	}

	public int getIntKey() {
		return intKey(panel, space);
	}

	public static int intKeyToPanel(int _intKey) {
		return _intKey/10000;
	}

	public static int intKeyToSpace(int _intKey) {
		return _intKey%10000;
	}

	public static String key(int _panel, int _space) {
		return String.format("%d-%d", _panel, _space);
	}

	public static int intKey(int _panel, int _space) {
		return 10000*_panel + _space;
	}

	public static int portToChip(int _port) {
		return (_port < 9) ? 1 : 0;
	}

	public static int portToPin(int _port) {
		return (_port < 9) ? _port - 1 : _port - 8;
	}

	public static int toPort(int _chip, int _pin) {
		return (_chip == 0) ? _pin + 8 : _pin + 1;
	}

	public static boolean isTandemBreakerA(int _space) {
		return (TANDEM_BREAKER_A_MASK & _space) != 0;
	}

	public static boolean isTandemBreakerB(int _space) {
		return (TANDEM_BREAKER_B_MASK & _space) != 0;
	}

	public static int toId(int _panel, int _space) {
		return (_panel << 12) | _space;
	}

	public static int toPanel(int _id) {
		return _id >> 12;
	}

	public static int toSpace(int _id) {
		return _id & (TANDEM_BREAKER_MASK | SPACE_MASK);
	}

	public static String toSpaceDisplay(int _space) {
		if (isTandemBreakerA(_space))
			return String.format("%dA", _space & SPACE_MASK);
		if (isTandemBreakerB(_space))
			return String.format("%dB", _space & SPACE_MASK);
		return String.valueOf(_space);
	}

	public int getSpaceIndex() {
		return space & SPACE_MASK;
	}

	@Override
	public boolean equals(Object _o) {
		if (this == _o) return true;
		if (_o == null || getClass() != _o.getClass()) return false;
		Breaker breaker = (Breaker) _o;
		return panel == breaker.panel && space == breaker.space;
	}

	@Override
	public boolean isIdentical(Breaker _o) {
		if (this == _o) return true;
		return panel == _o.panel && space == _o.space && meter == _o.meter && hub == _o.hub && port == _o.port && sizeAmps == _o.sizeAmps && Double.compare(_o.calibrationFactor, calibrationFactor) == 0 && Double.compare(_o.lowPassFilter, lowPassFilter) == 0 && doublePower == _o.doublePower && Objects.equals(name, _o.name) && Objects.equals(description, _o.description) && polarity == _o.polarity && type == _o.type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(panel, space);
	}
}

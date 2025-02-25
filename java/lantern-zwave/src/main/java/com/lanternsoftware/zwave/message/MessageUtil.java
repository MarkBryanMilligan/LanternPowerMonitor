package com.lanternsoftware.zwave.message;

import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class MessageUtil {
	private static final int SIZE_MASK = 0x07;
	private static final int SCALE_MASK = 0x18;
	private static final int SCALE_SHIFT = 0x03;
	private static final int PRECISION_MASK = 0xe0;
	private static final int PRECISION_SHIFT = 0x05;

	public static double getTemperatureCelsius(byte[] _payload, int _offset) {
		int size = _payload[_offset] & SIZE_MASK;
		int scale = (_payload[_offset] & SCALE_MASK) >> SCALE_SHIFT;
		int precision = (_payload[_offset] & PRECISION_MASK) >> PRECISION_SHIFT;

		if ((size+_offset) >= _payload.length)
			return 0.0;

		int value = 0;
		for (int i = 0; i < size; ++i) {
			value <<= 8;
			value |= _payload[_offset + i + 1] & 0xFF;
		}

		BigDecimal result;
		if ((_payload[_offset + 1] & 0x80) == 0x80) {
			if (size == 1)
				value |= 0xffffff00;
			else if (size == 2)
				value |= 0xffff0000;
		}
		result = BigDecimal.valueOf(value);
		BigDecimal divisor = BigDecimal.valueOf(Math.pow(10, precision));
		double temp = result.divide(divisor, RoundingMode.HALF_EVEN).doubleValue();
		return (scale == 1) ? (temp-32)/1.8 : temp;
	}
}

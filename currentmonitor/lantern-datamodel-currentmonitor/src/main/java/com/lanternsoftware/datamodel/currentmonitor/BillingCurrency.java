package com.lanternsoftware.datamodel.currentmonitor;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum BillingCurrency {
	DOLLAR("$"),
	EURO("€"),
	POUND_STERLING("£");

	public final String symbol;

	BillingCurrency(String _symbol) {
		symbol = _symbol;
	}

	public String format(double _value) {
		return format(BigDecimal.valueOf(_value));
	}

	public String format(BigDecimal _value) {
		if (_value.compareTo(BigDecimal.ZERO) < 0)
			return "-" + symbol + _value.abs().setScale(2, RoundingMode.HALF_EVEN);
		return symbol + _value.setScale(2, RoundingMode.HALF_EVEN);
	}

	public String formatValue(double _value) {
		return formatValue(BigDecimal.valueOf(_value));
	}

	public String formatValue(BigDecimal _value) {
		if (_value.compareTo(BigDecimal.ZERO) < 0)
			return "-" + _value.abs().setScale(2, RoundingMode.HALF_EVEN);
		return _value.setScale(2, RoundingMode.HALF_EVEN).toString();
	}
}

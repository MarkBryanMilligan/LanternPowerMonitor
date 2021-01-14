package com.lanternsoftware.currentmonitor;

import java.util.Date;

public interface CurrentListener {
	void onCurrentEvent(int _chip, int _pin, double _currentAmps, Date _start);
}

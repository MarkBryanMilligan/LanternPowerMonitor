package com.lanternsoftware.zwave;

import com.lanternsoftware.datamodel.zwave.Switch;
import com.lanternsoftware.datamodel.zwave.SwitchType;
import com.lanternsoftware.util.concurrency.ConcurrencyUtils;
import com.lanternsoftware.zwave.security.SecurityController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSecurity {
	protected static final Logger LOG = LoggerFactory.getLogger(TestSecurity.class);

	public static void main(String[] args) {
		SecurityController c = new SecurityController();
		Switch sw = new Switch("Garage", "Door 1", 1000, true, false, null, 0);
		sw.setGpioPin(7);
		sw.setType(SwitchType.SECURITY);
		c.listen(sw, (nodeId, _open) -> LOG.error("Door event, now " + (_open ? "OPEN" : "CLOSED")));
		ConcurrencyUtils.sleep(60000);
		c.shutdown();
	}
}

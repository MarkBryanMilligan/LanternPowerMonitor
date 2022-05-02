package com.lanternsoftware.rules.actions;

import com.lanternsoftware.datamodel.rules.Alert;
import com.lanternsoftware.datamodel.rules.FcmDevice;
import com.lanternsoftware.datamodel.rules.Rule;
import com.lanternsoftware.rules.RulesEngine;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.external.LanternFiles;
import com.lanternsoftware.util.cloudservices.google.FirebaseHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AbstractAlertAction implements ActionImpl {
	protected static final Logger logger = LoggerFactory.getLogger(AbstractAlertAction.class);
	protected static final FirebaseHelper firebaseHelper = new FirebaseHelper(LanternFiles.CONFIG_PATH + "google_account_key.json");

	protected void sendAlert(Rule _rule, Alert _alert) {
		List<FcmDevice> devices = RulesEngine.instance().dao().getFcmDevicesForAccount(_rule.getAccountId());
		if (devices.isEmpty())
			return;
		for (FcmDevice device : devices) {
			firebaseHelper.sendMessage(device.getToken(), new DaoEntity("payload", DaoSerializer.toBase64ZipBson(_alert)).and("payloadClass", Alert.class.getCanonicalName()));
		}
	}
}

package com.lanternsoftware.rules.actions;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.lanternsoftware.datamodel.rules.Alert;
import com.lanternsoftware.datamodel.rules.FcmDevice;
import com.lanternsoftware.datamodel.rules.Rule;
import com.lanternsoftware.rules.RulesEngine;
import com.lanternsoftware.util.external.LanternFiles;
import com.lanternsoftware.util.dao.DaoSerializer;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.List;

public abstract class AbstractAlertAction implements ActionImpl {
	protected static final Logger logger = LoggerFactory.getLogger(AbstractAlertAction.class);
	protected static final FirebaseMessaging messaging;
	static {
		FirebaseMessaging m = null;
		try {
			FileInputStream is = new FileInputStream(LanternFiles.CONFIG_PATH + "google_account_key.json");
			FirebaseOptions options = FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(is)).build();
			m = FirebaseMessaging.getInstance(FirebaseApp.initializeApp(options));
			IOUtils.closeQuietly(is);
		}
		catch (Exception _e) {
			logger.error("Failed to load google credentials", _e);
		}
		messaging = m;
	}

	protected void sendAlert(Rule _rule, Alert _alert) {
		List<FcmDevice> devices = RulesEngine.instance().dao().getFcmDevicesForAccount(_rule.getAccountId());
		if (devices.isEmpty())
			return;
		for (FcmDevice device : devices) {
			Message msg = Message.builder().setToken(device.getToken()).putData("payload", DaoSerializer.toBase64ZipBson(_alert)).putData("payloadClass", Alert.class.getCanonicalName()).setAndroidConfig(AndroidConfig.builder().setPriority(AndroidConfig.Priority.HIGH).setDirectBootOk(true).build()).build();
			try {
				messaging.send(msg);
			} catch (Exception _e) {
				if (_e.getMessage().contains("not found")) {
					RulesEngine.instance().dao().removeFcmDevice(device.getId());
				}
				logger.error("Failed to send message to account {}, device {}", _rule.getAccountId(), device.getName(), _e);
			}
		}
	}
}

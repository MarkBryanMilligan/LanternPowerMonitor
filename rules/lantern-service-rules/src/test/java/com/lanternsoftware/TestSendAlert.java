package com.lanternsoftware;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.lanternsoftware.dataaccess.rules.MongoRulesDataAccess;
import com.lanternsoftware.dataaccess.rules.RulesDataAccess;
import com.lanternsoftware.datamodel.rules.Alert;
import com.lanternsoftware.datamodel.rules.FcmDevice;
import com.lanternsoftware.util.LanternFiles;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.mongo.MongoConfig;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;

public class TestSendAlert {
	public static void main(String[] args) {
		RulesDataAccess dao = new MongoRulesDataAccess(MongoConfig.fromDisk(LanternFiles.OPS_PATH + "mongo.cfg"));
		for (FcmDevice d : dao.getFcmDevicesForAccount(100)) {
			Alert alert = new Alert();
			alert.setMessage("Test Alert");
			Message msg = Message.builder().setToken(d.getToken()).putData("payload", DaoSerializer.toBase64ZipBson(alert)).putData("payloadClass", Alert.class.getCanonicalName()).build();
			try {
				FileInputStream is = new FileInputStream("d:\\zwave\\firebase\\account_key.json");
				FirebaseOptions options = FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(is)).build();
				FirebaseMessaging.getInstance(FirebaseApp.initializeApp(options)).send(msg);
				IOUtils.closeQuietly(is);
			} catch (Exception _e) {
				_e.printStackTrace();
			}
		}
	}
}

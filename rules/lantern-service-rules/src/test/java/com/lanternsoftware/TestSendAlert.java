package com.lanternsoftware;

import com.lanternsoftware.dataaccess.rules.MongoRulesDataAccess;
import com.lanternsoftware.dataaccess.rules.RulesDataAccess;
import com.lanternsoftware.datamodel.rules.Alert;
import com.lanternsoftware.datamodel.rules.FcmDevice;
import com.lanternsoftware.util.cloudservices.google.FirebaseHelper;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.mongo.MongoConfig;
import com.lanternsoftware.util.external.LanternFiles;

public class TestSendAlert {
	public static void main(String[] args) {
		RulesDataAccess dao = new MongoRulesDataAccess(MongoConfig.fromDisk(LanternFiles.CONFIG_PATH + "mongo.cfg"));
		for (FcmDevice d : dao.getFcmDevicesForAccount(100)) {
			if (!d.getName().contains("Sony"))
				continue;
			Alert alert = new Alert();
			alert.setMessage("Garage Door 1 is still open");
			new FirebaseHelper(LanternFiles.CONFIG_PATH + "google_sso.json").sendMessage(d.getToken(), new DaoEntity("payload", DaoSerializer.toBase64ZipBson(alert)).and("payloadClass", Alert.class.getCanonicalName()));
		}
	}
}

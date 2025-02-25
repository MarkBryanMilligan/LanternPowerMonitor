package com.lanternsoftware.powermonitor;

import com.lanternsoftware.powermonitor.datamodel.EmailCredentials;
import com.lanternsoftware.powermonitor.datamodel.EmailProvider;
import com.lanternsoftware.util.ResourceLoader;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.cloudservices.google.FirebaseCredentials;

public class CreateEmailCredentials {
	public static void main(String[] args) {
		EmailCredentials c = new EmailCredentials();
		c.setEmailFrom("mark.milligan@lanternsoftware.com");
		c.setServerUrlBase("https://lanternpowermonitor.com/powermonitor/");
		c.setProvider(EmailProvider.MAILJET);
		c.setApiKey("<redacted>");
		c.setApiSecret("<redacted>");
		ResourceLoader.writeFile("d:\\zwave\\email.json", DaoSerializer.toJson(c));
		FirebaseCredentials cd = new FirebaseCredentials();
	}
}

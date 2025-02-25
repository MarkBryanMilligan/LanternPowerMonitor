package com.lanternsoftware.powermonitor.email;

import com.lanternsoftware.powermonitor.datamodel.EmailCredentials;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailJetProvider implements IEmailProvider {
	protected static final Logger LOG = LoggerFactory.getLogger(MailJetProvider.class);

	@Override
	public int sendTextEmail(EmailCredentials _credentials, String _to, String _subject, String _message) {
		MailjetClient client;
		MailjetRequest request;
		MailjetResponse response;
		client = new MailjetClient(ClientOptions.builder().apiKey(_credentials.getApiKey()).apiSecretKey(_credentials.getApiSecret()).build());
		request = new MailjetRequest(Emailv31.resource).property(Emailv31.MESSAGES, new JSONArray().put(new JSONObject()
				.put(Emailv31.Message.FROM, new JSONObject().put("Email", _credentials.getEmailFrom()).put("Name", "Lantern Power Monitor"))
				.put(Emailv31.Message.TO, new JSONArray().put(new JSONObject().put("Email", _to)))
				.put(Emailv31.Message.SUBJECT, _subject)
				.put(Emailv31.Message.TEXTPART, _message)));
		try {
			response = client.post(request);
			return response.getStatus();
		} catch (Exception _e) {
			LOG.error("Failed to send email", _e);
			return 500;
		}
	}
}

package com.lanternsoftware.currentmonitor.email;

import com.lanternsoftware.datamodel.currentmonitor.EmailCredentials;

public class SendGridProvider implements IEmailProvider {
	@Override
	public int sendTextEmail(EmailCredentials _credentials, String _to, String _subject, String _message) {
/*		Email to = new Email(email);
		Content content = new Content("text/plain", "Reset your password using this link:\nhttps://lanternpowermonitor.com/currentmonitor/resetPassword/" + key);
		Mail mail = new Mail(from, subject, to, content);
		SendGrid sg = new SendGrid(api_key);
		Request request = new Request();
		try {
			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");
			request.setBody(mail.build());
			Response response = sg.api(request);
			LOG.info("Password reset email status: {}\nfrom: {}\nto: {}\nkey: {}\nhost: {}", response.getStatusCode(), from.getEmail(), to.getEmail(), api_key, sg.getHost());
			zipBsonResponse(_resp, new DaoEntity("success", response.getStatusCode() == 200));
		} catch (IOException ex) {
			LOG.error("Failed to send password reset email", ex);
			_resp.setStatus(500);
		}*/
		return 500;
	}
}

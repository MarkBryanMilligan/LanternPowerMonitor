package com.lanternsoftware.powermonitor.email;

import com.lanternsoftware.powermonitor.datamodel.EmailCredentials;

public interface IEmailProvider {
	int sendTextEmail(EmailCredentials _credentials, String _to, String _subject, String _message);
}

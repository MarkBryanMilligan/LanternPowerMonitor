package com.lanternsoftware.currentmonitor.email;

import com.lanternsoftware.datamodel.currentmonitor.EmailCredentials;

public interface IEmailProvider {
	int sendTextEmail(EmailCredentials _credentials, String _to, String _subject, String _message);
}

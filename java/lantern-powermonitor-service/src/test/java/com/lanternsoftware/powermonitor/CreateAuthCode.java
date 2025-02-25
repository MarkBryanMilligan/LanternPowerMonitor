package com.lanternsoftware.powermonitor;

import com.lanternsoftware.util.external.LanternFiles;
import com.lanternsoftware.util.ResourceLoader;
import com.lanternsoftware.util.cryptography.AESTool;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.auth.AuthCode;
import org.mindrot.jbcrypt.BCrypt;

public class CreateAuthCode {
	private static final AESTool aes = new AESTool(ResourceLoader.loadFile(LanternFiles.CONFIG_PATH + "authKey.dat"));

	public static void main(String[] args) {
		System.out.println(aes.encryptToBase64(DaoSerializer.toZipBson(new AuthCode(906, null))));

		System.out.println(BCrypt.hashpw("Jom3bang8", BCrypt.gensalt(11)));
	}
}

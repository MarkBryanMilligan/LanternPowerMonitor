package com.lanternsoftware.currentmonitor;

import com.lanternsoftware.util.external.LanternFiles;
import com.lanternsoftware.util.ResourceLoader;
import com.lanternsoftware.util.cryptography.AESTool;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.auth.AuthCode;

public class CreateAuthCode {
	private static final AESTool aes = new AESTool(ResourceLoader.loadFile(LanternFiles.CONFIG_PATH + "authKey.dat"));

	public static void main(String[] args) {
		System.out.println(aes.encryptToBase64(DaoSerializer.toZipBson(new AuthCode(100, null))));
	}
}

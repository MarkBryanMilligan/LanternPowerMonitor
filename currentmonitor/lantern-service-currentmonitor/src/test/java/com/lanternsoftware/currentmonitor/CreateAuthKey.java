package com.lanternsoftware.currentmonitor;

import com.lanternsoftware.util.LanternFiles;
import com.lanternsoftware.util.ResourceLoader;
import com.lanternsoftware.util.cryptography.AESTool;

public class CreateAuthKey {
	public static void main(String[] args) {
		ResourceLoader.writeFile(LanternFiles.OPS_PATH + "authKey.dat", AESTool.generateRandomSecretKey().getEncoded());
	}
}

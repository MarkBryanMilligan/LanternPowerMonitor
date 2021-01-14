package com.lanternsoftware.datamodel.currentmonitor;

import com.lanternsoftware.util.cryptography.AESTool;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class HubConfigService {
	public static final UUIDFormatter uuidFormat = new UUIDFormatter("c5650001-d50f-49af-b906-cada0dc17937");
	private static final AESTool aes = new AESTool(37320708309265127L,-8068168662055796771L,-4867793276337148572L,4425609941731230765L);
	private static final UUID serviceUUID = uuidFormat.format(1);

	public HubConfigService() {
	}

	public static UUID getServiceUUID() {
		return serviceUUID;
	}

	public List<HubConfigCharacteristic> getCharacteristics() {
		return Arrays.asList(HubConfigCharacteristic.values());
	}

	public static byte[] encryptWifiCreds(String _ssid, String _password) {
		DaoEntity creds = new DaoEntity("ssid", _ssid).and("pwd", _password);
		return aes.encrypt(DaoSerializer.toZipBson(creds));
	}

	public static String decryptWifiSSID(byte[] _payload) {
		return DaoSerializer.getString(DaoSerializer.fromZipBson(aes.decrypt(_payload)), "ssid");
	}

	public static String decryptWifiPassword(byte[] _payload) {
		return DaoSerializer.getString(DaoSerializer.fromZipBson(aes.decrypt(_payload)), "pwd");
	}
}

package com.lanternsoftware.util.cryptography;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.lanternsoftware.util.LanternFiles;
import com.lanternsoftware.util.ResourceLoader;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lanternsoftware.util.CollectionUtils;

public class AESTool {
	private static final Logger LOG = LoggerFactory.getLogger(AESTool.class);

	private final SecretKey key;
	private final static SecureRandom rng = rng();
	private final byte[] iv;

	private static SecureRandom rng() {
		try {
			SecureRandom rng = SecureRandom.getInstance("SHA1PRNG");
			rng.generateSeed(16);
			return rng;
		} catch (NoSuchAlgorithmException e) {
			LOG.error("Failed to initialize SecureRandom with SHA1PRNG", e);
			return null;
		}
	}

	public static byte[] randomIV() {
		return rng.generateSeed(16);
	}

	/**
	 * @return a randomly generated AES secret key
	 */
	public static SecretKey generateRandomSecretKey() {
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			KeySpec spec = new PBEKeySpec(Base64.encodeBase64String(new SecureRandom().generateSeed(32)).toCharArray(), new SecureRandom().generateSeed(32), 65536, 256);
			SecretKey key = factory.generateSecret(spec);
			return new SecretKeySpec(key.getEncoded(), "AES");
		} catch (Exception e) {
			LOG.error("Failed to generate a random AES secret key", e);
			return null;
		}
	}

	public static void printRandomSecretKey() {
		SecretKey key = generateRandomSecretKey();
		byte[] btKey = key.getEncoded();
		StringBuilder builder = null;
		for (long lValue : toLongs(btKey)) {
			if (builder == null)
				builder = new StringBuilder("new AESTool(");
			else
				builder.append(",");
			builder.append(lValue);
			builder.append("L");
		}
		builder.append(");");
		System.out.println(builder.toString());
	}

	public static AESTool authTool() {
		return new AESTool(ResourceLoader.loadFile(LanternFiles.OPS_PATH + "authKey.dat"));
	}

	/**
	 * @param _btKey the encoded form of a {@link SecretKey} object.  See the {@link SecretKey#getEncoded()} method.
	 */
	public AESTool(byte[] _btKey) {
		this(new SecretKeySpec(_btKey, "AES"));
	}

	/**
	 * @param _btKey the encoded form of a {@link SecretKey} object.  See the {@link SecretKey#getEncoded()} method.
	 * @param _iv    the initialization vector to use.  If this is set, every call of encrypt for a given input will produce the same output.  If null is passed, every call of encrypt for a given input will generate a random IV and the output will be different each time (recommended).
	 */
	public AESTool(byte[] _btKey, byte[] _iv) {
		this(new SecretKeySpec(_btKey, "AES"), _iv);
	}

	/**
	 * @param _arrKey the encoded form of a {@link SecretKey} object converted to an array of long values using the {@link AESTool#toLongs(byte[])} method.  See the {@link SecretKey#getEncoded()} method.
	 */
	public AESTool(long... _arrKey) {
		this(new SecretKeySpec(toByteArray(_arrKey), "AES"));
	}

	/**
	 * @param _arrKey the encoded form of a {@link SecretKey} object converted to an array of long values using the {@link AESTool#toLongs(byte[])} method.  See the {@link SecretKey#getEncoded()} method.
	 * @param _iv     the initialization vector to use.  If this is set, every call of encrypt for a given input will produce the same output.  If null is passed, every call of encrypt for a given input will generate a random IV and the output will be different each time (recommended).
	 */
	public AESTool(byte[] _iv, long... _arrKey) {
		this(new SecretKeySpec(toByteArray(_arrKey), "AES"), _iv);
	}

	public AESTool(SecretKey _key) {
		this(_key, null);
	}

	public AESTool(SecretKey _key, byte[] _iv) {
		key = _key;
		if ((_iv != null) && (_iv.length != 16))
			throw new RuntimeException("Initialization Vector must be null or exactly 16 bytes in length");
		iv = _iv;
	}

	/**
	 * @param _data a string to be encrypted with this tool's secret key
	 * @return the encrypted data as a base64 encoded string
	 */
	public String encryptToBase64(String _data) {
		return encryptToBase64(toByteArray(_data));
	}

	/**
	 * @param _btData the binary data to be encrypted with this tool's secret key
	 * @return the encrypted data as a base64 encoded string
	 */
	public String encryptToBase64(byte[] _btData) {
		if (_btData == null)
			return null;
		return Base64.encodeBase64String(encrypt(_btData));
	}

	/**
	 * @param _data a string to be encrypted with this tool's secret key
	 * @return the encrypted data as a url safe base64 encoded string
	 */
	public String encryptToUrlSafeBase64(String _data) {
		return encryptToUrlSafeBase64(toByteArray(_data));
	}

	/**
	 * @param _btData the binary data to be encrypted with this tool's secret key
	 * @return the encrypted data as a url safe base64 encoded string
	 */
	public String encryptToUrlSafeBase64(byte[] _btData) {
		if (_btData == null)
			return null;
		return Base64.encodeBase64URLSafeString(encrypt(_btData));
	}

	/**
	 * @param _data a string to be encrypted with this tool's secret key
	 * @return the encrypted data in binary form
	 */
	public byte[] encrypt(String _data) {
		return encrypt(toByteArray(_data));
	}

	/**
	 * @param _btData the binary data to be encrypted with this tool's secret key
	 * @return the encrypted data in binary form
	 */
	public byte[] encrypt(byte[] _btData) {
		if (_btData == null)
			return null;
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			byte[] btIV = (iv != null) ? iv : randomIV();
			cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(btIV));
			if (iv != null)
				return cipher.doFinal(_btData);
			else {
				byte[] btSalt = rng.generateSeed(16);
				return CollectionUtils.merge(btIV, cipher.doFinal(CollectionUtils.merge(btSalt, _btData)));
			}
		} catch (Exception e) {
			LOG.error("Failed to encrypt data", e);
			return null;
		}
	}

	/**
	 * @param _base64 the base64 encoded representation of the aes encrypted byte array to be decrypted with this tool's
	 *                secret key
	 * @return the decrypted byte array transformed to a string.
	 */
	public String decryptFromBase64ToString(String _base64) {
		return toString(decryptFromBase64(_base64));
	}

	/**
	 * @param _base64 the base64 encoded representation of the aes encrypted byte array to be decrypted with this tool's
	 *                secret key
	 * @return the decrypted byte array
	 */
	public byte[] decryptFromBase64(String _base64) {
		return _base64 == null ? null : decrypt(Base64.decodeBase64(_base64));
	}

	/**
	 * @param _btData the encrypted byte array to be decrypted with this tool's secret key
	 * @return the decrypted byte array transformed to a string
	 */
	public String decryptToString(byte[] _btData) {
		return toString(decrypt(_btData));
	}

	/**
	 * @param _btData the encrypted byte array to be decrypted with this tool's secret key
	 * @return the decrypted byte array
	 */
	public byte[] decrypt(byte[] _btData) {
		if (_btData == null)
			return null;
		try {
			Cipher decipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			if (iv == null) {
				decipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(Arrays.copyOfRange(_btData, 0, 16)));
				byte[] btData = decipher.doFinal(Arrays.copyOfRange(_btData, 16, _btData.length));
				return Arrays.copyOfRange(btData, 16, btData.length);
			} else {
				decipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
				return decipher.doFinal(_btData);
			}
		} catch (Exception e) {
			LOG.error("Failed to decrypt data", e);
			return null;
		}
	}

	/**
	 * @param _btData a byte array to convert to an array of longs
	 * @return the array of long values that contains the data from the byte array.
	 */
	public static long[] toLongs(byte[] _btData) {
		if (_btData == null)
			return null;
		long[] lData = new long[_btData.length / 8];
		LongBuffer data = ByteBuffer.wrap(_btData).order(ByteOrder.BIG_ENDIAN).asLongBuffer();
		data.get(lData);
		return lData;
	}

	/**
	 * @param _arrLongs an array of longs to convert into a byte array representing the same data
	 * @return the converted byte array
	 */
	public static byte[] toByteArray(long... _arrLongs) {
		ByteBuffer input = ByteBuffer.allocate(_arrLongs.length * 8).order(ByteOrder.BIG_ENDIAN);
		for (long lInput : _arrLongs) {
			input.putLong(lInput);
		}
		return input.array();
	}

	/**
	 * Handles and logs the missing encoding exception that will never happen.
	 *
	 * @param _btString the UTF-8 encoded representation of a string
	 * @return the String object created from the byte array
	 */
	public static String toString(byte[] _btString) {
		if (_btString == null)
			return null;
		return new String(_btString, StandardCharsets.UTF_8);
	}

	/**
	 * Handles and logs the missing encoding exception that will never happen.
	 *
	 * @param _value the string to turn into a byte array
	 * @return the UTF-8 encoded byte array representation of the string
	 */
	public static byte[] toByteArray(String _value) {
		if (_value == null)
			return null;
		return _value.getBytes(StandardCharsets.UTF_8);
	}
}

package com.lanternsoftware.util.hash;

import java.security.MessageDigest;

import com.lanternsoftware.util.NullUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lanternsoftware.util.CollectionUtils;

public abstract class AbstractHashTool {
    private final Logger LOG = LoggerFactory.getLogger(getClass());
    
    protected final MessageDigest digest;
    protected final byte[] staticSalt;
    protected final boolean prependSalt;
    protected final int iterations;
    
    public AbstractHashTool(String _algorithm, String _staticSalt, boolean _prependSalt, int _iterationCount) {
        prependSalt = _prependSalt;
        MessageDigest digestInstance = null;
        try {
            digestInstance = MessageDigest.getInstance(_algorithm);
        }
        catch (Exception e) {
            LOG.error("Failed to create digest: " + _algorithm, e);
        }
        digest = digestInstance;
        staticSalt = NullUtils.toByteArray(_staticSalt);
        iterations = Math.max(1, _iterationCount);
    }

	/**
	 * @param _value a string value to hash using the static salt and algorithm of this hash tool
	 * @return the hex encoded hashed value
	 */
    public synchronized String hashHex(String _value) {
        return hashHex(_value, null);
    }

	/**
	 * @param _value a string value to hash using the static salt and algorithm of this hash tool
	 * @param _salt a salt to use for this hash operation along with the static salt of this hash tool
	 * @return the hex encoded hashed value
	 */
    public synchronized String hashHex(String _value, String _salt) {
        return new String(Hex.encodeHex(hash(_value, _salt)));
    }

	/**
	 * @param _value a string value to hash using the static salt and algorithm of this hash tool
	 * @return the base64 encoded hashed value
	 */
    public synchronized String hash64(String _value) {
        return hash64(_value, null);
    }

	/**
	 * @param _value a string value to hash using the static salt and algorithm of this hash tool
	 * @param _salt a salt to use for this hash operation along with the static salt of this hash tool
	 * @return the base64 encoded hashed value
	 */
    public synchronized String hash64(String _value, String _salt) {
        return new String(Base64.encodeBase64(hash(_value, _salt)));
    }

	/**
	 * @param _value a string value to hash using the static salt and algorithm of this hash tool
	 * @return the hashed value
	 */
	public synchronized byte[] hash(String _value) {
        return hash(_value, null);
    }

	/**
	 * @param _value a string value to hash using the static salt and algorithm of this hash tool
	 * @param _salt a salt to use for this hash operation along with the static salt of this hash tool
	 * @return the hashed value
	 */
    public synchronized byte[] hash(String _value, String _salt) {
        byte[] btValue = NullUtils.toByteArray(_value);
        byte[] btSalt = NullUtils.toByteArray(_salt);
        for (int i = 0; i < iterations; i++)
            btValue = hash(salt(btValue, btSalt));
        return btValue;
    }

    /**
     * @param _value a byte array to hash using the static salt and algorithm of this hash tool
     * @return the base64 encoded hashed value
     */
    public synchronized String hash64(byte[] _value) {
        return new String(Base64.encodeBase64(hash(_value)));
    }

    /**
	 * @param _value a byte array to hash using the static salt and algorithm of this hash tool
	 * @return the hashed value
	 */
    public synchronized byte[] hash(byte[] _value) {
        return digest.digest(_value);
    }

	/**
	 * @param _value a byte array to hash using the static salt and algorithm of this hash tool
	 * @param _salt a salt to use for this hash operation along with the static salt of this hash tool
	 * @return the hashed value
	 */
    public synchronized byte[] salt(byte[] _value, byte[] _salt) {
        if (prependSalt)
            return CollectionUtils.merge(staticSalt, _salt, _value);
        return CollectionUtils.merge(_value, staticSalt, _salt);
    }
}

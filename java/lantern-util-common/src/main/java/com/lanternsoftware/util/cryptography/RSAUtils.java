package com.lanternsoftware.util.cryptography;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import com.lanternsoftware.util.NullUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lanternsoftware.util.CollectionUtils;

public abstract class RSAUtils {
    private static final Logger LOG = LoggerFactory.getLogger(RSAUtils.class);

    public static KeyPair generateRandomRSAKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.genKeyPair();
        }
        catch (NoSuchAlgorithmException _e) {
            LOG.error("Failed to generate RSA key pair", _e);
            return null;
        }
    }

    public static String toString(RSAPrivateKey _key) {
        if (_key == null)
            return null;
        StringBuilder b = new StringBuilder(Base64.encodeBase64String(_key.getModulus().toByteArray()));
        b.append(",");
        b.append(Base64.encodeBase64String(_key.getPrivateExponent().toByteArray()));
        return b.toString();
    }

    public static String toString(RSAPublicKey _key) {
        if (_key == null)
            return null;
        StringBuilder b = new StringBuilder(Base64.encodeBase64String(_key.getModulus().toByteArray()));
        b.append(",");
        b.append(Base64.encodeBase64String(_key.getPublicExponent().toByteArray()));
        return b.toString();
    }

    public static String toPEM(RSAPublicKey _key) {
        StringBuilder pem = new StringBuilder("-----BEGIN PUBLIC KEY-----\r\n");
        pem.append(NullUtils.wrap(Base64.encodeBase64String(_key.getEncoded()), 64, true));
        pem.append("\r\n-----END PUBLIC KEY-----");
        return pem.toString();
    }

    public static String toPEM(Certificate _cert) {
        try {
            StringBuilder pem = new StringBuilder("-----BEGIN CERTIFICATE-----\r\n");
            pem.append(NullUtils.wrap(Base64.encodeBase64String(_cert.getEncoded()), 64, true));
            pem.append("\r\n-----END CERTIFICATE-----");
            return pem.toString();
        } catch (CertificateEncodingException _e) {
            LOG.error("Failed to generate certificate PEM", _e);
            return null;
        }
    }

    public static String toPEM(RSAPrivateKey _key) {
        StringBuilder pem = new StringBuilder("-----BEGIN RSA PRIVATE KEY-----\r\n");
        pem.append(NullUtils.wrap(Base64.encodeBase64String(_key.getEncoded()), 64, true));
        pem.append("\r\n-----END RSA PRIVATE KEY-----");
        return pem.toString();
    }

    public static RSAPrivateKey toPrivateKey(String _privateKey64) {
        try {
            String[] parts = NullUtils.makeNotNull(_privateKey64).split(",");
            if (CollectionUtils.size(parts) == 2) {
                KeyFactory fact = KeyFactory.getInstance("RSA");
                RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(new BigInteger(Base64.decodeBase64(parts[0])), new BigInteger(Base64.decodeBase64(parts[1])));
                return (RSAPrivateKey) fact.generatePrivate(keySpec);
            }
        }
        catch (Exception _e) {
            LOG.error("Failed to generate RSA private key", _e);
        }
        return null;
    }

    public static RSAPublicKey toPublicKey(String _publicKey64) {
        try {
            String[] parts = NullUtils.makeNotNull(_publicKey64).split(",");
            if (CollectionUtils.size(parts) == 2) {
                KeyFactory fact = KeyFactory.getInstance("RSA");
                RSAPublicKeySpec keySpec = new RSAPublicKeySpec(new BigInteger(Base64.decodeBase64(parts[0])), new BigInteger(Base64.decodeBase64(parts[1])));
                return (RSAPublicKey) fact.generatePublic(keySpec);
            }
        }
        catch (Exception _e) {
            LOG.error("Failed to generate RSA public key", _e);
        }
        return null;
    }

    public static RSAPublicKey fromPEMtoPublicKey(String _pem) {
        if (_pem == null)
            return null;
        String pem = _pem.replaceAll("(-+BEGIN PUBLIC KEY-+|-+END PUBLIC KEY-+|\\r|\\n)", "");
        X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.decodeBase64(pem));
        try {
            KeyFactory fact = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) fact.generatePublic(spec);
        }
        catch (Exception _e) {
            LOG.error("Failed to generate RSA public key", _e);
            return null;
        }
    }

    public static Certificate fromPEMtoCertificate(String _pem) {
        String pem = _pem.replaceAll("(-+BEGIN CERTIFICATE-+|-+END CERTIFICATE-+|\\r|\\n)", "");
        ByteArrayInputStream is = null;
        try {
            is = new ByteArrayInputStream(Base64.decodeBase64(pem));
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return cf.generateCertificate(is);
        }
        catch (Exception _e) {
            LOG.error("Failed to generate RSA certificate", _e);
            return null;
        }
        finally {
            IOUtils.closeQuietly(is);
        }
    }

    public static Certificate loadCert(String _keystoreFileName, String _keystorePassword, String _certAlias) {
        return loadCert(loadKeystore(_keystoreFileName, _keystorePassword), _certAlias);
    }

    public static Certificate loadCert(InputStream _is, String _keystorePassword, String _certAlias) {
        return loadCert(loadKeystore(_is, _keystorePassword), _certAlias);
    }

    public static Certificate loadCert(KeyStore _keystore, String _certAlias) {
        try {
            return _keystore.getCertificate(_certAlias);
        }
        catch (Exception e) {
            LOG.error("Failed to load certificate {}", e.getMessage(), e);
            return null;
        }
    }

    public static PrivateKey loadPrivateKey(String _keystoreFileName, String _password, String _certAlias) {
        return loadPrivateKey(_keystoreFileName, _password, _password, _certAlias);
    }

    public static PrivateKey loadPrivateKey(InputStream _is, String _password, String _certAlias) {
        return loadPrivateKey(_is, _password, _password, _certAlias);
    }

    public static PrivateKey loadPrivateKey(String _keystoreFileName, String _keystorePassword, String _certPassword, String _certAlias) {
        return getPrivateKey(loadKeystore(_keystoreFileName, _keystorePassword), _certPassword, _certAlias);
    }

    public static PrivateKey loadPrivateKey(InputStream _is, String _keystorePassword, String _certPassword, String _certAlias) {
        return getPrivateKey(loadKeystore(_is, _keystorePassword), _certPassword, _certAlias);
    }

    public static PrivateKey getPrivateKey(KeyStore _keystore, String _certPassword, String _certAlias) {
        try {
            return (PrivateKey) _keystore.getKey(_certAlias, _certPassword.toCharArray());
        }
        catch (Exception e) {
            LOG.error("Failed to load key: {}", e.getMessage(), e);
            return null;
        }
    }

    public static KeyStore loadKeystore(String _keystoreFileName, String _keystorePassword) {
        try {
            return loadKeystore(new FileInputStream(_keystoreFileName), _keystorePassword);
        }
        catch (Exception e) {
            LOG.error("Failed to load keystore: {}", e.getMessage(), e);
            return null;
        }
    }

    public static KeyStore loadKeystore(InputStream _is, String _keystorePassword) {
        try {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(_is, _keystorePassword.toCharArray());
            return keystore;
        }
        catch (Exception e) {
            LOG.error("Failed to load keystore: {}", e.getMessage(), e);
            return null;
        }
        finally {
            IOUtils.closeQuietly(_is);
        }
    }

}

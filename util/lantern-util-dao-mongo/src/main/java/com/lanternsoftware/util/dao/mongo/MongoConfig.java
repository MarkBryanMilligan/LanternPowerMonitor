package com.lanternsoftware.util.dao.mongo;

import java.util.Collections;
import java.util.List;

import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.annotations.DBSerializable;
import org.apache.commons.codec.binary.Base64;

import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.ResourceLoader;
import com.lanternsoftware.util.cryptography.AESTool;

@DBSerializable
public class MongoConfig {
    private static final AESTool aes = new AESTool(4501188070455102914L,4127218394209583290L,8065326024699768144L,6272281743831953728L);
    private List<String> hosts;
    private String username;
    private String password;
    private String clientKeystorePath;
    private String clientKeystorePassword;
    private String caKeystorePath;
    private String caKeystorePassword;
    private String databaseName;
    private String authenticationDatabase;

    public MongoConfig() {
    }

    public MongoConfig(String _host, String _username, String _password, String _databaseName) {
        this(Collections.singletonList(_host), _username, _password, null, null, null, null, _databaseName);
    }

    public MongoConfig(List<String> _hosts, String _username, String _password, String _clientKeystorePath, String _clientKeystorePassword, String _caKeystorePath, String _caKeystorePassword, String _databaseName) {
        hosts = _hosts;
        username = _username;
        password = _password;
        clientKeystorePath = _clientKeystorePath;
        clientKeystorePassword = _clientKeystorePassword;
        caKeystorePath = _caKeystorePath;
        caKeystorePassword = _caKeystorePassword;
        databaseName = _databaseName;
    }

    public static AESTool getAes() {
        return aes;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> _hosts) {
        hosts = _hosts;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String _username) {
        username = _username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String _password) {
        password = _password;
    }

    public String getClientKeystorePath() {
        return clientKeystorePath;
    }

    public void setClientKeystorePath(String _clientKeystorePath) {
        clientKeystorePath = _clientKeystorePath;
    }

    public String getClientKeystorePassword() {
        return clientKeystorePassword;
    }

    public void setClientKeystorePassword(String _clientKeystorePassword) {
        clientKeystorePassword = _clientKeystorePassword;
    }

    public String getCaKeystorePath() {
        return caKeystorePath;
    }

    public void setCaKeystorePath(String _caKeystorePath) {
        caKeystorePath = _caKeystorePath;
    }

    public String getCaKeystorePassword() {
        return caKeystorePassword;
    }

    public void setCaKeystorePassword(String _caKeystorePassword) {
        caKeystorePassword = _caKeystorePassword;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String _databaseName) {
        databaseName = _databaseName;
    }

    public String getAuthenticationDatabase() {
        return authenticationDatabase;
    }

    public void setAuthenticationDatabase(String _authenticationDatabase) {
        authenticationDatabase = _authenticationDatabase;
    }

    public void saveToDisk(String _filePath) {
        ResourceLoader.writeFile(_filePath, encrypt());
    }

    public byte[] encrypt() {
        return aes.encrypt(BsonUtils.toByteArray(DaoSerializer.toDaoEntity(this).toDocument()));
    }

    public String encryptToString() {
        return Base64.encodeBase64String(encrypt());
    }

    public static MongoConfig fromDisk(String _path) {
        return decrypt(ResourceLoader.loadFile(_path));
    }

    public static MongoConfig decrypt(byte[] _configData) {
        if ((_configData == null) || (_configData.length == 0))
            return null;
        return DaoSerializer.fromDaoEntity(new DaoEntity(BsonUtils.fromByteArray(aes.decrypt(_configData))), MongoConfig.class);
    }

    public static MongoConfig decryptFromString(String _config) {
        if (NullUtils.isEmpty(_config))
            return null;
        return decrypt(Base64.decodeBase64(_config));
    }
}

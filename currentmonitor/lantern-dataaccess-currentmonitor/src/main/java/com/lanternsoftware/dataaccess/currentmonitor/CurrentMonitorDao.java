package com.lanternsoftware.dataaccess.currentmonitor;

import com.lanternsoftware.datamodel.currentmonitor.Account;
import com.lanternsoftware.util.dao.auth.AuthCode;
import com.lanternsoftware.datamodel.currentmonitor.BreakerConfig;
import com.lanternsoftware.datamodel.currentmonitor.BreakerGroup;
import com.lanternsoftware.datamodel.currentmonitor.BreakerGroupEnergy;
import com.lanternsoftware.datamodel.currentmonitor.BreakerPower;
import com.lanternsoftware.datamodel.currentmonitor.EnergyBlockViewMode;
import com.lanternsoftware.datamodel.currentmonitor.HubPowerMinute;
import com.lanternsoftware.util.dao.mongo.MongoProxy;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public interface CurrentMonitorDao {
	void shutdown();

	void putBreakerPower(BreakerPower _current);
	List<BreakerPower> getBreakerPowerForAccount(int _accountId);
	BreakerPower getLatestBreakerPower(int _accountId, int _hub, int _port);
	BreakerGroupEnergy getBreakerGroupEnergy(int _accountId, String _groupId, EnergyBlockViewMode _viewMode, Date _start);
	byte[] getBreakerGroupEnergyBinary(int _accountId, String _groupId, EnergyBlockViewMode _viewMode, Date _start);
	void putBreakerGroupEnergy(BreakerGroupEnergy _energy);

	void putHubPowerMinute(HubPowerMinute _power);

	BreakerConfig getConfig(int _accountId);
	BreakerConfig getMergedConfig(AuthCode _authCode);
	void putConfig(BreakerConfig _config);

	void updateSummaries(BreakerGroup _rootGroup, Set<Date> _daysToSummarize, TimeZone _tz);
	void rebuildSummaries(int _accountId);
	void rebuildSummariesAsync(int _accountId);
	void rebuildSummaries(int _accountId, Date _start, Date _end);

	String addPasswordResetKey(String _email);
	String getEmailForResetKey(String _key);
	boolean resetPassword(String _key, String _password);
	String authenticateAccount(String _username, String _password);
	String getAuthCodeForEmail(String _email, TimeZone _tz);
	Account authCodeToAccount(String _authCode);
	AuthCode decryptAuthCode(String _authCode);

	Account putAccount(Account _account);
	Account getAccount(int _accountId);
	Account getAccountByUsername(String _username);
	TimeZone getTimeZoneForAccount(int _accountId);
	String getTimeZoneForAccount(String _authCode);

	MongoProxy getProxy();
}

package com.lanternsoftware.powermonitor.dataaccess;

import com.lanternsoftware.powermonitor.datamodel.Account;
import com.lanternsoftware.powermonitor.datamodel.BreakerConfig;
import com.lanternsoftware.powermonitor.datamodel.BreakerPower;
import com.lanternsoftware.powermonitor.datamodel.EnergySummary;
import com.lanternsoftware.powermonitor.datamodel.EnergyViewMode;
import com.lanternsoftware.powermonitor.datamodel.HubCommand;
import com.lanternsoftware.powermonitor.datamodel.HubPowerMinute;
import com.lanternsoftware.powermonitor.datamodel.archive.ArchiveStatus;
import com.lanternsoftware.powermonitor.datamodel.hub.HubSample;
import com.lanternsoftware.util.DateRange;
import com.lanternsoftware.util.dao.auth.AuthCode;
import com.lanternsoftware.util.dao.mongo.MongoProxy;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public interface PowerMonitorDao {
	void shutdown();

	void putBreakerPower(BreakerPower _current);
	List<BreakerPower> getBreakerPowerForAccount(int _accountId);
	BreakerPower getLatestBreakerPower(int _accountId, int _hub, int _port);
	EnergySummary getEnergySummary(int _accountId, String _groupId, EnergyViewMode _viewMode, Date _start);
	byte[] getEnergySummaryBinary(int _accountId, String _groupId, EnergyViewMode _viewMode, Date _start);
	byte[] getChargeSummaryBinary(int _accountId, int _planId, String _groupId, EnergyViewMode _viewMode, Date _start);
	void putEnergySummary(EnergySummary _energy);

	void putHubPowerMinute(HubPowerMinute _power);
	Iterable<HubPowerMinute> streamHubPowerMinutes(int _accountId, Date _start, Date _end);

	void archiveMonth(int _accountId, Date _month);
	InputStream streamArchive(int _accountId, Date _month);
	void putArchiveStatus(ArchiveStatus _status);
	void deleteArchiveStatus(int _accountId, Date _month);
	List<ArchiveStatus> getArchiveStatus(int _accountId);

	DateRange getMonitoredDateRange(int _accountId);

	BreakerConfig getConfig(int _accountId);
	BreakerConfig getMergedConfig(AuthCode _authCode);
	void putConfig(BreakerConfig _config);

	void rebuildSummaries(int _accountId);
	void rebuildSummaries(int _accountId, Date _start, Date _end);

	String addPasswordResetKey(String _email);
	String getEmailForResetKey(String _key);
	boolean resetPassword(String _key, String _password);
	String authenticateAccount(String _username, String _password);
	String getAuthCodeForEmail(String _email, TimeZone _tz);
	Account authCodeToAccount(String _authCode);
	AuthCode decryptAuthCode(String _authCode);
	String exchangeAuthCode(String _authCode, int _acctId);
	boolean isTimeZoneBlacklisted(String _timezone);

	Account putAccount(Account _account);
	Account getAccount(int _accountId);
	Account getAccountByUsername(String _username);
	void deleteAccount(int _accountId);
	TimeZone getTimeZoneForAccount(int _accountId);
	String getTimeZoneForAccount(String _authCode);

	void putHubCommand(HubCommand _command);
	List<HubCommand> getAllHubCommands();
	void deleteHubCommand(String _id);

	void putHubSample(HubSample _sample);
	List<HubSample> getSamplesForAccount(int _accountId);


	MongoProxy getProxy();
}

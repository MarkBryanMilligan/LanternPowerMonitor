package com.lanternsoftware.powermonitor;

import com.lanternsoftware.powermonitor.datamodel.BreakerConfig;
import com.lanternsoftware.powermonitor.datamodel.EnergySummary;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.http.HttpPool;
import com.lanternsoftware.util.servlet.BasicAuth;
import org.apache.http.client.methods.HttpGet;

import java.util.Date;
import java.util.TimeZone;

public class GetEnergySummary {
	public static void main(String[] args) {
		HttpPool pool = new HttpPool(10, 10, 10000, 10000, 10000);
		HttpGet authRequest = new HttpGet("https://lanternpowermonitor.com/powermonitor/auth");
		authRequest.addHeader("Authorization",	BasicAuth.toHeader("<username>", "<password>"));
		String authRep = pool.executeToString(authRequest);
		String authCode = DaoSerializer.getString(DaoSerializer.parse(authRep), "auth_code");

		HttpGet configRequest = new HttpGet("https://lanternpowermonitor.com/powermonitor/config");
		configRequest.addHeader("auth_code", authCode);
		String configRep = pool.executeToString(configRequest);
		BreakerConfig config = DaoSerializer.parse(configRep, BreakerConfig.class);

		Date day = DateUtils.date(6, 5, 2022, TimeZone.getTimeZone("America/Chicago"));
		HttpGet summaryRequest = new HttpGet("https://lanternpowermonitor.com/powermonitor/energy/" + config.getRootGroup().getId() + "/DAY/" + day.getTime());
		summaryRequest.addHeader("auth_code", authCode);
		EnergySummary summary = DaoSerializer.fromZipBson(pool.executeToByteArray(summaryRequest), EnergySummary.class);
		System.out.println(DaoSerializer.toJson(summary));

		pool.shutdown();

	}
}

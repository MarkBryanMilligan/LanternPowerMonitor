package com.lanternsoftware.powermonitor;

import com.lanternsoftware.util.http.HttpPool;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

public class RebuildSummariesRemote {
	public static void main(String[] args) {
		HttpPool pool = new HttpPool(10, 10, 10000, 10000, 10000);
		HttpGet r = new HttpGet("https://lanternpowermonitor.com/powermonitor/rebuildSummaries/998");
		r.addHeader("auth_code", "<redacted>");
		CloseableHttpResponse resp = pool.execute(r);
		System.out.println(resp.getStatusLine().getStatusCode());
		pool.shutdown();
	}
}

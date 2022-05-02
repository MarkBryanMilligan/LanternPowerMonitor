package com.lanternsoftware.util.http;

public class HttpFactory {
	private static final int CONNECTIONS = 10;
	private static HttpPool pool;
	public static synchronized HttpPool pool() {
		if (pool == null)
			pool = new HttpPool(CONNECTIONS, CONNECTIONS);
		return pool;
	}

	public static synchronized void shutdown() {
		if (pool != null) {
			pool.shutdown();
			pool = null;
		}
	}
}

package com.safziy.commom.http;

import java.util.Map;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

public class HttpRequester {

	private HttpClient httpClient;

	// private static final int DEFAULT_CONNECT_TIMEOUT = 10000;

	// private static final int DEFAULT_READ_TIMEOUT = 50000;

	public static final int CODE_200 = 200;

	private HttpRequester() {
	}

	private static class LazyHolder {
		private static final HttpRequester holder = new HttpRequester();
	}

	public static final HttpRequester getInstance() {
		return LazyHolder.holder;
	}

	public void start() throws Exception {
		// SSLContext ctx = SSLContext.getInstance("TLS");
		// X509TrustManager tm = new X509TrustManager() {
		// public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		// return null;
		// }
		//
		// @Override
		// public void checkClientTrusted(java.security.cert.X509Certificate[]
		// chain, String authType)
		// throws java.security.cert.CertificateException {
		// }
		//
		// @Override
		// public void checkServerTrusted(java.security.cert.X509Certificate[]
		// chain, String authType)
		// throws java.security.cert.CertificateException {
		// }
		// };
		// ctx.init(null, new TrustManager[] { tm }, null);
		// SchemeRegistry schemeRegistry = new SchemeRegistry();
		// SSLSocketFactory ssf = new SSLSocketFactory(ctx,
		// SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		// schemeRegistry.register(new Scheme("https", 443, ssf));// 支持ssl协议
		// schemeRegistry.register(new Scheme("http", 80,
		// PlainSocketFactory.getSocketFactory()));
		// ThreadSafeClientConnManager connManager = new
		// ThreadSafeClientConnManager(schemeRegistry);
		// connManager.setMaxTotal(200);
		// connManager.setDefaultMaxPerRoute(20);
		// HttpParams httpParams = new BasicHttpParams();
		// HttpConnectionParams.setConnectionTimeout(httpParams,
		// DEFAULT_CONNECT_TIMEOUT);
		// HttpConnectionParams.setSoTimeout(httpParams, DEFAULT_READ_TIMEOUT);
		// httpClient = new DefaultHttpClient(connManager, httpParams);

		httpClient = HttpClients.createDefault();
	}

	public String doGet(String url, Map<String, Object> params, String headerName, String headerValue) {
		return HttpUtils.doGet(url, params, httpClient, headerName, headerValue);
	}

	public String doGet(String url, Map<String, Object> params) {
		return HttpUtils.doGet(url, params, httpClient);
	}

	public String doPost(String url, Map<String, Object> params) {
		return HttpUtils.doPost(url, params, httpClient);
	}

	public String doPost(String url, Map<String, Object> params, String headerName, String headerValue) {
		return HttpUtils.doPost(url, params, httpClient, headerName, headerValue);
	}

	public String doPost(String url, String param, String headerName, String headerValue) {
		return HttpUtils.doPost(url, param, httpClient, headerName, headerValue);
	}

	public String doPost(String url, String param) {
		return HttpUtils.doPost(url, param, httpClient);
	}

}

package com.safziy.commom.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.safziy.commom.utils.ValidateUtils;
import com.safziy.service.log.LogUtil;

public class HttpUtils {

	public static final String ERROR = "{\"ret\":100}";

	public static String doGet(String url, HttpClient httpClient) {
		HttpGet httpGet = null;
		try {
			httpGet = new HttpGet(url);
			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String result = EntityUtils.toString(entity, "utf8");
				return result;
			} else {
				return ERROR;
			}
		} catch (Exception e) {
			if (ValidateUtils.isNotNull(httpGet)) {
				httpGet.abort();
			}
			LogUtil.error(e);
			return ERROR;
		}
	}

	public static String doGet(String url, HttpClient httpClient, String headerName, String headerValue) {
		HttpGet httpGet = null;
		try {
			httpGet = new HttpGet(url);
			if (ValidateUtils.isNotNull(headerName) && ValidateUtils.isNotNull(headerValue)) {
				httpGet.setHeader(headerName, headerValue);
			}
			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String result = EntityUtils.toString(entity, "utf8");
				return result;
			} else {
				return ERROR;
			}
		} catch (Exception e) {
			if (ValidateUtils.isNotNull(httpGet)) {
				httpGet.abort();
			}
			LogUtil.error(e);
			return ERROR;
		}
	}

	/**
	 * 执行一个HTTP POST请求，返回请求响应的HTML
	 * 
	 * @param url
	 *            请求的URL地址
	 * @param params
	 *            请求的查询参数,可以为null
	 * @param charset
	 *            字符集
	 * @param pretty
	 *            是否美化
	 * @return 返回请求响应的HTML
	 */
	public static String doPost(String url, Map<String, Object> params, HttpClient client) {
		HttpPost httpPost = null;
		try {
			httpPost = new HttpPost(url);
			if (ValidateUtils.isNotNull(params)) {
				List<NameValuePair> nvps = new LinkedList<NameValuePair>();
				for (Map.Entry<String, Object> entry : params.entrySet()) {
					nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
				}
				httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
			}

			HttpResponse response = client.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String result = EntityUtils.toString(entity, "utf8");
				return result;
			} else {
				return ERROR;
			}
		} catch (Exception e) {
			if (ValidateUtils.isNotNull(httpPost)) {
				httpPost.abort();
			}
			LogUtil.error(e);
			return ERROR;
		}
	}

	public static String doPost(String url, Map<String, Object> params, HttpClient client, String headerName,
			String headerValue) {
		HttpPost httpPost = null;
		try {
			httpPost = new HttpPost(url);
			if (ValidateUtils.isNotNull(headerName) && ValidateUtils.isNotNull(headerValue)) {
				httpPost.setHeader(headerName, headerValue);
			}
			if (ValidateUtils.isNotNull(params)) {
				List<NameValuePair> nvps = new LinkedList<NameValuePair>();
				for (Map.Entry<String, Object> entry : params.entrySet()) {
					nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
				}
				httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
			}

			HttpResponse response = client.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String result = EntityUtils.toString(entity, "utf8");
				return result;
			} else {
				return ERROR;
			}
		} catch (Exception e) {
			if (ValidateUtils.isNotNull(httpPost)) {
				httpPost.abort();
			}
			LogUtil.error(e);
			return ERROR;
		}
	}

	public static String doGet(String url, Map<String, Object> params, HttpClient httpClient) {

		StringBuilder urlBuilder = new StringBuilder();
		if (ValidateUtils.isNotNullAndMoreThanZero(params)) {
			urlBuilder.append(url).append("?").append(createQueryStr(params));
		} else {
			urlBuilder.append(url);
		}

		return doGet(urlBuilder.toString(), httpClient);
	}

	public static String doGet(String url, Map<String, Object> params, HttpClient httpClient, String headerName,
			String headerValue) {

		StringBuilder urlBuilder = new StringBuilder();
		if (ValidateUtils.isNotNullAndMoreThanZero(params)) {
			urlBuilder.append(url).append("?").append(createQueryStr(params));
		} else {
			urlBuilder.append(url);
		}

		return doGet(urlBuilder.toString(), httpClient, headerName, headerValue);
	}

	private static String createQueryStr(Map<String, Object> params) {
		StringBuilder queryBuilder = new StringBuilder();
		String seperator = "";
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			queryBuilder.append(seperator + entry.getKey() + "=" + entry.getValue());
			seperator = "&";
		}
		return queryBuilder.toString();
	}

	/**
	 * 序列化对象
	 */
	public static byte[] toByte(Object obj) {
		byte[] bytes = null;
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ObjectOutputStream oo = null;
		try {
			oo = new ObjectOutputStream(bo);
			oo.writeObject(obj);
			bytes = bo.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bo.flush();
				bo.close();
				if (oo != null) {
					oo.flush();
					oo.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bytes;
	}

	public static String doPost(String url, String param, HttpClient httpClient) {
		HttpPost httpPost = null;
		try {
			httpPost = new HttpPost(url);
			if (ValidateUtils.isNotNull(param)) {
				httpPost.setEntity(new ByteArrayEntity(param.getBytes("UTF-8")));
			}

			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String result = EntityUtils.toString(entity, "utf8");
				return result;
			} else {
				return ERROR;
			}
		} catch (Exception e) {
			if (ValidateUtils.isNotNull(httpPost)) {
				httpPost.abort();
			}
			LogUtil.error(e);
			return ERROR;
		}
	}

	public static String doPost(String url, String param, HttpClient httpClient, String headerName, String headerValue) {
		HttpPost httpPost = null;
		try {
			httpPost = new HttpPost(url);
			if (ValidateUtils.isNotNull(headerName) && ValidateUtils.isNotNull(headerValue)) {
				httpPost.setHeader(headerName, headerValue);
			}
			if (ValidateUtils.isNotNull(param)) {
				httpPost.setEntity(new ByteArrayEntity(param.getBytes("UTF-8")));
			}
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String result = EntityUtils.toString(entity, "utf8");
				return result;
			} else {
				return ERROR;
			}
		} catch (Exception e) {
			if (ValidateUtils.isNotNull(httpPost)) {
				httpPost.abort();
			}
			LogUtil.error(e);
			return ERROR;
		}
	}
}

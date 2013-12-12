package com.gloria.phoneinterface.communications;

import java.io.UnsupportedEncodingException;
import java.security.KeyStore;

import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.util.Log;

import com.gloria.offlineexperiments.Base64;

public class GloriaApiProxy {
	

	private HttpClient lastHttpClient = null;

	public HttpClient getHttpClient() {
		shutdown();
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore
					.getDefaultType());
			trustStore.load(null, null);

			SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(
					params, registry);

			lastHttpClient = new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			lastHttpClient = new DefaultHttpClient();
		}
		return lastHttpClient;
	}

	public void shutdown() {
		if (lastHttpClient != null) {
			lastHttpClient.getConnectionManager().shutdown();
			lastHttpClient = null;
		}
	}

	public HttpGet getHttpGetRequest(String operationURL, String authToken) {
		HttpGet getRequest = new HttpGet(operationURL);
		setHeaders(getRequest, authToken);
		return getRequest;
	}

	public HttpPost getHttpPostRequest(String operationURL, String authToken,
			HttpEntity entity) {
		HttpPost postRequest = new HttpPost(operationURL);
		setHeaders(postRequest, authToken);
		postRequest.setEntity(entity);
		return postRequest;
	}

	public String getAuthorizationTokenFromUserPassword(String username,
			String password) {
		String accessToken = username + ":" + password;
		byte[] accessTokenBytes;
		try {
			accessTokenBytes = accessToken.getBytes("iso-8859-1");
		} catch (UnsupportedEncodingException e) {
			Log.e("GLORIA API", "Cannot encode authorization token!");
			return null;
		}
		return Base64.encodeBase64String(accessTokenBytes);
	}

	private void setHeaders(HttpRequestBase request, String authToken) {
		request.setHeader("content-type", "application/json");
		request.setHeader("Authorization", "Basic " + authToken);
	}
}

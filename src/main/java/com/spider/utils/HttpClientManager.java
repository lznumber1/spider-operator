/**
 * 
 */
package com.spider.utils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author zhe.li
 * @date 2017年7月15日
 */
@Component
public class HttpClientManager {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${httpclient.connect.timeout}")
	private int connectTimeout;

	@Value("${httpclient.socket.timeout}")
	private int socketTimeout;

	@Value("${httpclient.pool.max}")
	private int maxPoolSize;

	@Value("${httpclient.perroute.max}")
	private int maxPerRoute;

	@Value("${fetch.retry.count}")
	protected int retryCount;

	private PoolingHttpClientConnectionManager connManager;

	private HttpRequestRetryHandler retryHandler;

	private final Map<String, CloseableHttpClient> httpClients = new HashMap<>();
	private final Map<String, CookieStore> cookieStores = new HashMap<>();

	@PostConstruct
	public void init() {
		Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", buildSSLConnectionSocketFactory()).build();
		connManager = new PoolingHttpClientConnectionManager(reg);
		connManager.setDefaultMaxPerRoute(maxPerRoute);
		connManager.setMaxTotal(maxPoolSize);
		retryHandler = new RequestRetryHandler(retryCount);
	}

	public CloseableHttpClient getHttpClient(String bizno) {
		return httpClients.get(bizno);
	}

	public CloseableHttpClient buildHttpClient(String bizno) {
		RequestConfig.Builder requestConfigBuilder = RequestConfig.custom().setConnectTimeout(connectTimeout)
				.setSocketTimeout(socketTimeout).setCircularRedirectsAllowed(false);

		// requestConfigBuilder.setProxy(new HttpHost("127.0.0.1", 8888));

		CookieStore cookieStore = new BasicCookieStore();

		List<Header> defaultHeaders = new ArrayList<>();
		defaultHeaders.add(new BasicHeader("Accept", "application/json, html/text, */*; q=0.01"));
		defaultHeaders.add(new BasicHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3"));
		defaultHeaders.add(new BasicHeader("Accept-Encoding", "gzip, deflate"));
		defaultHeaders.add(new BasicHeader("Connection", "keep-alive"));
		defaultHeaders.add(new BasicHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 6.2; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0"));

		CloseableHttpClient client = HttpClientBuilder.create()//
				.setDefaultCookieStore(cookieStore)//
				.setDefaultRequestConfig(requestConfigBuilder.build())//
				.setConnectionManager(connManager)//
				.setDefaultHeaders(defaultHeaders)//
				.setRetryHandler(retryHandler)//
				.build();

		cookieStores.put(bizno, cookieStore);
		httpClients.put(bizno, client);

		return client;
	}

	public String getCookie(final String bizno, final String cookieName) {
		for (Cookie c : cookieStores.get(bizno).getCookies()) {
			if (cookieName.equalsIgnoreCase(c.getName())) {
				return c.getValue();
			}
		}
		return null;
	}

	private SSLConnectionSocketFactory buildSSLConnectionSocketFactory() {
		try {
			return new SSLConnectionSocketFactory(createIgnoreVerifySSL()); // 优先绕过安全证书
		} catch (KeyManagementException e) {
			logger.error("ssl connection fail", e);
		} catch (NoSuchAlgorithmException e) {
			logger.error("ssl connection fail", e);
		}
		return SSLConnectionSocketFactory.getSocketFactory();
	}

	private SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
		// 实现一个X509TrustManager接口，用于绕过验证
		X509TrustManager trustManager = new X509TrustManager() {

			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

		};

		SSLContext sc = SSLContext.getInstance("SSLv3");
		sc.init(null, new TrustManager[] { trustManager }, null);
		return sc;
	}

}

class RequestRetryHandler implements HttpRequestRetryHandler {
	private int retryCount;

	public RequestRetryHandler(int retryCount) {
		this.retryCount = retryCount;
	}

	@Override
	public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
		Args.notNull(exception, "Exception parameter");
		Args.notNull(context, "HTTP context");
		if (executionCount > this.retryCount) {
			return false;
		}
		return true;
	}
}

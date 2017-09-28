package com.spider.fetcher;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;

import com.spider.consts.CrawlCode;
import com.spider.utils.Base64Utils;

public abstract class BaseFetcher {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${fetch.retry.count}")
	protected int retryCount;

	private Queue<AsyncFetchTask> tasks = new LinkedList<>();

	public abstract Map<String, Object> login(Map<String, Object> params) throws Exception;

	public abstract CrawlCode fetch(Map<String, Object> params);

	@Autowired
	protected TaskExecutor taskExecutor;

	@PostConstruct
	public void init() {
		new Thread(() -> {
			while (true) {
				try {
					AsyncFetchTask task = tasks.poll();
					if (task == null) {
						TimeUnit.SECONDS.sleep(5);
						continue;
					}
					if (task.getAlreadyExecCount() < retryCount + 1) {
						taskExecutor.execute(task);
					}
				} catch (Exception e) {
					logger.error("采集任务错误", e);
				}
			}
		}).start();
	}

	protected void addFetch(Map<String, Object> params) {
		AsyncFetchTask task = new AsyncFetchTask(params);
		this.tasks.offer(task);
	}

	protected Map<String, Object> status(Map<String, Object> map, CrawlCode code) {
		map.put("code", code.getCode());
		map.put("msg", code.getMsg());
		return map;
	}

	protected String getText(String url, CloseableHttpClient client) throws Exception {
		CloseableHttpResponse response = client.execute(new HttpGet(url));
		return extractText(response);
	}

	protected String getText(HttpUriRequest request, CloseableHttpClient client) throws Exception {
		CloseableHttpResponse response = client.execute(request);
		return extractText(response);
	}

	protected String postText(String url, CloseableHttpClient client) throws Exception {
		CloseableHttpResponse response = client.execute(new HttpPost(url));
		return extractText(response);
	}

	protected String postText(HttpUriRequest request, CloseableHttpClient client) throws Exception {
		CloseableHttpResponse response = client.execute(request);
		return extractText(response);
	}

	protected String postText(String url, Map<String, String> parameters, CloseableHttpClient client) throws Exception {
		HttpPost request = new HttpPost(url);
		final List<NameValuePair> params = new ArrayList<>();
		parameters.forEach((k, v) -> {
			params.add(new BasicNameValuePair(k, v));
		});
		request.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		CloseableHttpResponse response = client.execute(request);
		return extractText(response);
	}

	protected byte[] getBytes(String url, CloseableHttpClient client) throws Exception {
		CloseableHttpResponse response = client.execute(new HttpGet(url));
		return extractByteArray(response);
	}

	protected String extractText(CloseableHttpResponse response) throws Exception {
		try {
			String text = EntityUtils.toString(response.getEntity());
			logger.info(text);
			return text;
		} finally {
			close(response);
		}
	}

	protected byte[] extractByteArray(CloseableHttpResponse response) throws Exception {
		try {
			return EntityUtils.toByteArray(response.getEntity());
		} finally {
			close(response);
		}
	}

	protected String getCaptchaImg(String url, CloseableHttpClient client) throws Exception {
		byte[] data = getBytes(url, client);
		return Base64Utils.imageToBase64(data);
	}

	protected void close(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class AsyncFetchTask implements Runnable {
		private Map<String, Object> params;
		private int alreadyExecCount;

		public AsyncFetchTask(Map<String, Object> params) {
			super();
			this.params = params;
		}

		@Override
		public void run() {
			alreadyExecCount++;
			fetch(params);
		}

		public int getAlreadyExecCount() {
			return alreadyExecCount;
		}

	}

}

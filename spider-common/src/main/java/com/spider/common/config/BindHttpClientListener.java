/**
 * 
 */
package com.spider.common.config;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.spider.common.utils.BiznoGenerator;
import com.spider.common.utils.HttpClientManager;

/**
 * @author zhe.li
 * @date 2017年7月15日
 */
@WebListener
public class BindHttpClientListener implements HttpSessionListener {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private HttpClientManager httpClientManager;

	public void sessionCreated(HttpSessionEvent se) {
		String bizno = BiznoGenerator.gen();
		logger.info("生成采集流水号: {}", bizno);
		se.getSession().setAttribute("bizno", bizno);
		httpClientManager.buildHttpClient(bizno);
	}

	public void sessionDestroyed(HttpSessionEvent se) {

	}

}

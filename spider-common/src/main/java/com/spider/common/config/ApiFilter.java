package com.spider.common.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

@WebFilter(urlPatterns = "/api/*")
public class ApiFilter implements Filter {

	private final String contentType = "application/json;charset=UTF-8";

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		response.setContentType(contentType);
		String bizno = (String) request.getSession().getAttribute("bizno");
		logger.debug("====>>>> 采集流水号为: {}", bizno);
		// String reqContent = getRequestBody(request);
		try {
			chain.doFilter(request, response);
		} catch (Exception e) {
			logger.error("处理请求错误", e);
		}
	}

	@Override
	public void destroy() {

	}

	String getStep(HttpServletRequest request) throws IOException {
		String body = getRequestBody(request);
		return JSON.parseObject(body).getString("step");
	}

	String getRequestBody(HttpServletRequest request) throws IOException {
		int len = request.getContentLength();
		ServletInputStream in = request.getInputStream();
		byte[] buffer = new byte[len];
		in.read(buffer, 0, len);
		String value = new String(buffer, "UTF-8");
		return value;
	}

}

package com.spider.config;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;

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
		// ResponseWrapper responseWrapper = new ResponseWrapper(response);
		// String resContent = new String(responseWrapper.getDataStream(), "UTF-8");
		// RestResponse fullResponse = new RestResponse(205, "OK-MESSAGE", resContent);
		// byte[] responseToSend = restResponseBytes(fullResponse);
		// response.getOutputStream().write(responseToSend);
		//
		// logger.info("request:{} ;resposne:{}", reqContent, resContent);
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

	String getResponseBody(HttpServletResponse response) throws IOException {
		return "";
	}

	byte[] restResponseBytes(RestResponse response) throws IOException {
		return new ObjectMapper().writeValueAsString(response).getBytes("UTF-8");
	}
}

class RestResponse implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 615995329051069751L;

	private int status;

	private String message;

	private Object data;

	public RestResponse(int status, String message, Object data) {
		this.status = status;
		this.message = message;
		this.data = data;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

}

class FilterServletOutputStream extends ServletOutputStream {

	DataOutputStream output;

	public FilterServletOutputStream(OutputStream output) {
		this.output = new DataOutputStream(output);
	}

	@Override
	public void write(int arg0) throws IOException {
		output.write(arg0);
	}

	@Override
	public void write(byte[] arg0, int arg1, int arg2) throws IOException {
		output.write(arg0, arg1, arg2);
	}

	@Override
	public void write(byte[] arg0) throws IOException {
		output.write(arg0);
	}

	@Override
	public boolean isReady() {
		return false;
	}

	@Override
	public void setWriteListener(WriteListener listener) {
	}
}

class ResponseWrapper extends HttpServletResponseWrapper {
	ByteArrayOutputStream output;
	FilterServletOutputStream filterOutput;
	// HttpResponseStatus status = HttpResponseStatus.OK;

	public ResponseWrapper(HttpServletResponse response) {
		super(response);
		output = new ByteArrayOutputStream();
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (filterOutput == null) {
			filterOutput = new FilterServletOutputStream(output);
		}
		return filterOutput;
	}

	public byte[] getDataStream() {
		return output.toByteArray();
	}
}
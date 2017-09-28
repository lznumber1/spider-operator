package com.spider.config;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import com.alibaba.druid.support.http.StatViewServlet;

@WebServlet(urlPatterns = "/druid/*", //
		initParams = { /*
						 * @WebInitParam(name = "loginUsername", value = "druid"),
						 * 
						 * @WebInitParam(name = "loginPassword", value = "druid"),
						 */
				@WebInitParam(name = "resetEnable", value = "true") })
public class DruidStatViewServlet extends StatViewServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4796601262402132186L;

}

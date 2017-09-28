/**
 * 
 */
package com.spider.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spider.consts.CrawlCode;
import com.spider.consts.CrawlStep;

/**
 * @author zhe.li
 * @date 2017年7月15日
 */
public abstract class BaseController {
	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected Map<String, Object> status(Map<String, Object> map, CrawlCode code) {
		map.put("code", code.getCode());
		map.put("msg", code.getMsg());
		return map;
	}

	protected boolean validateTel(String tel) {
		if (StringUtils.isBlank(tel) || !tel.matches("\\d{11}")) {
			return false;
		}
		return true;
	}

	protected JSONObject transRequest(String json) {
		JSONObject params = null;
		try {
			params = JSON.parseObject(json);
			params.put("step", CrawlStep.valueOf(params.getString("step").toUpperCase()));
		} catch (Exception e) {
			logger.error("请求错误,请求参数: {}", json);
			return null;
		}
		return params;
	}

	protected String getBizno(HttpServletRequest request) {
		return (String) request.getSession().getAttribute("bizno");
	}

}

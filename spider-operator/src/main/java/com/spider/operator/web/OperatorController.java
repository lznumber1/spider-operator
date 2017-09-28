/**
 * 
 */
package com.spider.operator.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.spider.common.consts.CrawlCode;
import com.spider.common.controller.BaseController;
import com.spider.common.model.tel.TelModel;
import com.spider.operator.fetcher.MobileFetcher;
import com.spider.operator.fetcher.UnicomFetcher;
import com.spider.operator.service.TelModelService;

/**
 * @author zhe.li
 * @date 2017年7月15日
 */
@Scope("singleton")
@RestController
@RequestMapping("/api/operator")
public class OperatorController extends BaseController {

	@Autowired
	private UnicomFetcher unicomFetcher;
	@Autowired
	private MobileFetcher mobileFetcher;
	@Autowired
	private TelModelService telModelService;

	@PostMapping(value = "/auth/v1", consumes = "application/json;charset=UTF-8")
	public Map<String, Object> auth(@RequestBody String json, HttpServletRequest request) {
		Map<String, Object> map = new HashMap<>();

		JSONObject params = transRequest(json);
		if (params == null) {
			return status(map, CrawlCode.BadRequest);
		}

		try {
			String bizno = getBizno(request);
			params.put("bizno", bizno);

			String operator = params.getString("operator");

			Map<String, Object> result = new HashMap<>();
			if ("移动".equals(operator)) {
				result = mobileFetcher.login(params);
			} else if ("联通".equals(operator)) {
				result = unicomFetcher.login(params);
			} else {
				result.put("code", 700);
				result.put("code", "暂不支持");
			}

			if (CrawlCode.LoginSuccess.getCode() == (int) result.get("code")) {// 登录成功销毁session
				request.getSession().invalidate();
			}

			map.putAll(result);

		} catch (Exception e) {
			logger.error("交互错误", e);
			return status(map, CrawlCode.Unknown);
		}

		return map;
	}

	@RequestMapping("/query/v1/{tel}")
	public Map<String, Object> query(@PathVariable String tel) {
		Map<String, Object> map = new HashMap<>();

		try {
			TelModel data = telModelService.query(tel);
			if (data == null) {
				map.put("code", 300);
				map.put("msg", "未查到");
			} else {
				map.put("code", 200);
				map.put("msg", "查到");
				map.put("result", data);
			}
		} catch (Exception e) {
			map.put("code", 500);
			map.put("msg", "服务异常");
			logger.error("查询出错", e);
		}

		return map;
	}

}

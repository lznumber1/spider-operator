/**
 * 
 */
package com.spider.operator.fetcher;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spider.common.consts.CrawlCode;
import com.spider.common.consts.CrawlStep;
import com.spider.common.fetcher.BaseFetcher;
import com.spider.common.model.tel.TelBasic;
import com.spider.common.model.tel.TelBillDetail;
import com.spider.common.model.tel.TelCallDetail;
import com.spider.common.model.tel.TelModel;
import com.spider.common.model.tel.TelNetDetail;
import com.spider.common.model.tel.TelSmsDetail;
import com.spider.common.utils.Base64Utils;
import com.spider.common.utils.HttpClientManager;
import com.spider.operator.service.TelModelService;

/**
 * @author zhe.li
 * @date 2017年7月15日
 */
@Scope("singleton")
@Component
public class UnicomFetcher extends BaseFetcher {

	@Autowired
	private HttpClientManager httpClientManager;
	@Autowired
	private TelModelService persistService;

	@Override
	public Map<String, Object> login(Map<String, Object> params) throws Exception {
		Map<String, Object> map = new HashMap<>();

		CrawlStep step = (CrawlStep) params.get("step");
		String tel = (String) params.get("tel");
		String bizno = (String) params.get("bizno");

		CloseableHttpClient client = httpClientManager.getHttpClient(bizno);

		if (step == CrawlStep.INIT) {// 检测是否需要验证码
			boolean needCaptcha = checkNeedCaptcha(tel, client);
			if (needCaptcha) {
				status(map, CrawlCode.NeedCaptcha);
				map.put("img", refreshCaptcha(client));
			} else {
				status(map, CrawlCode.NotNeedCaptcha);
			}
		} else if (step == CrawlStep.SENDCAPTCHA) {// 刷新验证码图片
			String base64Img = refreshCaptcha(client);
			if (StringUtils.isNotBlank(base64Img)) {
				status(map, CrawlCode.CaptchaSuccess);
				map.put("img", base64Img);
			} else {
				status(map, CrawlCode.CaptchaFailed);
			}
		} else if (step == CrawlStep.SENDSMS) {// 发送登录短信
			String smsCode = sendSMS(tel, client);
			if ("0000".equals(smsCode)) {
				status(map, CrawlCode.SMSSuccess);
			} else if ("7098".equals(smsCode)) {
				status(map, CrawlCode.SMSOVER);
			} else {
				status(map, CrawlCode.SMSFailed);
			}
		} else if (step == CrawlStep.VALIDCAPTCHA) {// 验证图片验证码
			String captcha = (String) params.get("captcha");
			if (validCaptcha(captcha, client)) {
				status(map, CrawlCode.ValidCapSucc);
			} else {
				status(map, CrawlCode.ValidCapError);
			}
		} else if (step == CrawlStep.LOGIN) {// 登录
			String password = (String) params.get("password");
			String code = (String) params.get("code");
			String captcha = (String) params.get("captcha");
			realLogin(tel, password, code, captcha, client, map, params, bizno);
		} else if (step == CrawlStep.SENDVALIDSMS) {// 发送身份验证短信
			sendValidSMS(tel, client, map);
		} else if (step == CrawlStep.VALID) {// 身份验证
			String code = (String) params.get("code");
			validate(tel, code, client, map, params);
		}
		return map;
	}

	@Override
	@Async
	public CrawlCode fetch(Map<String, Object> params) {
		String tel = (String) params.get("tel");
		String bizno = (String) params.get("bizno");
		CloseableHttpClient client = httpClientManager.getHttpClient(bizno);

		logger.info("采集流水号:{} 手机号:{} 登录成功,开始异步采集", bizno, tel);

		try {
			TelModel data = new TelModel(bizno);
			data.setBizno(bizno);

			// 基本信息
			TelBasic telBasic = getBasic(bizno, client);
			data.setBasic(telBasic);

			// 通话详单
			List<TelCallDetail> callDetails = getCallDetail(bizno, client);
			data.setCallDetails(callDetails);

			// 短信详单
			List<TelSmsDetail> smsDetails = getSmsDetail(bizno, client);
			data.setSmsDetails(smsDetails);

			// 历史账单
			List<TelBillDetail> billDetails = getBillDetail(bizno, client);
			data.setBillDetails(billDetails);

			// 上网记录
			List<TelNetDetail> netDetails = getNetDetail(bizno, client);
			data.setNetDetails(netDetails);

			// 持久化
			persistService.persist(data);

			logger.info("抓取的数据: {}", JSON.toJSONString(data));

		} catch (Exception e) {
			logger.error("采集错误,流水号:{} 手机号:{}", bizno, tel);
			return CrawlCode.FetchFailed;
		}

		return CrawlCode.FetchSuccess;
	}

	private boolean validCaptcha(String captcha, CloseableHttpClient client) throws Exception {
		String url = String.format(
				"https://uac.10010.com/portal/Service/CtaIdyChk?callback=jQuery172011731003193063283_%d&verifyCode=%s&verifyType=1&_=%d",
				new Date().getTime(), captcha, new Date().getTime());
		String text = getText(url, client);
		Matcher m = Pattern.compile("\\{\\\"resultCode\\\"\\:\\\"(true|false)\\\"\\}").matcher(text);
		if (m.find()) {
			return Boolean.parseBoolean(m.group(1));
		}
		return false;
	}

	private void realLogin(String tel, String password, String code, String captcha, CloseableHttpClient client,
			Map<String, Object> map, Map<String, Object> params, String bizno) throws Exception {
		String urlFormat = "https://uac.10010.com/portal/Service/MallLogin?callback=jQuery172018511274194241223_%d&req_time=%d&redirectURL=http://www.10010.com&userName=%s&password=%s&pwdType=%s&productType=01&uvc=%s&verifyCode=%s&redirectType=01&rememberMe=1&_=%d";
		String uvc = httpClientManager.getCookie(bizno, "uacverifykey");
		String url = String.format(urlFormat, new Date().getTime(), new Date().getTime(), tel, password, "01",
				uvc == null ? StringUtils.EMPTY : uvc, captcha == null ? StringUtils.EMPTY : captcha,
				new Date().getTime());
		logger.info("Real Login Url: {}", url);
		String text = getText(url, client);
		logger.info("tel:{}, real login response:{}", tel, text);
		Matcher m = Pattern.compile("resultCode\\:\\\"?(\\d+)\\\"?").matcher(text);
		if (m.find()) {
			String s = m.group(1);
			if ("0000".equals(s)) {
				// 获取e3和route
				getText("http://iservice.10010.com/e3/static/common/mall_info?callback=jsonp1500307501622", client);
				status(map, CrawlCode.NeedValid);// 需要身份验证
			} else if ("7001".equals(s)) {// 需要图片验证码
				status(map, CrawlCode.NeedCaptcha);
				map.put("img", refreshCaptcha(client));
			} else if ("7007".equals(s) || "7072".equals(s) || "7110".equals(s)) {// 用户名密码错误
				status(map, CrawlCode.UserPwdError);
			} else if ("7038".equals(s) || "7110".equals(s)) {// 出错次数已达上限
				status(map, CrawlCode.LoginFailed);
				map.put("msg", "出错次数已达上限，请3小时后重试");
			} else {
				status(map, CrawlCode.LoginFailed);
			}
		}
	}

	private void validate(String tel, String code, CloseableHttpClient client, Map<String, Object> map,
			Map<String, Object> params) {
		try {
			HttpPost request = new HttpPost(
					"http://iservice.10010.com/e3/static/query/verificationSubmit?_=1500345269365&accessURL=http://iservice.10010.com/e4/query/bill/call_dan-iframe.html?menuCode=000100030001&menuid=000100030001");
			List<NameValuePair> parameters = new ArrayList<>();
			parameters.add(new BasicNameValuePair("inputcode", code));
			parameters.add(new BasicNameValuePair("menuId", "000100030001"));
			request.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
			String text = getText(request, client);
			JSONObject json = JSON.parseObject(text);
			String flag = json.getString("flag");
			if ("00".equals(flag)) {
				status(map, CrawlCode.LoginSuccess);
				addFetch(params);// 登录成功，异步采集
			} else if ("02".equals(flag)) {
				status(map, CrawlCode.ValidFailed);
			} else {
				status(map, CrawlCode.Unknown);
			}
		} catch (Exception e) {
			logger.error("身份验证发生异常", e);
			status(map, CrawlCode.Unknown);
		}

	}

	private void sendValidSMS(String tel, CloseableHttpClient client, Map<String, Object> map) {
		try {
			String url = String.format(
					"http://iservice.10010.com/e3/static/query/sendRandomCode?_=%d&accessURL=http://iservice.10010.com/e4/query/bill/call_dan-iframe.html?menuCode=000100030001&menuid=000100030001",
					new Date().getTime());
			Map<String, String> parameters = new HashMap<>();
			parameters.put("menuId", "000100030001");
			postText(url, parameters, client);
			// String text = postText(url, parameters, client);
			// JSONObject json = JSON.parseObject(text);
			// if (json.getBooleanValue("issuccess") && json.getBooleanValue("sendcode")) {
			status(map, CrawlCode.SendValidSuccess);
			// } else {
			// status(map, CrawlCode.SendValidFailed);
			// }
		} catch (Exception e) {
			logger.error("发送身份验证短信失败", e);
			status(map, CrawlCode.SendValidFailed);
		}
	}

	private String sendSMS(String tel, CloseableHttpClient client) throws Exception {
		String url = String.format(
				"https://uac.10010.com/portal/Service/SendMSG?callback=jQuery172018511274194241223_%d&req_time=%d&mobile=%s&_=%d",
				new Date().getTime(), new Date().getTime(), tel, new Date().getTime());
		String text = getText(url, client);
		Matcher m = Pattern.compile("resultCode\\:\\\"(\\d+)\\\"").matcher(text);
		if (m.find()) {
			String s = m.group(1);
			return s;
		}
		return null;
	}

	private String refreshCaptcha(CloseableHttpClient client) {
		try {
			CloseableHttpResponse response = client.execute(new HttpGet(
					String.format("https://uac.10010.com/portal/Service/CreateImage?t=%d", new Date().getTime())));
			byte[] data = extractByteArray(response);
			return Base64Utils.imageToBase64(data);
		} catch (Exception e) {
			logger.error("刷新验证码错误", e);
		}
		return StringUtils.EMPTY;
	}

	private boolean checkNeedCaptcha(String tel, CloseableHttpClient client) throws Exception {
		String url = String.format(
				"https://uac.10010.com/portal/Service/CheckNeedVerify?callback=jQuery172044089140908447855_%d&userName=%s&pwdType=01&_=%d",
				new Date().getTime(), tel, new Date().getTime());
		String text = getText(url, client);
		Matcher m = Pattern.compile("\\{\\\"resultCode\\\"\\:\\\"(true|false)\\\"\\}").matcher(text);
		if (m.find()) {
			return Boolean.parseBoolean(m.group(1));
		}
		return false;
	}

	private TelBasic getBasic(String bizno, CloseableHttpClient client) {
		TelBasic data = new TelBasic(bizno);
		try {
			String url = String.format("http://iservice.10010.com/e3/static/query/userinfoquery?_=%d",
					new Date().getTime());
			String text = postText(url, client);
			JSONObject json = JSON.parseObject(text);
			data.setTel(json.getJSONObject("userInfo").getString("usernumber"));
			data.setName(json.getJSONObject("userInfo").getString("custName"));
			data.setCertno(json.getJSONObject("userInfo").getString("certnum"));
			data.setAddress(json.getJSONObject("userInfo").getString("certaddr"));
			data.setJoinDate(new SimpleDateFormat("yyyyMMdd")
					.parse(json.getJSONObject("userInfo").getString("opendate").substring(0, 8)));
			data.setStatus(json.getJSONObject("userInfo").getString("status"));
			data.setLeftAmount(json.getJSONObject("resource").getDouble("balance"));
			text = postText(String.format(
					"http://iservice.10010.com/e3/static/query/consumptionAnalysis?_=%d&accessURL=http://iservice.10010.com/e4/index_server.html",
					new Date().getTime()), client);
			data.setCurMonthCost(JSON.parseObject(text).getDouble("totalfee"));
			data.setSuccess(true);
		} catch (Exception e) {
			logger.error("采集基本信息异常", e);
		}
		return data;
	}

	private List<TelSmsDetail> getSmsDetail(String bizno, CloseableHttpClient client) {
		List<TelSmsDetail> list = new ArrayList<>();
		try {
			String urlFormat = "http://iservice.10010.com/e3/static/query/sms?_=%d&accessURL=http://iservice.10010.com/e4/query/calls/call_sms-iframe.html?menuCode=000100030002&menuid=000100030002";
			DateFormat df = new SimpleDateFormat("yyyyMMdd");
			DateFormat df2 = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
			Calendar c = Calendar.getInstance(Locale.CHINA);
			c.setTime(new Date());
			Date endDate = c.getTime();
			c.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH));
			Date beginDate = c.getTime();
			for (int x = 0; x < 6; x++, c.add(Calendar.DAY_OF_MONTH, -1), endDate = c.getTime(), c
					.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH)), beginDate = c.getTime()) {
				try {
					HttpPost request = new HttpPost(String.format(urlFormat, new Date().getTime()));
					List<NameValuePair> parameters = new ArrayList<>();
					parameters.add(new BasicNameValuePair("pageNo", "1"));
					parameters.add(new BasicNameValuePair("pageSize", "1000"));
					parameters.add(new BasicNameValuePair("begindate", df.format(beginDate)));
					parameters.add(new BasicNameValuePair("enddate", df.format(endDate)));
					request.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
					String text = postText(request, client);
					boolean success = JSON.parseObject(text).getBooleanValue("isSuccess");
					if (!success) {
						continue;
					}
					JSONArray items = JSON.parseObject(text).getJSONObject("pageMap").getJSONArray("result");
					if (items == null || items.size() == 0) {
						logger.warn("查询出短信详单为空!");
						continue;
					}
					for (int i = 0; i < items.size(); i++) {
						try {
							JSONObject json = items.getJSONObject(i);
							TelSmsDetail data = new TelSmsDetail(bizno);
							data.setSmsTel(json.getString("othernum"));
							data.setSmsTime(df2.parse(json.getString("smsdate") + json.getString("smstime")));
							data.setSmsType("2".equals(json.getString("smstype")) ? "发送" : "接受");
							list.add(data);
						} catch (Exception ignore) {
						}
					}
				} catch (Exception e) {
					logger.error("抓取短信详单错误", e);
				} finally {
					TimeUnit.MILLISECONDS.sleep(500);
				}
			}

		} catch (Exception e) {
			logger.error("抓取短信详单错误", e);
		}
		return list;
	}

	private List<TelNetDetail> getNetDetail(String bizno, CloseableHttpClient client) {
		List<TelNetDetail> list = new ArrayList<>();
		try {
			String urlFormat = "http://iservice.10010.com/e3/static/query/callFlow?_=%d&accessURL=http://iservice.10010.com/e4/query/basic/call_flow_iframe1.html";
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			DateFormat df2 = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
			Calendar c = Calendar.getInstance(Locale.CHINA);
			c.setTime(new Date());
			Date endDate = c.getTime();
			c.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH));
			Date beginDate = c.getTime();
			for (int x = 0; x < 6; x++, c.add(Calendar.DAY_OF_MONTH, -1), endDate = c.getTime(), c
					.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH)), beginDate = c.getTime()) {
				try {
					HttpPost request = new HttpPost(String.format(urlFormat, new Date().getTime()));
					List<NameValuePair> parameters = new ArrayList<>();
					parameters.add(new BasicNameValuePair("pageNo", "1"));
					parameters.add(new BasicNameValuePair("pageSize", "1000"));
					parameters.add(new BasicNameValuePair("beginDate", df.format(beginDate)));
					parameters.add(new BasicNameValuePair("endDate", df.format(endDate)));
					request.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
					String text = postText(request, client);
					boolean success = JSON.parseObject(text).getBooleanValue("issuccess");
					if (!success) {
						continue;
					}
					JSONArray items = JSON.parseObject(text).getJSONObject("result").getJSONArray("record");
					if (items == null || items.size() == 0) {
						logger.warn("查询出上网详单为空!");
						continue;
					}
					for (int i = 0; i < items.size(); i++) {
						try {
							JSONObject json = items.getJSONObject(i);
							TelNetDetail data = new TelNetDetail(bizno);
							data.setNetTime(df2.parse(json.getString("begindate") + json.getString("begintime")));
							data.setNetArea(json.getString("homeareaname"));
							list.add(data);
						} catch (Exception ignore) {
						}
					}
				} catch (Exception e) {
					logger.error("抓取短信详单错误", e);
				} finally {
					TimeUnit.MILLISECONDS.sleep(500);
				}
			}

		} catch (Exception e) {
			logger.error("抓取短信详单错误", e);
		}
		return list;
	}

	private List<TelBillDetail> getBillDetail(String bizno, CloseableHttpClient client) {
		List<TelBillDetail> list = new ArrayList<>();
		try {
			String urlFormat = "http://iservice.10010.com/e3/static/query/queryHistoryBill?_=%d&accessURL=http://iservice.10010.com/e4/skip.html?menuCode=000100020001&menuCode=000100020001&menuid=000100020001";
			DateFormat df = new SimpleDateFormat("yyyyMM");
			Calendar c = Calendar.getInstance(Locale.CHINA);
			c.setTime(new Date());
			c.add(Calendar.MONTH, -1);
			String month = df.format(c.getTime());
			for (int x = 0; x < 6; x++, c.add(Calendar.MONTH, -1), month = df.format(c.getTime())) {
				try {
					HttpPost request = new HttpPost(String.format(urlFormat, new Date().getTime()));
					List<NameValuePair> parameters = new ArrayList<>();
					parameters.add(new BasicNameValuePair("querytype", "0001"));
					parameters.add(new BasicNameValuePair("querycode", "0001"));
					parameters.add(new BasicNameValuePair("billdate", month));
					parameters.add(new BasicNameValuePair("flag", "1"));
					request.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));

					String text = postText(request, client);

					TelBillDetail data = new TelBillDetail(bizno);
					data.setCost(JSON.parseObject(text).getDoubleValue("payTotal"));
					data.setMonth(month);
					list.add(data);
				} catch (Exception e) {
					logger.error("抓取短信详单错误", e);
				} finally {
					TimeUnit.MILLISECONDS.sleep(500);
				}
			}
		} catch (Exception e) {
			logger.error("抓取短信详单错误", e);
		}
		return list;
	}

	private List<TelCallDetail> getCallDetail(String bizno, CloseableHttpClient client) {
		List<TelCallDetail> list = new ArrayList<>();
		try {
			String urlFormat = "http://iservice.10010.com/e3/static/query/callDetail?_=%d&accessURL=http://iservice.10010.com/e4/query/bill/call_dan-iframe.html?menuCode=000100030001&menuid=000100030001";
			DateFormat df = new SimpleDateFormat("yyyyMMdd");
			DateFormat df2 = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
			Pattern pattern = Pattern.compile("\\d+");
			Calendar c = Calendar.getInstance(Locale.CHINA);
			c.setTime(new Date());
			Date endDate = c.getTime();
			c.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH));
			Date beginDate = c.getTime();
			for (int x = 0; x < 6; x++, c.add(Calendar.DAY_OF_MONTH, -1), endDate = c.getTime(), c
					.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH)), beginDate = c.getTime()) {
				try {
					HttpPost request = new HttpPost(String.format(urlFormat, new Date().getTime()));
					List<NameValuePair> parameters = new ArrayList<>();
					parameters.add(new BasicNameValuePair("pageNo", "1"));
					parameters.add(new BasicNameValuePair("pageSize", "1000"));
					parameters.add(new BasicNameValuePair("beginDate", df.format(beginDate)));
					parameters.add(new BasicNameValuePair("endDate", df.format(endDate)));
					request.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
					String text = postText(request, client);

					// boolean success = JSON.parseObject(text).getBooleanValue("isSuccess");
					// if (!success) {
					// continue;
					// }

					JSONArray items = JSON.parseObject(text).getJSONObject("pageMap").getJSONArray("result");
					if (items == null || items.size() == 0) {
						logger.warn("查询出通话详单为空!");
						continue;
					}

					for (int i = 0; i < items.size(); i++) {
						try {
							JSONObject json = items.getJSONObject(i);
							TelCallDetail data = new TelCallDetail(bizno);
							data.setCallTel(json.getString("othernum"));
							data.setCallType(json.getString("calltypeName"));
							data.setCallTime(df2.parse(json.getString("calldate") + json.getString("calltime")));
							Matcher m = pattern.matcher(json.getString("calllonghour"));
							List<Integer> nums = new ArrayList<>();
							while (m.find()) {
								nums.add(Integer.parseInt(m.group()));
							}
							int seconds = 0;
							if (nums.size() == 3) {
								seconds = 3600 * nums.get(0) + 60 * nums.get(1) + nums.get(2);
							} else if (nums.size() == 2) {
								seconds = 60 * nums.get(0) + nums.get(1);
							} else {
								seconds = nums.get(0);
							}
							data.setDuration(seconds);
							data.setSelfArea(json.getString("homeareaName"));
							data.setCallArea(json.getString("calledhome"));
							list.add(data);
						} catch (Exception ignore) {
						}
					}

				} catch (Exception e) {
					logger.error("获取通话详单发生异常", e);
				} finally {
					TimeUnit.MILLISECONDS.sleep(500);
				}
			}
		} catch (Exception e) {
			logger.error("获取通话详单发生异常", e);
		}
		return list;
	}

}

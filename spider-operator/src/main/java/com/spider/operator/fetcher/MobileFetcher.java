package com.spider.operator.fetcher;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spider.common.consts.CrawlCode;
import com.spider.common.consts.CrawlStep;
import com.spider.common.fetcher.BaseFetcher;
import com.spider.common.utils.HttpClientManager;

@Component
public class MobileFetcher extends BaseFetcher {

	@Autowired
	private HttpClientManager httpClientManager;
	// @Autowired
	// private TelModelService persistService;

	@Override
	public Map<String, Object> login(Map<String, Object> params) throws Exception {
		Map<String, Object> map = new HashMap<>();

		CrawlStep step = (CrawlStep) params.get("step");
		String bizno = (String) params.get("bizno");
		String tel = (String) params.get("tel");

		if (step == CrawlStep.INIT) {// 检测是否需要验证码
			checkNeedVerifyCode(bizno, tel, map);
		} else if (step == CrawlStep.SENDCAPTCHA) {// 刷新验证码图片

		} else if (step == CrawlStep.SENDSMS) {// 发送登录短信
			sendSMS(bizno, tel, map);
		} else if (step == CrawlStep.VALIDCAPTCHA) {// 验证图片验证码

		} else if (step == CrawlStep.LOGIN) {// 登录
			String password = (String) params.get("password");
			String sms = (String) params.get("code");
			login(bizno, tel, password, sms, map);
		} else if (step == CrawlStep.SENDVALIDSMS) {// 发送身份验证短信

		} else if (step == CrawlStep.VALID) {// 进行身份验证

		}

		return map;

	}

	private void login(String bizno, String tel, String password, String sms, Map<String, Object> map)
			throws Exception {
		CloseableHttpClient client = httpClientManager.getHttpClient(bizno);
		String format = "https://login.10086.cn/login.htm?accountType=01&account=%s&password=%s&pwdType=01&smsPwd=%s&inputCode=&backUrl=http://shop.10086.cn/i/&rememberMe=0&channelID=12003&protocol=https:&timestamp=%d";
		String url = String.format(format, tel, password, sms, new Date().getTime());
		String text = getText(url, client);
		JSONObject json = JSON.parseObject(text);
		int code = json.getIntValue("code");
		if (code == 6002) {
			status(map, CrawlCode.ValidSMSError);
		} else if (code == 2036) {
			status(map, CrawlCode.UserPwdError);
		} else {
			status(map, CrawlCode.LoginSuccess);
		}
	}

	private void sendSMS(String bizno, String tel, Map<String, Object> map) throws Exception {
		CloseableHttpClient client = httpClientManager.getHttpClient(bizno);
		String url = "https://login.10086.cn/sendRandomCodeAction.action";
		Map<String, String> parameters = new HashMap<>();
		parameters.put("userName", tel);
		parameters.put("type", "01");
		parameters.put("channelID", "12003");
		String text = postText(url, parameters, client);
		if ("0".equals(text)) {
			status(map, CrawlCode.SMSSuccess);
		} else {
			status(map, CrawlCode.SMSFailed);
		}
	}

	private void checkNeedVerifyCode(String bizno, String tel, Map<String, Object> map) throws Exception {
		CloseableHttpClient client = httpClientManager.getHttpClient(bizno);
		String url = "https://login.10086.cn/captchazh.htm?type=12";
		String base64Img = getCaptchaImg(url, client);
		map.put("img", base64Img);
		url = String.format("https://login.10086.cn/needVerifyCode.htm?accountType=01&account=%s&timestamp=%d", tel,
				new Date().getTime());
		String text = getText(url, client);
		if (JSON.parseObject(text).getIntValue("needVerifyCode") == 1) {
			status(map, CrawlCode.NeedSMS);
		} else {
			status(map, CrawlCode.NotNeedSMS);
		}
	}

	@Async
	@Override
	public CrawlCode fetch(Map<String, Object> params) {
		try {
			String tel = (String) params.get("tel");
			String bizno = (String) params.get("bizno");
			// TimeUnit.SECONDS.sleep(3);
			logger.info("采集流水号:{} 手机号:{} 登录成功,开始异步采集", bizno, tel);
		} catch (Exception e) {
			logger.error("移动采集错误", e);
			return CrawlCode.FetchFailed;
		}
		return CrawlCode.FetchSuccess;
	}

}

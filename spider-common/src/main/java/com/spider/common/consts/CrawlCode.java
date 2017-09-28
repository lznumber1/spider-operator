package com.spider.common.consts;

public enum CrawlCode {

	// -------------------------------
	NeedCaptcha(100, "需要图片验证码"), //
	NotNeedCaptcha(101, "不需要图片验证码"), //
	NeedValid(102, "需要身份验证码"), //
	NeedSMS(103, "需要短信验证码"), //
	NotNeedSMS(104, "需要身份验证码"), //

	// -------------------------------
	FetchSuccess(200, "采集成功"), //
	LoginSuccess(201, "登录成功"), //
	ValidSuccess(202, "身份验证成功"), //

	CaptchaSuccess(203, "刷新验证码成功"), //
	SMSSuccess(204, "发送短信成功"), //
	ValidCapSucc(206, "验证码正确"), //
	ValidSMSSucc(207, "短信正确"), //
	SendValidSuccess(209, "发送身份验证短信成功"), //

	// -------------------------------
	FetchFailed(300, "采集失败"), //
	LoginFailed(301, "登录失败"), //
	ValidFailed(302, "身份验证失败"), //

	CaptchaFailed(303, "刷新验证码失败"), //
	SMSFailed(304, "发送短信失败"), //
	SMSOVER(305, "当日短信发送次数已达上限，请明日再试"), //
	ValidCapError(306, "验证码错误"), //
	ValidSMSError(307, "短信错误"), //
	SendValidFailed(309, "发送身份验证短信失败"), //

	// -------------------------------
	BadRequest(400, "请求错误"), //
	UserPwdError(401, "用户名或密码错误"), //

	// -------------------------------
	Unknown(500, "服务异常"),//

	;

	private int code;
	private String msg;

	private CrawlCode(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public int getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}

}

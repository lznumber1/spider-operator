package com.spider.model.tel;

import java.util.Date;

import com.spider.model.FetchResult;

public class TelSmsDetail extends FetchResult {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5673754383651721486L;

	private Date smsTime;
	private String smsTel;
	private String smsType;

	public TelSmsDetail() {
		super();
	}

	public TelSmsDetail(String bizno) {
		super(bizno);
	}

	public Date getSmsTime() {
		return smsTime;
	}

	public void setSmsTime(Date smsTime) {
		this.smsTime = smsTime;
	}

	public String getSmsTel() {
		return smsTel;
	}

	public void setSmsTel(String smsTel) {
		this.smsTel = smsTel;
	}

	public String getSmsType() {
		return smsType;
	}

	public void setSmsType(String smsType) {
		this.smsType = smsType;
	}

}

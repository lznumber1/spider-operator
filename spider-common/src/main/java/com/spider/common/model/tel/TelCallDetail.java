package com.spider.common.model.tel;

import java.util.Date;

import com.spider.common.model.FetchResult;

public class TelCallDetail extends FetchResult {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5083513718664867974L;

	private String callTel;
	private String callType;
	private Date callTime;
	private int duration;
	private String selfArea;
	private String callArea;

	public TelCallDetail() {
		super();
	}

	public TelCallDetail(String bizno) {
		super(bizno);
	}

	public String getCallTel() {
		return callTel;
	}

	public void setCallTel(String callTel) {
		this.callTel = callTel;
	}

	public String getCallType() {
		return callType;
	}

	public void setCallType(String callType) {
		this.callType = callType;
	}

	public Date getCallTime() {
		return callTime;
	}

	public void setCallTime(Date callTime) {
		this.callTime = callTime;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getSelfArea() {
		return selfArea;
	}

	public void setSelfArea(String selfArea) {
		this.selfArea = selfArea;
	}

	public String getCallArea() {
		return callArea;
	}

	public void setCallArea(String callArea) {
		this.callArea = callArea;
	}

}

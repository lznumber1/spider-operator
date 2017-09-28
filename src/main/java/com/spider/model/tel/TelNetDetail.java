package com.spider.model.tel;

import java.util.Date;

import com.spider.model.FetchResult;

public class TelNetDetail extends FetchResult {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7366882591679879006L;

	private Date netTime;
	private String netArea;

	public TelNetDetail() {
		super();
	}

	public TelNetDetail(String bizno) {
		super(bizno);
	}

	public Date getNetTime() {
		return netTime;
	}

	public void setNetTime(Date netTime) {
		this.netTime = netTime;
	}

	public String getNetArea() {
		return netArea;
	}

	public void setNetArea(String netArea) {
		this.netArea = netArea;
	}

}

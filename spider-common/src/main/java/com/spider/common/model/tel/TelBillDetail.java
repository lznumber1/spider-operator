package com.spider.common.model.tel;

import com.spider.common.model.FetchResult;

public class TelBillDetail extends FetchResult {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3866341678066415787L;

	private String month;
	private Double cost;

	public TelBillDetail() {
		super();
	}

	public TelBillDetail(String bizno) {
		super(bizno);
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public Double getCost() {
		return cost;
	}

	public void setCost(Double cost) {
		this.cost = cost;
	}

}

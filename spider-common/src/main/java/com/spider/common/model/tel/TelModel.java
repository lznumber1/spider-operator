package com.spider.common.model.tel;

import java.util.List;

import com.spider.common.model.FetchResult;

public class TelModel extends FetchResult {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4656316164772077446L;

	private TelBasic basic;
	private List<TelCallDetail> callDetails;
	private List<TelSmsDetail> smsDetails;
	private List<TelBillDetail> billDetails;
	private List<TelNetDetail> netDetails;

	public TelModel() {
		super();
	}

	public TelModel(String bizno) {
		super(bizno);
	}

	public String getBizno() {
		return bizno;
	}

	public void setBizno(String bizno) {
		this.bizno = bizno;
	}

	public TelBasic getBasic() {
		return basic;
	}

	public void setBasic(TelBasic basic) {
		this.basic = basic;
	}

	public List<TelCallDetail> getCallDetails() {
		return callDetails;
	}

	public void setCallDetails(List<TelCallDetail> callDetails) {
		this.callDetails = callDetails;
	}

	public List<TelSmsDetail> getSmsDetails() {
		return smsDetails;
	}

	public void setSmsDetails(List<TelSmsDetail> smsDetails) {
		this.smsDetails = smsDetails;
	}

	public List<TelNetDetail> getNetDetails() {
		return netDetails;
	}

	public void setNetDetails(List<TelNetDetail> netDetails) {
		this.netDetails = netDetails;
	}

	public List<TelBillDetail> getBillDetails() {
		return billDetails;
	}

	public void setBillDetails(List<TelBillDetail> billDetails) {
		this.billDetails = billDetails;
	}

}

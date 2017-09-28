package com.spider.model.tel;

import java.util.Date;

import com.spider.model.FetchResult;

public class TelBasic extends FetchResult {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8317542763344238921L;

	private boolean success = true;
	private String tel;
	private String name;
	// private String certType;
	private String certno;
	private String address;
	private Date joinDate;
	private String status;
	private Double leftAmount;
	// private Double leftFlow;
	// private Double leftVoice;
	private Double curMonthCost;

	public TelBasic() {
	}

	public TelBasic(String bizno) {
		super(bizno);
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	// public String getCertType() {
	// return certType;
	// }
	//
	// public void setCertType(String certType) {
	// this.certType = certType;
	// }

	public String getCertno() {
		return certno;
	}

	public void setCertno(String certno) {
		this.certno = certno;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Date getJoinDate() {
		return joinDate;
	}

	public void setJoinDate(Date joinDate) {
		this.joinDate = joinDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Double getLeftAmount() {
		return leftAmount;
	}

	public void setLeftAmount(Double leftAmount) {
		this.leftAmount = leftAmount;
	}

	// public Double getLeftFlow() {
	// return leftFlow;
	// }
	//
	// public void setLeftFlow(Double leftFlow) {
	// this.leftFlow = leftFlow;
	// }
	//
	// public Double getLeftVoice() {
	// return leftVoice;
	// }
	//
	// public void setLeftVoice(Double leftVoice) {
	// this.leftVoice = leftVoice;
	// }

	public Double getCurMonthCost() {
		return curMonthCost;
	}

	public void setCurMonthCost(Double curMonthCost) {
		this.curMonthCost = curMonthCost;
	}

}

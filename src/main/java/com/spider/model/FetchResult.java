package com.spider.model;

import java.io.Serializable;

public abstract class FetchResult implements Serializable {

	/**
	 * 
	 */
	protected static final long serialVersionUID = 4288079599653661391L;

	protected String bizno;

	protected FetchResult() {
		super();
	}

	protected FetchResult(String bizno) {
		super();
		this.bizno = bizno;
	}

	public String getBizno() {
		return bizno;
	}

	public void setBizno(String bizno) {
		this.bizno = bizno;
	}

}

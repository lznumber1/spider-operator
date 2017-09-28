package com.spider.common.fetcher;

import java.util.Map;

import com.spider.common.consts.CrawlCode;

public interface Fetcher {

	public CrawlCode login(Map<String, Object> params) throws Exception;

	public void fetch(Map<String, Object> params);

}

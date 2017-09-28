package com.spider.fetcher;

import java.util.Map;

import com.spider.consts.CrawlCode;

public interface Fetcher {

	public CrawlCode login(Map<String, Object> params) throws Exception;

	public void fetch(Map<String, Object> params);

}

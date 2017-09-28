/**
 * 
 */
package com.spider.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * @author zhe.li
 * @date 2017年7月16日
 */
public class BiznoGenerator {
	public static String gen() {
		return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + new Random().nextInt(999999);
	}

	public static void main(String[] args) {
		System.out.println(gen());
	}
}

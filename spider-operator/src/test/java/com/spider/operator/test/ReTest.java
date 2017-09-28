package com.spider.operator.test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReTest {
	public static void main(String[] args) throws Exception {
		String s = "2小时9分57秒";
		Pattern pattern = Pattern.compile("\\d+");
		Matcher m = pattern.matcher(s);
		List<Integer> nums = new ArrayList<>();
		while (m.find()) {
			nums.add(Integer.parseInt(m.group()));
		}
		int seconds = 0;
		if (nums.size() == 3) {
			seconds = 3600 * nums.get(0) + 60 * nums.get(1) + nums.get(2);
		} else if (nums.size() == 2) {
			seconds = 60 * nums.get(0) + nums.get(1);
		} else {
			seconds = nums.get(0);
		}
		System.out.println(seconds);
	}
}

package com.spider.operator.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {

	@RequestMapping("/auth")
	public String auth() {
		System.out.println("IndexController.auth()");
		return "auth";
	}

	// @RequestMapping("/success")
	// public String success() {
	// return "success";
	// }

}

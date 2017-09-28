package com.spider.operator;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@ServletComponentScan("com.spider.common.config")
@MapperScan("com.spider.common.mapper")
@ComponentScan({ "com.spider" })
public class App {
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
}

package com.spider.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class TaskExecutorConfig {

	@Bean
	@Primary
	public TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor bean = new ThreadPoolTaskExecutor();
		bean.setCorePoolSize(5);
		bean.setAllowCoreThreadTimeOut(true);
		bean.setDaemon(true);
		return bean;
	}

}

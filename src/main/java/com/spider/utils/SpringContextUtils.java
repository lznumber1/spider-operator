package com.spider.utils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class SpringContextUtils implements ApplicationContextAware {
	private static ApplicationContext context;

	public static void load(String... configLocations) {
		context = new ClassPathXmlApplicationContext(configLocations);
	}

	public static <T> T getBean(Class<T> requiredType) {
		return context.getBean(requiredType);
	}

	public static <T> T getBean(String name, Class<T> requiredType) {
		return context.getBean(name, requiredType);
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] getBeans(Class<T> requiredType) {
		Collection<T> collection = context.getBeansOfType(requiredType).values();

		if (collection.size() > 0) {
			T[] arr = (T[]) Array.newInstance(requiredType, collection.size());

			int x = 0;
			Iterator<T> it = collection.iterator();
			while (it.hasNext()) {
				arr[x++] = it.next();
			}

			return arr;
		}

		return null;
	}

	public static ApplicationContext getContext() {
		return context;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;
	}

}

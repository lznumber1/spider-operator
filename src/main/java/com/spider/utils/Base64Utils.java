/**
 * 
 */
package com.spider.utils;

import java.util.Date;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * @author zhe.li
 * @date 2017年7月16日
 */
public class Base64Utils {

	public static void main(String[] args) throws Exception {
		CloseableHttpClient client = HttpClients.createDefault();
		CloseableHttpResponse response = client.execute(new HttpGet(
				String.format("https://uac.10010.com/portal/Service/CreateImage?t=%d", new Date().getTime())));
		String strImg = imageToBase64(EntityUtils.toByteArray(response.getEntity()));
		System.out.println(strImg);
	}

	public static String imageToBase64(byte[] data) {
		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(data);
	}

	public static byte[] base64ToImage(String base64Img) {
		BASE64Decoder decoder = new BASE64Decoder();
		try {
			byte[] b = decoder.decodeBuffer(base64Img);
			for (int i = 0; i < b.length; ++i) {
				if (b[i] < 0) {
					b[i] += 256;
				}
			}
			return b;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

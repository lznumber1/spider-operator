package com.spider.common.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import com.spider.common.model.tel.TelSmsDetail;

public interface TelSmsDetailMapper {

	@Insert("<script>"//
			+ "insert into tel_sms_details(bizno,sms_time,sms_tel,sms_type) "//
			+ "values "//
			+ "<foreach collection =\"list\" item=\"item\" index= \"index\" separator =\",\"> "//
			+ "(#{item.bizno},#{item.smsTime},#{item.smsTel},#{item.smsType}) "//
			+ "</foreach> "//
			+ "</script>") //
	public void batchInsert(List<TelSmsDetail> smsDetails);

	@Select("select * from tel_sms_details where bizno=#{0}")
	@Results({ @Result(column = "sms_time", property = "smsTime"), @Result(column = "sms_tel", property = "smsTel"),
			@Result(column = "sms_type", property = "smsType") })
	public List<TelSmsDetail> query(String bizno);

}

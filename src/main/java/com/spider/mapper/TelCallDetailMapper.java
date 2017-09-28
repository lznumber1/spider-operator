package com.spider.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import com.spider.model.tel.TelCallDetail;

public interface TelCallDetailMapper {

	@Insert("<script>"//
			+ "insert into tel_call_details(bizno,call_tel,call_type,call_time,`duration`,self_area,call_area) "//
			+ "values "//
			+ "<foreach collection =\"list\" item=\"item\" index= \"index\" separator =\",\"> "//
			+ "(#{item.bizno},#{item.callTel},#{item.callType},#{item.callTime},#{item.duration},#{item.selfArea},#{item.callArea}) "//
			+ "</foreach> "//
			+ "</script>") //
	public void batchInsert(List<TelCallDetail> callDetails);

	@Select("select * from tel_call_details where bizno=#{0}")
	@Results({ @Result(column = "call_tel", property = "callTel"), @Result(column = "call_type", property = "callType"),
			@Result(column = "call_time", property = "callTime"), @Result(column = "self_area", property = "selfArea"),
			@Result(column = "call_area", property = "callArea") })
	public List<TelCallDetail> query(String bizno);

}

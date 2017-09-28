package com.spider.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import com.spider.model.tel.TelNetDetail;

public interface TelNetDetailMapper {

	@Insert("<script>"//
			+ "insert into tel_net_details(bizno,net_time,net_area) "//
			+ "values "//
			+ "<foreach collection =\"list\" item=\"item\" index= \"index\" separator =\",\"> "//
			+ "(#{item.bizno},#{item.netTime},#{item.netArea}) "//
			+ "</foreach> "//
			+ "</script>") //
	public void batchInsert(List<TelNetDetail> netDetails);

	@Select("select * from tel_net_details where bizno=#{0}")
	@Results({ @Result(column = "net_time", property = "netTime"), @Result(column = "net_area", property = "netArea") })
	public List<TelNetDetail> query(String bizno);

}

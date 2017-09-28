package com.spider.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import com.spider.model.tel.TelBasic;

public interface TelBasicMapper {

	@Insert("insert into tel_basic(bizno,`success`,tel,`name`,certno,address,join_date,`status`,left_amount,cur_month_cost) values(#{bizno},#{success},#{tel},#{name},#{certno},#{address},#{joinDate},#{status},#{leftAmount},#{curMonthCost})")
	public void insert(TelBasic telBasic);

	@Select("select * from tel_basic where tel=#{0} order by created_at desc limit 1")
	@Results({ @Result(column = "join_date", property = "joinDate"),
			@Result(column = "left_amount", property = "leftAmount"),
			@Result(column = "cur_month_cost", property = "curMonthCost") })
	public TelBasic getLasted(String tel);

}

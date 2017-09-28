package com.spider.common.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import com.spider.common.model.tel.TelBillDetail;

public interface TelBillDetailMapper {

	@Insert("<script>"//
			+ "insert into tel_bill_details(bizno,`month`,`cost`) "//
			+ "values "//
			+ "<foreach collection =\"list\" item=\"item\" index= \"index\" separator =\",\"> "//
			+ "(#{item.bizno},#{item.month},#{item.cost}) "//
			+ "</foreach> "//
			+ "</script>") //
	public void batchInsert(List<TelBillDetail> billDetails);

	@Select("select * from tel_bill_details where bizno=#{0}")
	public List<TelBillDetail> query(String bizno);

}

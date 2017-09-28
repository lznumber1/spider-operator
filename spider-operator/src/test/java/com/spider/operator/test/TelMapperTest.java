package com.spider.operator.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spider.common.mapper.TelBasicMapper;
import com.spider.common.mapper.TelBillDetailMapper;
import com.spider.common.mapper.TelCallDetailMapper;
import com.spider.common.mapper.TelNetDetailMapper;
import com.spider.common.mapper.TelSmsDetailMapper;
import com.spider.common.model.tel.TelBasic;
import com.spider.common.model.tel.TelBillDetail;
import com.spider.common.model.tel.TelCallDetail;
import com.spider.common.model.tel.TelNetDetail;
import com.spider.common.model.tel.TelSmsDetail;

@SpringBootApplication
@ServletComponentScan("com.spider.config")
@ComponentScan({ "com.spider" })
@MapperScan("com.spider.mapper")
@RestController
public class TelMapperTest {

	@Autowired
	private TelBasicMapper telBasicMapper;
	@Autowired
	private TelCallDetailMapper telCallDetailMapper;
	@Autowired
	private TelBillDetailMapper telBillDetailMapper;
	@Autowired
	private TelNetDetailMapper telNetDetailMapper;
	@Autowired
	private TelSmsDetailMapper telSmsDetailMapper;

	@RequestMapping("/run")
	public void run() {
		try {
			String bizno = "2017072114232711test";
			TelBasic telBasic = new TelBasic(bizno);
			telBasic.setTel("12345678910");
			telBasic.setAddress("北京市朝阳区");
			telBasic.setCertno("xxxxxxxxxxx");
			telBasic.setCurMonthCost(34.03);
			telBasic.setJoinDate(new Date());
			telBasic.setLeftAmount(43.23);
			telBasic.setName("张三");
			telBasic.setSuccess(true);
			telBasicMapper.insert(telBasic);

			List<TelCallDetail> callDetails = new ArrayList<>();
			for (int i = 1; i < 10; i++) {
				TelCallDetail callDetail = new TelCallDetail(bizno);
				callDetail.setCallTel(i + "" + i + "" + i + "" + i);
				callDetail.setCallArea("上海");
				callDetail.setCallTime(new Date());
				callDetail.setCallType("被叫");
				callDetail.setDuration(i * 5);
				callDetail.setSelfArea("北京");
				callDetails.add(callDetail);
			}
			telCallDetailMapper.batchInsert(callDetails);

			List<TelBillDetail> billDetails = new ArrayList<>();
			for (int i = 1; i < 7; i++) {
				TelBillDetail billDetail = new TelBillDetail(bizno);
				billDetail.setCost(i * 5.3);
				billDetail.setMonth("20170" + i);
				billDetails.add(billDetail);
			}
			telBillDetailMapper.batchInsert(billDetails);

			List<TelNetDetail> netDetails = new ArrayList<>();
			for (int i = 1; i < 30; i++) {
				TelNetDetail data = new TelNetDetail(bizno);
				data.setNetArea("北京");
				data.setNetTime(new Date());
				netDetails.add(data);
			}
			telNetDetailMapper.batchInsert(netDetails);

			List<TelSmsDetail> smsDetails = new ArrayList<>();
			for (int i = 0; i < 20; i++) {
				TelSmsDetail data = new TelSmsDetail(bizno);
				data.setSmsTel("2424245335");
				data.setSmsTime(new Date());
				data.setSmsType("发送");
				smsDetails.add(data);
			}
			telSmsDetailMapper.batchInsert(smsDetails);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(TelMapperTest.class, args);
	}

}

package com.spider.operator.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.spider.common.mapper.TelBasicMapper;
import com.spider.common.mapper.TelBillDetailMapper;
import com.spider.common.mapper.TelCallDetailMapper;
import com.spider.common.mapper.TelNetDetailMapper;
import com.spider.common.mapper.TelSmsDetailMapper;
import com.spider.common.model.tel.TelBasic;
import com.spider.common.model.tel.TelBillDetail;
import com.spider.common.model.tel.TelCallDetail;
import com.spider.common.model.tel.TelModel;
import com.spider.common.model.tel.TelNetDetail;
import com.spider.common.model.tel.TelSmsDetail;

@Component
public class TelModelService {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private TaskExecutor taskExecutor;

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

	public void persist(final TelModel telModel) {
		if (telModel == null) {
			logger.warn("telModel can not be null");
			return;
		}
		taskExecutor.execute(() -> {
			if (telModel.getBasic() != null) {
				try {
					telBasicMapper.insert(telModel.getBasic());
				} catch (Exception e) {
					logger.error("持久化错误,bizno:{}", telModel.getBizno());
					logger.error("持久化基本信息错误", e);
				}
			}
			if (telModel.getCallDetails() != null) {
				try {
					telCallDetailMapper.batchInsert(telModel.getCallDetails());
				} catch (Exception e) {
					logger.error("持久化错误,bizno:{}", telModel.getBizno());
					logger.error("持久化通话记录错误", e);
				}
			}
			if (telModel.getBillDetails() != null) {
				try {
					telBillDetailMapper.batchInsert(telModel.getBillDetails());
				} catch (Exception e) {
					logger.error("持久化错误,bizno:{}", telModel.getBizno());
					logger.error("持久化历史错误", e);
				}
			}
			if (telModel.getNetDetails() != null) {
				try {
					telNetDetailMapper.batchInsert(telModel.getNetDetails());
				} catch (Exception e) {
					logger.error("持久化错误,bizno:{}", telModel.getBizno());
					logger.error("持久化上网记录错误", e);
				}
			}
			if (telModel.getSmsDetails() != null) {
				try {
					telSmsDetailMapper.batchInsert(telModel.getSmsDetails());
				} catch (Exception e) {
					logger.error("持久化错误,bizno:{}", telModel.getBizno());
					logger.error("持久化短信记录错误", e);
				}
			}
			logger.info("持久化====>>>>bizno:{}", telModel.getBizno());
		});
	}

	public TelModel query(String tel) {
		TelBasic telBasic = telBasicMapper.getLasted(tel);
		if (telBasic == null) {
			return null;
		}
		String bizno = telBasic.getBizno();

		TelModel telModel = new TelModel(bizno);
		telModel.setBasic(telBasic);

		List<TelCallDetail> callDetails = telCallDetailMapper.query(bizno);
		telModel.setCallDetails(callDetails);
		List<TelBillDetail> billDetails = telBillDetailMapper.query(bizno);
		telModel.setBillDetails(billDetails);
		List<TelNetDetail> netDetails = telNetDetailMapper.query(bizno);
		telModel.setNetDetails(netDetails);
		List<TelSmsDetail> smsDetails = telSmsDetailMapper.query(bizno);
		telModel.setSmsDetails(smsDetails);

		return telModel;
	}

	// public TelModel queryByBizno(String bizno) {
	// TelModel telModel = new TelModel(bizno);
	// return telModel;
	// }

}

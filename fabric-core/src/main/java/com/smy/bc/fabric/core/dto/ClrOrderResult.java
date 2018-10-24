package com.smy.bc.fabric.core.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ClrOrderResult implements Serializable{
    private static final long serialVersionUID = -7714376151711389500L;
    /**
     *对账ID
     */
    private String id;
    /**
     * 对账用的订单号
     */
    private String orderId;
    /**
     * 账务日期yyyyMMdd
     */
    private int accountDate;
    /**
     *org2 txId
     */
    private String nccTxId;
    /**
     * cts txId
     */
    private String ctsTxId;
    /**
     *对账状态
     */
    private String status;
    /**
     * 异常码
     */
    private String errCode;
    /**
     * 异常描述
     */
    private String errMsg;
    /**
     * 扩展信息
     */
    private String extMsg;
    /**
     * 对账创建时间
     */
    private Date createDatetime;
    /**
     * 对账更新时间
     */
    private Date updateDatetime;

}

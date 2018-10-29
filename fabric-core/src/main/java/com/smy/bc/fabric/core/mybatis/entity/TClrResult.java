package com.smy.bc.fabric.core.mybatis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 对账结果表
 * </p>
 *
 * @author zhaofeng
 * @since 2018-10-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TClrResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 系统码
     */
    private String sysCode;

    /**
     * 订单账务日期
     */
    private Integer actDate;

    /**
     * 对账次数
     */
    private Integer clrRetryTimes;

    /**
     * 对账关闭时间
     */
    private LocalDateTime clrCloseDatetime;

    /**
     * 对账完成状态 未完成;不存在;平;不平
     */
    private String clrStatus;

    /**
     * nccTxId
     */
    private String nccTxId;

    /**
     * ctsTxId
     */
    private String ctsTxId;

    /**
     * 对账不平code码
     */
    private String clrCode;

    /**
     * 对账不平结果描述
     */
    private String clrMsg;

    /**
     * 对账不平信息json明细
     */
    private String clrJsonMsg;

    /**
     * 区块号
     */
    private Integer blockNumber;

    /**
     * 当前hash
     */
    private String blockHash;

    /**
     * 前hash
     */
    private String blockPreHash;

    /**
     * 交易id
     */
    private String txId;

    /**
     * 对账结束时间
     */
    private LocalDateTime clrEndDatetime;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 创建日期时间
     */
    private LocalDateTime createDatetime;

    /**
     * 更新日期时间
     */
    private LocalDateTime updateDatetime;


}

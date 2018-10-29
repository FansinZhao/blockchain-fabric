package com.smy.bc.fabric.core.mybatis.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 借款订单表
 * </p>
 *
 * @author zhaofeng
 * @since 2018-10-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TClrOrder implements Serializable {

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
     * 客户号
     */
    private String custNo;

    /**
     * 银行卡号
     */
    private String bankCardNo;

    /**
     * 资金方编码
     */
    private String captitalCode;

    /**
     * 子资金方编码
     */
    private String captitalSubCode;

    /**
     * 借款金额
     */
    private BigDecimal transAmt;

    /**
     * 借款期次
     */
    private Integer term;

    /**
     * 状态
     */
    private String status;

    /**
     * 订单账务日期
     */
    private Integer actDate;

    /**
     * 订单日期
     */
    private LocalDateTime orderDatetime;

    /**
     * 扩展信息
     */
    private String extMsg;

    /**
     * 备注
     */
    private String remark;

    /**
     * 存证状态
     */
    private String blockStatus;

    /**
     * 存证异常码
     */
    private String blockErrCode;

    /**
     * 存证异常描述
     */
    private String blockErrMsg;

    /**
     * 区块号
     */
    private Long blockNumber;

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
     * 创建日期时间
     */
    private LocalDateTime createDatetime;

    /**
     * 更新日期时间
     */
    private LocalDateTime updateDatetime;


}

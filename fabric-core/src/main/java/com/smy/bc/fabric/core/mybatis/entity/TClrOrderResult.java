package com.smy.bc.fabric.core.mybatis.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author zhaofeng
 * @since 2018-10-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TClrOrderResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 账务日期yyyyMMdd
     */
    private LocalDate accountDate;

    /**
     * txId
     */
    private String nccTxId;

    /**
     * txId
     */
    private String ctsTxId;

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
     * 账务日期
     */
    private Integer actDate;

    /**
     * 扩展信息
     */
    private String extMsg;

    /**
     * 备注
     */
    private String remark;

    /**
     * 异常码
     */
    private String errCode;

    /**
     * 异常描述
     */
    private String errMsg;

    /**
     * 创建日期时间
     */
    private LocalDateTime createDatetime;

    /**
     * 更新日期时间
     */
    private LocalDateTime updateDatetime;


}

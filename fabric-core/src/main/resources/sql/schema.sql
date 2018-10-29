-- use mysql;
-- GRANT ALL PRIVILEGES ON *.* TO 'root'@'%'IDENTIFIED BY 'root' WITH GRANT OPTION;
-- FLUSH PRIVILEGES

SET NAMES utf8;

CREATE DATABASE `smy_bc` CHARACTER SET 'utf8' COLLATE 'utf8_general_ci';
use smy_bc;
-- ----------------------------
-- Table structure for t_clr_order
-- ----------------------------
DROP TABLE IF EXISTS `t_clr_order`;
CREATE TABLE `t_clr_order` (
  `id` bigint(19) NOT NULL AUTO_INCREMENT,
  `order_id` varchar(20) NOT NULL COMMENT '订单id',
  `sys_code` varchar(20) DEFAULT NULL COMMENT '系统码',
  `cust_no` varchar(20) DEFAULT NULL COMMENT '客户号',
  `bank_card_no` varchar(25) DEFAULT NULL COMMENT '银行卡号',
  `captital_code` varchar(20) DEFAULT NULL COMMENT '资金方编码',
  `captital_sub_code` varchar(20) DEFAULT NULL COMMENT '子资金方编码',
  `trans_amt` decimal(20,2) DEFAULT NULL COMMENT '借款金额',
  `term` int(2) DEFAULT NULL COMMENT '借款期次',
  `status` varchar(20) DEFAULT NULL COMMENT '状态',
  `act_date` int(8) DEFAULT NULL COMMENT '订单账务日期',
  `order_datetime` timestamp NULL DEFAULT NULL COMMENT '订单日期',
  `ext_msg` varchar(256) DEFAULT NULL COMMENT '扩展信息',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  `block_status` varchar(20) DEFAULT NULL COMMENT '存证状态',
  `block_err_code` varchar(20) DEFAULT NULL COMMENT '存证异常码',
  `block_err_msg` varchar(256) DEFAULT NULL COMMENT '存证异常描述',
  `block_number` int(10) DEFAULT NULL COMMENT '区块号',
  `block_hash` varchar(128) DEFAULT NULL COMMENT '当前hash',
  `block_pre_hash` varchar(128) DEFAULT NULL COMMENT '前hash',
  `tx_id` varchar(128) DEFAULT NULL COMMENT '交易id',
  `create_datetime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期时间',
  `update_datetime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ORDER_ID_SYS_CODE` (`order_id`,`sys_code`),
  KEY `INDEX_CREATE_DATETIME` (`create_datetime`),
  KEY `INDEX_UPDATE_DATETIME_BLOCK_STATUS` (`update_datetime`,`block_status`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8 COMMENT='借款订单表';

DROP TABLE IF EXISTS `t_clr_result`;
CREATE TABLE `t_clr_result` (
  `id` BIGINT(19) NOT NULL AUTO_INCREMENT,
  `order_id` VARCHAR(20) NOT NULL COMMENT '订单id',
  `sys_code` VARCHAR(20) DEFAULT NULL COMMENT '系统码',
  `act_date` INT(8) DEFAULT NULL COMMENT '订单账务日期',
  `clr_retry_times` INT(8) DEFAULT 0 COMMENT '对账次数',
  `clr_close_datetime` TIMESTAMP NULL DEFAULT NULL COMMENT '对账关闭时间',
  `clr_status` VARCHAR(20) DEFAULT NULL COMMENT '对账完成状态 未完成;不存在;平;不平',
  `ncc_tx_id` VARCHAR(64) DEFAULT NULL COMMENT 'nccTxId',
  `cts_tx_id` VARCHAR(64) DEFAULT NULL COMMENT 'ctsTxId',
  `clr_code`  VARCHAR(20) DEFAULT NULL COMMENT '对账不平code码',
  `clr_msg`  VARCHAR(128) DEFAULT NULL COMMENT '对账不平结果描述',
  `clr_json_msg`  VARCHAR(128) DEFAULT NULL COMMENT '对账不平信息json明细',
  `block_number` INT(10) DEFAULT NULL COMMENT '区块号',
  `block_hash` VARCHAR(128) DEFAULT NULL COMMENT '当前hash',
  `block_pre_hash` VARCHAR(128) DEFAULT NULL COMMENT '前hash',
  `tx_id` VARCHAR(128) DEFAULT NULL COMMENT '交易id',
  `clr_end_datetime` TIMESTAMP NULL DEFAULT NULL COMMENT '对账结束时间',
  `version` INT(8) DEFAULT 0 COMMENT '版本号',
  `create_datetime` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期时间',
  `update_datetime` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ORDER_ID_SYS_CODE` (`order_id`,`sys_code`),
  KEY `INDEX_UPDATE_DATETIME_CLR_STATUS` (`update_datetime`,`clr_status` ),
  KEY `INDEX_CREATE_DATETIME_CLRSTATUS` (`create_datetime`,`clr_status` )
) ENGINE=INNODB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8 COMMENT='对账结果表';


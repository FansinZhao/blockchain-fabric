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
CREATE TABLE smy_bc.`t_clr_order` (
  `id` bigint(19) NOT NULL AUTO_INCREMENT,
  `sys_code` varchar(20) DEFAULT NULL COMMENT '系统码',
  `order_id` varchar(20) NOT NULL COMMENT '订单id',
  `cust_no` varchar(20) DEFAULT NULL COMMENT '客户号',
  `bank_card_no` varchar(25) DEFAULT NULL COMMENT '银行卡号',
  `captital_code` varchar(20) DEFAULT NULL COMMENT '资金方编码',
  `captital_sub_code` varchar(20) DEFAULT NULL COMMENT '子资金方编码',
  `trans_amt` decimal(20,2) DEFAULT NULL COMMENT '借款金额',
  `term` int(2) DEFAULT NULL COMMENT '借款期次',
  `status` varchar(20) DEFAULT NULL COMMENT '状态',
  `act_date` int(8) DEFAULT NULL COMMENT '账务日期',
  `ext_msg` varchar(256) DEFAULT NULL COMMENT '扩展信息',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  `block_status` varchar(20) DEFAULT NULL COMMENT '存证状态',
  `block_err_code` varchar(20) DEFAULT NULL COMMENT '存证异常码',
  `block_err_msg` varchar(256) DEFAULT NULL COMMENT '存证异常描述',
  `block_number` bigint(19) DEFAULT NULL COMMENT '区块号',
  `block_hash` varchar(128) DEFAULT NULL COMMENT '当前hash',
  `block_pre_hash` varchar(128) DEFAULT NULL COMMENT '前hash',
  `tx_id` varchar(128) DEFAULT NULL COMMENT '交易id',
  `create_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期时间',
  `update_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create unique index INDEX_ORDER_SYSCODE
  on t_clr_order (order_id,sys_code);

create index INDEX_ORDER_ID
  on t_clr_order (order_id);

create index INDEX_BLOCK_HASH
  on t_clr_order (block_hash);

-- ----------------------------
-- Table structure for t_clr_order_result
-- ----------------------------
DROP TABLE IF EXISTS `t_clr_order_result`;
CREATE TABLE smy_bc.`t_clr_order_result` (
  `id` bigint(19) NOT NULL AUTO_INCREMENT,
  `order_id` varchar(20) NOT NULL COMMENT '订单id',
  `account_date` date NOT NULL COMMENT '账务日期yyyyMMdd',
  `ncc_tx_id` varchar(64) DEFAULT NULL COMMENT 'txId',
  `cts_tx_id` varchar(64) DEFAULT NULL COMMENT 'txId',
  `cust_no` varchar(20) DEFAULT NULL COMMENT '客户号',
  `bank_card_no` varchar(25) DEFAULT NULL COMMENT '银行卡号',
  `captital_code` varchar(20) DEFAULT NULL COMMENT '资金方编码',
  `captital_sub_code` varchar(20) DEFAULT NULL COMMENT '子资金方编码',
  `trans_amt` decimal(20,2) DEFAULT NULL COMMENT '借款金额',
  `term` int(2) DEFAULT NULL COMMENT '借款期次',
  `status` varchar(20) DEFAULT NULL COMMENT '状态',
  `act_date` int(8) DEFAULT NULL COMMENT '账务日期',
  `ext_msg` varchar(256) DEFAULT NULL COMMENT '扩展信息',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  `err_code` varchar(20) DEFAULT NULL COMMENT '异常码',
  `err_msg` varchar(256) DEFAULT NULL COMMENT '异常描述',
  `create_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期时间',
  `update_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create unique index INDEX_ORDER_ID
  on t_clr_order_result (order_id);

create index INDEX_NCC_TX_ID
  on t_clr_order_result (ncc_tx_id);

create index INDEX_CTS_TX_ID
  on t_clr_order_result (cts_tx_id);

create index INDEX_CUST_BANK
  on t_clr_order_result (cust_no,bank_card_no);


package com.smy.bc.fabric.api.controller;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.smy.bc.fabric.core.FabricTemplate;
import com.smy.bc.fabric.core.mybatis.entity.TClrOrder;
import com.smy.bc.fabric.core.mybatis.entity.TClrResult;
import com.smy.bc.fabric.core.mybatis.service.ITClrOrderService;
import com.smy.bc.fabric.core.mybatis.service.ITClrResultService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.protos.common.Common;
import org.hyperledger.fabric.protos.peer.PeerEvents;
import org.hyperledger.fabric.sdk.BlockInfo;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>Title: FabricApiController</p>
 * <p>Description: </p>
 *
 * @author zhaofeng
 * @version 1.0
 * @date 18-9-12
 */
@Api(value = "/")
@RestController
@RequestMapping("/")
@Slf4j
public class FabricApiController {

    @Resource
    private FabricTemplate fabricTemplate;


    @Resource
    private ITClrOrderService orderService;

    @Resource
    private ITClrResultService resultService;


    @ApiOperation("org2/cts 保存数据到bc")
    @RequestMapping(value = "save", method = RequestMethod.POST)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "order", value = "org2/cts交易信息", paramType = "body", defaultValue = "{\"sysCode\":\"CTS\",\"custNo\":\"a123456789\",\"orderId\":\"O0123456\",\"bankCardNo\":\"123456789\",\"transAmt\":123.45,}", required = true),
            @ApiImplicitParam(name = "ccName", value = "调用智能合约", paramType = "query", defaultValue = "org1", required = true)
    })
    public String save2BlockChain(@RequestBody TClrOrder order, String ccName) {


        //1 查询交易是否已入库

        QueryWrapper<TClrOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", order.getOrderId());
        queryWrapper.eq("sys_code", order.getSysCode());
        queryWrapper.orderByDesc("update_datetime");
        TClrOrder clrOrder = orderService.getOne(queryWrapper);

        if (clrOrder != null) {
            //2 查询对账表
            //直接查询db
            QueryWrapper<TClrResult> resultQW = new QueryWrapper<>();
            resultQW.eq("order_id", order.getOrderId());
            resultQW.orderByDesc("update_datetime");
            TClrResult orderResult = resultService.getOne(resultQW);

            if (orderResult != null) {
                log.info("查询DB数据id:{} orderId:{} result：{}", orderResult.getId(), orderResult.getOrderId(), orderResult);
                //是否短路？
            }

            //直接查询智能合约
            try {
                String result = fabricTemplate.queryByChaincode("account", "query", order.getOrderId());
                if (StrUtil.isNotBlank(result)) {
                    //保存到db对账状态
                    TClrResult orderResult1 = new Gson().newBuilder().create().fromJson(result, TClrResult.class);
                    boolean save = resultService.save(orderResult1);
                    log.info("保存结果:{} {}",save,orderResult1.getId());
                    return result;
                }

            } catch (ProposalException e) {
                e.printStackTrace();
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
            //3触发异步对账接口
            try {
                String txId = fabricTemplate.invokeBlockChain("account", "clrCheck", order.getOrderId(),"org1");
                log.info("txId:{}",txId);
                //db不用保存对账状态
            } catch (ProposalException e) {
                e.printStackTrace();
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        } else {
            //2 调用智能合约保存数据
            log.info("开始异步写入区块链信息:{}", JSON.toJSONString(order));
            String txId = null;

            try {
                txId = fabricTemplate.invokeBlockChain(ccName, "create", JSON.toJSONString(order));
                log.info("txId:{}",txId);
            } catch (ProposalException e) {
                e.printStackTrace();
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
            //查询结果
            try {
                int time = 3;
                while(time-->0){
                    log.info("等待3s");
                    Thread.sleep(3000);

                    String result = fabricTemplate.queryByChaincode(ccName, "query", order.getOrderId(),order.getSysCode());
                    if (StrUtil.isNotBlank(result)) {
                        //查询区块详细信息
                        BlockInfo blockInfo = fabricTemplate.queryBlockByTransactionID(txId);
                        //保存到db对账状态
                        TClrOrder tClrOrder = new Gson().newBuilder().create().fromJson(result, TClrOrder.class);
                        tClrOrder.setTxId(txId);
                        tClrOrder.setBlockHash(HexUtil.encodeHexStr(blockInfo.getDataHash()));
                        tClrOrder.setBlockNumber(blockInfo.getBlockNumber());
                        tClrOrder.setBlockPreHash(HexUtil.encodeHexStr(blockInfo.getPreviousHash()));
                        boolean save = orderService.save(tClrOrder);
                        log.info("保存结果:{} {}",save,tClrOrder.getId());
                        //print 详细信息
                        queryBlockByTransactionID(txId);

                        return "保存区块链成功！"+result;
                    }
                }
                return "未在区块链上查询到信息！";
            } catch (ProposalException e) {
                e.printStackTrace();
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        return "调用完成,请稍后查询！";
    }

    @ApiOperation("根据txID查询区块信息")
    @RequestMapping(value = "queryBlockByTransactionID", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "txId", value = "transactionID", paramType = "query", required = true)
    })
    public String queryBlockByTransactionID(String txId) {

        StringBuilder sb = new StringBuilder("BlockInfo:");

        try {
            BlockInfo blockInfo = fabricTemplate.queryBlockByTransactionID(txId);

            Common.Block block = blockInfo.getBlock();
            String blockJson = new Gson().newBuilder().create().toJson(block);
            long blockNumber = blockInfo.getBlockNumber();
            String channelId = blockInfo.getChannelId();
            PeerEvents.FilteredBlock filteredBlock = blockInfo.getFilteredBlock();
            String filterBlockJson = new Gson().newBuilder().create().toJson(filteredBlock);
            byte[] dataHash = blockInfo.getDataHash();
            byte[] previousHash = blockInfo.getPreviousHash();
            int transactionCount = blockInfo.getTransactionCount();
            byte[] transActionsMetaData = blockInfo.getTransActionsMetaData();

            sb.append("\n").append("Block:").append(blockJson)
                    .append("\n").append("BlockNumber:").append(blockNumber)
                    .append("\n").append("ChannelId:").append(channelId)
                    .append("\n").append("FilteBlockJson:").append(filterBlockJson)
                    .append("\n").append("DataHash:").append(new String(dataHash))
                    .append("\n").append("PreviousHash:").append(new String(previousHash))
                    .append("\n").append("TransactionCount:").append(transactionCount)
                    .append("\n").append("TransActionsMetaData:").append(new String(transActionsMetaData));

            log.info("区块信息：{}", sb.toString());

        } catch (ProposalException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }



    @ApiOperation(value = "启动事件监听")
    @RequestMapping(value = "chaincodeEvent", method = RequestMethod.GET)
    public String chaincodeEvent() {
        String s = fabricTemplate.registerChaincodeEventListener();
        return "启动区块链事件监听" + s;
    }

    @ApiOperation(value = "解绑事件监听")
    @ApiImplicitParam(name = "handle", value = "区块事件监听handle", paramType = "query", required = true)
    @RequestMapping(value = "unchaincodeEvent", method = RequestMethod.POST)
    public String unchaincodeEvent(String handle) {
        boolean s = fabricTemplate.unregisterChaincodeEventListener(handle);
        return handle + " 事件监听解绑:" + s;
    }


    @ApiOperation(value = "启动区块监听")
    @RequestMapping(value = "blockEvent", method = RequestMethod.GET)
    public String blockEvent() {
        String s = fabricTemplate.registerBlockListener();
        return "启动区块事件监听" + s;
    }

    @ApiOperation(value = "解绑事件监听")
    @ApiImplicitParam(name = "handle", value = "区块监听handle", paramType = "query", required = true)
    @RequestMapping(value = "unblockEvent", method = RequestMethod.POST)
    public String unblockEvent(String handle) {
        boolean s = fabricTemplate.unregisterBlockListener(handle);
        return handle + " 事件监听解绑:" + s;
    }


    @ApiOperation(value = "查询智能合约", notes = "使用方式 list|query args[]|history args[]")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "chaincodeName", value = "智能合约名称", defaultValue = "account,org1,org2", paramType = "query", dataType = "string", required = true),
            @ApiImplicitParam(name = "funcName", value = "查询接口", defaultValue = "list,query,history", paramType = "query", dataType = "string", required = true),
            @ApiImplicitParam(name = "args", value = "查询参数", example = "key1", paramType = "query", dataType = "string", allowMultiple = true)
    })
    @RequestMapping(value = "query", method = RequestMethod.POST)
    public String query(String chaincodeName, String funcName, String... args) {

        if (args == null) {
            args = new String[]{};
        }

        try {
            return fabricTemplate.queryByChaincode(chaincodeName, funcName, args);
        } catch (ProposalException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        return "query 异常!";
    }

    @RequestMapping()
    public String index() {
        return "fabric-api启动成功!";
    }

    @ApiOperation(value = "调用智能合约", notes = "使用方式 create args[],update args[],init args[],invoke args[]")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "chaincodeName", value = "智能合约名称", defaultValue = "account,org1,org2", paramType = "query", dataType = "string", required = true),
            @ApiImplicitParam(name = "funcName", value = "智能合约方法", defaultValue = "create,update,init,invoke", paramType = "query", dataType = "string", required = true),
            @ApiImplicitParam(name = "args", value = "调用参数", example = "[\"key1\",\"value\"]", paramType = "query", dataType = "string", allowMultiple = true)
    })
    @RequestMapping(value = "invoke", method = RequestMethod.POST)
    public String invoke(String chaincodeName, String funcName, String... args) {
        try {
            fabricTemplate.invokeBlockChain(chaincodeName, funcName, args);
        } catch (ProposalException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        return funcName + " 调用完成";
    }

}

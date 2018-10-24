package main

import (
	"encoding/json"
	"fmt"
	. "github.com/hyperledger/fabric/chaincode/common"
	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
	"sort"
	"time"
)

var log = shim.NewLogger("org1")

type SmartContract struct {
}

func (s *SmartContract) Init(APIstub shim.ChaincodeStubInterface) pb.Response {
	log.SetLevel(shim.LogDebug)
	log.Infof("Init Success!")

	return shim.Success(nil)
}

func (s *SmartContract) Invoke(APIstub shim.ChaincodeStubInterface) pb.Response {

	function, args := APIstub.GetFunctionAndParameters()
	log.Infof("调用智能合约！%s %s", function, args)

	if function == "query" {
		return s.query(APIstub, args)
	} else if function == "create" {
		return s.create(APIstub, args)
	} else if function == "triggerCheck" {
		return s.triggerCheck(APIstub, args)
	} else if function == "queryByParams" {
		return s.queryByParams(APIstub, args)
	} else if function == "queryByIndex" {
		return s.queryByIndex(APIstub, args)
	} else if function == "queryTxIDByKey" {
		return s.queryTxIDByKey(APIstub, args)
	}

	return shim.Error("Invalid Smart Contract function name.")
}

func (s *SmartContract) create(APIstub shim.ChaincodeStubInterface, args []string) pb.Response {

	if len(args) < 1 {
		log.Errorf("参数数量不正确，期望为>1个")
		return shim.Error("Incorrect number of arguments. Expecting > 1")
	}

	var clrOrder ClrOrder

	error := json.Unmarshal([]byte(args[0]), &clrOrder)

	if error != nil {
		log.Error(error.Error())
		return shim.Error(error.Error())
	}

	log.Infof("json 反序列化对象:", clrOrder)

	key := clrOrder.OrderId + clrOrder.SysCode
	APIstub.PutState(key, []byte(args[0]))
	log.Infof("保存数据key:%s", key)

	//compositeKey, keyErr := APIstub.CreateCompositeKey(CLRORDER_KEY, []string{clrOrder.OrderId, clrOrder.SysCode})
	//
	//if keyErr != nil {
	//	log.Errorf("CreateCompositeKey %s %s %s", clrOrder.OrderId, clrOrder.SysCode, keyErr.Error())
	//	return shim.Error(keyErr.Error())
	//}
	//
	//APIstub.PutState(compositeKey, []byte(args[0]))
	//log.Infof("%s %s compositeKey:%s", clrOrder.OrderId, clrOrder.SysCode, compositeKey)

	err := APIstub.SetEvent("create", []byte(args[0]))
	if err != nil {
		log.Error("监听事件异常!", err)
		return shim.Error(err.Error())
	}
	log.Infof("发送事件 %s", "invoke")
	txID := APIstub.GetTxID()
	ClrOrderResponse := ClrOrderResponse{Key: key, TxID: txID}

	jsonBytes, jsonErr := json.Marshal(ClrOrderResponse)

	if jsonErr != nil {
		log.Errorf("Marshal %s %s", ClrOrderResponse, jsonErr.Error())
		return shim.Error(jsonErr.Error())
	}

	log.Infof("返回客户端信息：%s", string(jsonBytes))

	return shim.Success(jsonBytes)
}

func (s *SmartContract) triggerCheck(APIstub shim.ChaincodeStubInterface, args []string) pb.Response {

	log.Infof("查询到区块链数据!触发对账!")

	queryResp := s.queryByParams(APIstub, args)

	queryBytes := queryResp.Payload

	var QueryClrOrders []QueryClrOrder

	error := json.Unmarshal(queryBytes, &QueryClrOrders)

	log.Infof("query %s", string(queryBytes))
	if error != nil {
		log.Errorf("json 反序列化异常！ %s %s", args, error.Error())
		return shim.Error(fmt.Sprintf("json 反序列化异常！ %s", args))
	}

	if len(QueryClrOrders) == 0 {
		log.Errorf("未查询到区块信息！ %s", args)
		return shim.Error(fmt.Sprintf("未查询到区块信息！ %s", args))
	}

	jsonValue, err := json.Marshal(QueryClrOrders[0].Record)

	if err != nil {
		log.Errorf("json格式化异常！ %s", err.Error())
		return shim.Error(fmt.Sprintf("json格式化异常！ %s", args))
	}

	log.Infof("查询到交易 %s", jsonValue)

	ccName := "account"
	ccArgs := [][]byte{[]byte("clrCheck"), []byte(args[0]), []byte(args[1])}

	nt := 1

	for i := 1; i <= nt; i++ {

		log.Infof("第 %d 次触发对账智能合约！", i)

		log.Infof("调用区块链>>> 智能合约：%s channel：%s 参数：%s ", ccName, APIstub.GetChannelID(), ccArgs)
		response := APIstub.InvokeChaincode(ccName, ccArgs, APIstub.GetChannelID())
		log.Infof("智能合约返回>>> 状态：%s 描述：%s 内容：%s", response.GetStatus(), response.GetMessage(), response.GetPayload())
		resultBytes := response.GetPayload()
		if response.Status == 200 && len(resultBytes) > 0 {
			log.Infof("对账结果：%s", string(resultBytes))
			return shim.Success(resultBytes)
		} else {
			log.Infof("%s\n 等待查询...3 s", response.Message)
			time.Sleep(time.Second * 3)
		}
	}

	log.Info("触发对账操作失败！")
	return shim.Error("未查询到对账信息")
}

func (s *SmartContract) query(APIstub shim.ChaincodeStubInterface, args []string) pb.Response {

	if len(args) < 2 {
		log.Errorf("Incorrect number of arguments. Expecting 2")
		return shim.Error(ArgsMismatch)
	}
	//compositeKey, err := APIstub.CreateCompositeKey(CLRORDER_KEY, args)
	//if err != nil {
	//	log.Errorf("CreateCompositeKey异常！ %s", err.Error())
	//	return shim.Error(fmt.Sprintf("CreateCompositeKey异常！ %s", args))
	//}
	//
	//orderBytes, err := APIstub.GetState(compositeKey)
	key := args[0] + args[1]
	orderBytes, err := APIstub.GetState(key)
	if err != nil {
		log.Errorf("APIstub.GetState(key) 异常！ %s", err.Error())
		return shim.Error(QueryError)
	}
	log.Infof("%s 下所有交易订单号 %s", args[1], string(orderBytes))

	return shim.Success(orderBytes)
}

// =======Rich queries =========================================================================
// Two examples of rich queries are provided below (parameterized query and ad hoc query).
// Rich queries pass a query string to the state database.
// Rich queries are only supported by state database implementations
//  that support rich query (e.g. CouchDB).
// The query string is in the syntax of the underlying state database.
// With rich queries there is no guarantee that the result set hasn't changed between
//  endorsement time and commit time, aka 'phantom reads'.
// Therefore, rich queries should not be used in update transactions, unless the
// application handles the possibility of result set changes between endorsement and commit time.
// Rich queries can be used for point-in-time queries against a peer.
// ============================================================================================

// ===== Example: Parameterized rich query =================================================
// queryMarblesByOwner queries for marbles based on a passed in owner.
// This is an example of a parameterized query where the query logic is baked into the chaincode,
// and accepting a single query parameter (owner).
// Only available on state databases that support rich query (e.g. CouchDB)
// =========================================================================================
func (s *SmartContract) queryByParams(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	if len(args) < 1 {
		return shim.Error(ArgsMismatch)
	}

	queryString := fmt.Sprintf("{\"selector\":{\"orderId\":\"%s\"}}", args[0])

	queryResults, err := GetQueryResultForQueryString(stub, queryString)
	if err != nil {
		log.Errorf("%s %s", queryString, err.Error())
		return shim.Error(QueryError)
	}

	var queryClrOrders []*QueryClrOrder
	jsonErr := json.Unmarshal(GetValidByte(queryResults), &queryClrOrders)
	if jsonErr != nil {
		log.Errorf("json.Unmarshal %s", jsonErr.Error())
		return shim.Error(JSONFormat)
	}

	err = QueryTxIdFromList(stub, queryClrOrders)
	if err != nil {
		log.Errorf(" QueryTxIdFromList %s", err.Error())
		return shim.Error(QueryError)
	}
	jsonBytes, jsonErr := json.Marshal(queryClrOrders)
	if jsonErr != nil {
		log.Errorf("json.Marshal %s", jsonErr.Error())
		return shim.Error(JSONFormat)
	}

	log.Infof("返回数据：%s", string(jsonBytes))

	return shim.Success(jsonBytes)
}

// ===== Example: Ad hoc rich query ========================================================
// queryMarbles uses a query string to perform a query for marbles.
// Query string matching state database syntax is passed in and executed as is.
// Supports ad hoc queries that can be defined at runtime by the client.
// If this is not desired, follow the queryMarblesForOwner example for parameterized queries.
// Only available on state databases that support rich query (e.g. CouchDB)
// =========================================================================================
func (s *SmartContract) queryByIndex(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	//   0
	// "queryString"
	if len(args) < 1 {
		return shim.Error(ArgsMismatch)
	}

	queryString := args[0]

	queryResults, err := GetQueryResultForQueryString(stub, queryString)
	if err != nil {
		return shim.Error(QueryError)
	}
	return shim.Success(queryResults)
}

func (s *SmartContract) queryTxIDByKey(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	if len(args) < 2 {
		return shim.Error(ArgsMismatch)
	}

	//compositeKey, err := stub.CreateCompositeKey(CLRORDER_KEY, args)
	//
	//if err != nil {
	//	log.Errorf("CreateCompositeKey %v %s", args, err.Error())
	//	return shim.Error(fmt.Sprintf("CreateCompositeKey %v %s", args, err.Error()))
	//}

	//result, err := GetHistoryListByKey(stub, compositeKey)
	result, err := GetHistoryListByKey(stub, args[0]+args[1])

	if err != nil {
		log.Errorf("getHistoryListResult 异常： %s", err.Error())
		return shim.Error(QueryError)
	}

	return shim.Success(result)

}

//根据key获取txId
func QueryTxIdFromList(stub shim.ChaincodeStubInterface, queryClrOrders []*QueryClrOrder) error {

	//查询TxID
	for i := 0; i < len(queryClrOrders); i++ {
		queryClrOrder := queryClrOrders[i]
		orderId := queryClrOrder.Record.OrderId
		sysCode := queryClrOrder.Record.SysCode
		//compositeKey, err := stub.CreateCompositeKey(CLRORDER_KEY, []string{orderId, sysCode})
		//
		//if err != nil {
		//	log.Errorf("CreateCompositeKey %s %s %s", orderId, sysCode, err.Error())
		//	return err
		//}
		key := orderId + sysCode
		historyBytes, err := GetHistoryListByKey(stub, key)
		//queryResponse := s.queryTxIDByKey(stub, []string{orderId + sysCode})

		if err != nil {
			log.Errorf("GetHistoryListByKey %s %s", key, err.Error())
			return err
		}

		var KeyModificationList KeyModificationList

		jsonErr := json.Unmarshal(historyBytes, &KeyModificationList)
		if jsonErr != nil {
			log.Errorf("json.Unmarshal 异常： %s", jsonErr.Error())
			return err
		}

		sort.Sort(KeyModificationList)

		sortedBytes, jsonErr := json.Marshal(KeyModificationList)
		if jsonErr != nil {
			log.Errorf("json.Marshal 异常： %s", jsonErr.Error())
			return err
		}

		log.Infof(" 排序后：%s", string(sortedBytes))
		queryClrOrder.TxID = KeyModificationList[0].TxId
	}
	return nil
}

func main() {

	err := shim.Start(new(SmartContract))
	if err != nil {
		log.Errorf("Error creating new Smart Contract: %s", err)
	}
}

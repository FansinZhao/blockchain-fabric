package main

import (
	"bytes"
	"encoding/json"
	"errors"
	"fmt"
	. "github.com/hyperledger/fabric/chaincode/common"
	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
	"strings"
)

var log = shim.NewLogger("account")

type SmartContract struct {
}

func (s *SmartContract) Init(APIstub shim.ChaincodeStubInterface) pb.Response {
	log.SetLevel(shim.LogDebug)
	return shim.Success(nil)
}

func (s *SmartContract) Invoke(APIstub shim.ChaincodeStubInterface) pb.Response {

	function, args := APIstub.GetFunctionAndParameters()
	log.Infof("调用智能合约！%s %s", function, args)
	if function == "query" {
		return s.query(APIstub, args)
	} else if function == "list" {
		return s.list(APIstub)
	} else if function == "update" {
		return s.update(APIstub, args)
	} else if function == "history" {
		return s.history(APIstub, args)
	} else if function == "invoke" {
		return s.invoke(APIstub, args)
	} else if function == "clrCheck" {
		return s.clrCheck(APIstub, args)
	}

	return shim.Error("Invalid Smart Contract function name.")
}

func (s *SmartContract) query(APIstub shim.ChaincodeStubInterface, args []string) pb.Response {

	if len(args) != 1 {
		log.Errorf("Incorrect number of arguments. Expecting 1")
		return shim.Error(ArgsMismatch)
	}

	carAsBytes, err := APIstub.GetState(args[0])
	if err != nil {
		log.Error("GetState 异常!", err.Error())
		return shim.Error(QueryError)
	}

	return shim.Success(carAsBytes)
}

func (s *SmartContract) list(APIstub shim.ChaincodeStubInterface) pb.Response {

	startKey := "B0000001"
	endKey := "B9999999"

	resultsIterator, err := APIstub.GetStateByRange(startKey, endKey)
	if err != nil {
		log.Error("GetStateByRange 异常!", err.Error())
		return shim.Error(err.Error())
	}
	defer resultsIterator.Close()

	// buffer is a JSON array containing QueryResults
	var buffer bytes.Buffer
	buffer.WriteString("[")

	bArrayMemberAlreadyWritten := false
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			log.Debugf("list ", err.Error())
			return shim.Error(err.Error())
		}
		// Add a comma before array members, suppress it for the first array member
		if bArrayMemberAlreadyWritten == true {
			buffer.WriteString(",")
		}
		buffer.WriteString("{\"Key\":")
		buffer.WriteString("\"")
		buffer.WriteString(queryResponse.Key)
		buffer.WriteString("\"")

		buffer.WriteString(", \"Record\":")
		// Record is a JSON object, so we write as-is
		buffer.WriteString(string(queryResponse.Value))
		buffer.WriteString("}")
		bArrayMemberAlreadyWritten = true
	}
	buffer.WriteString("]")

	log.Infof("- listAllAcount:\n%s\n", buffer.String())

	return shim.Success(buffer.Bytes())
}

func (s *SmartContract) update(APIstub shim.ChaincodeStubInterface, args []string) pb.Response {
	if len(args) != 2 {
		log.Debugf("Incorrect number of arguments. Expecting 2")
		return shim.Error("Incorrect number of arguments. Expecting 2")
	}

	accountAsBytes, _ := APIstub.GetState(args[0])
	account := ClrOrder{}

	json.Unmarshal(accountAsBytes, &account)
	account.Status = args[1]
	log.Debugf("Account update status:%s", args[1])
	accountAsBytes, _ = json.Marshal(account)
	APIstub.PutState(args[0], accountAsBytes)
	log.Debugf("Account update end")
	err := APIstub.SetEvent("update", accountAsBytes)
	if err != nil {
		log.Error("监听事件异常!", err)
	}
	log.Debugf("发送事件 %s", "update")

	return shim.Success(nil)
}

func (s *SmartContract) history(APIstub shim.ChaincodeStubInterface, args []string) pb.Response {
	if len(args) != 1 {
		log.Debugf("Incorrect number of arguments. Expecting 2")
		return shim.Error("Incorrect number of arguments. Expecting 2")
	}

	resultsIterator, err := APIstub.GetHistoryForKey(args[0])
	if err != nil {
		log.Error("查询历史异常 %s %s", args[0], err.Error())
		return shim.Error(err.Error())
	}

	defer resultsIterator.Close()
	// buffer is a JSON array containing QueryRecords
	var buffer bytes.Buffer
	buffer.WriteString("[")

	bArrayMemberAlreadyWritten := false
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			log.Error("历史遍历异常 %s %s", queryResponse.TxId, err.Error())
			return shim.Error(err.Error())
		}
		// Add a comma before array members, suppress it for the first array member
		if bArrayMemberAlreadyWritten == true {
			buffer.WriteString(",")
		}
		item, _ := json.Marshal(queryResponse)
		buffer.Write(item)
		bArrayMemberAlreadyWritten = true
	}
	buffer.WriteString("]")
	log.Infof("historyResult:\n%s\n", buffer.String())
	return shim.Success(buffer.Bytes())

}
func (s *SmartContract) invoke(APIstub shim.ChaincodeStubInterface, args []string) pb.Response {
	if len(args) > 4 {
		log.Error("Incorrect number of arguments. Expecting > 4")
		return shim.Error("Incorrect number of arguments. Expecting 4")
	}

	var ccArgsArgs []byte
	for _, value := range args[3:] {
		ccArgsArgs = []byte(value)
	}
	ccArgs := [][]byte{[]byte(args[2]), ccArgsArgs}
	response := APIstub.InvokeChaincode(args[0], ccArgs, args[1])
	log.Infof("调用区块链 %s %s %s 返回:%s", args[0], args[1], ccArgs, response.GetPayload())
	return shim.Success([]byte(response.GetPayload()))

}
func (s *SmartContract) clrCheck(APIstub shim.ChaincodeStubInterface, args []string) pb.Response {

	if len(args) < 1 {
		log.Error("Incorrect number of arguments. Expecting = 3")
		return shim.Error(ArgsMismatch)
	}

	orderId := args[0]

	log.Infof("交易参数：%s", orderId)

	ccName := args[1]

	ccArgs := [][]byte{[]byte("queryByParams"), []byte(orderId)}

	aClrOrder, aTxId, err := QueryClrOrderFromCC(APIstub, ccName, ccArgs)

	if err != nil {
		log.Errorf("QueryClrOrderFromCC %s %s %s", ccName, ccArgs, err.Error())
		return shim.Error(QueryError)
	}

	ccName = args[2]

	bClrOrder, bTxId, err := QueryClrOrderFromCC(APIstub, ccName, ccArgs)

	if err != nil {
		log.Errorf("QueryClrOrderFromCC %s %s %s", ccName, ccArgs, err.Error())
		return shim.Error(QueryError)
	}

	resultBytes := Check(aClrOrder, bClrOrder, aTxId, bTxId)

	APIstub.PutState(orderId, resultBytes)
	log.Infof("记录对账结果%s : %s", orderId, string(resultBytes))
	return shim.Success(resultBytes)
}

func QueryClrOrderFromCC(stub shim.ChaincodeStubInterface, ccName string, ccArgs [][]byte) (ClrOrder, string, error) {
	log.Infof("调用区块链>>> 智能合约：%s channel：%s 参数：%s ", ccName, stub.GetChannelID(), ccArgs)
	response := stub.InvokeChaincode(ccName, ccArgs, stub.GetChannelID())
	log.Infof("智能合约返回>>> 状态：%s 描述：%s 内容：%s", response.GetStatus(), response.GetMessage(), response.GetPayload())
	queryBytes := response.GetPayload()

	if response.Status != shim.OK || len(response.Payload) == 0 {
		log.Errorf("queryTxIDByKey %s", response)
		return ClrOrder{}, "", errors.New(fmt.Sprintf("智能合约：%s channel：%s 参数：%s ", ccName, stub.GetChannelID(), ccArgs))
	}

	var queryClrOrders []QueryClrOrder
	jsonErr := json.Unmarshal(queryBytes, &queryClrOrders)
	if jsonErr != nil {
		log.Errorf("json.Unmarshal %s", jsonErr.Error())
		return ClrOrder{}, "", jsonErr
	}

	if len(queryClrOrders) != 1 {
		log.Errorf("对账主体不完整,数组期望大小为2 %s", queryClrOrders)
		return ClrOrder{}, "", errors.New(fmt.Sprintf("对账主体不完整,数组期望大小为2 %v", queryClrOrders))
	}

	return queryClrOrders[0].Record, queryClrOrders[0].TxID, nil
}

//对账逻辑
func Check(aClrOrder, bClrOrder ClrOrder, aTxId, bTxId string) []byte {
	var status, clrCode string

	var clrMsg string

	buffer := CheckDetail(aClrOrder, bClrOrder)

	if buffer.Len() > 0 {
		status = "fail"
		clrCode = "0001"
		clrMsg = "差错帐"
	} else {
		status = "success"
		clrCode = "0000"
		clrMsg = "平帐"
	}

	var AccountResult ClrOrderResult
	AccountResult.Status = status
	AccountResult.NccTxId = aTxId
	AccountResult.CtsTxId = bTxId
	AccountResult.ClrCode = clrCode
	AccountResult.ClrMsg = clrMsg
	AccountResult.ClrJsonMsg = buffer.String()
	AccountResult.OrderId = aClrOrder.OrderId
	resultBytes, _ := json.Marshal(AccountResult)

	return resultBytes
}

func CheckDetail(aClrOrder, bClrOrder ClrOrder) *bytes.Buffer {

	var clrJsonMsg string
	var buffer bytes.Buffer

	buffer.WriteString("{")

	if strings.Compare(aClrOrder.CustNo, bClrOrder.CustNo) != 0 {
		clrJsonMsg = fmt.Sprintf("\"CustNo\":\"%s:%s,%s:%s\"", aClrOrder.SysCode, aClrOrder.CustNo, bClrOrder.SysCode, bClrOrder.CustNo)
		ConstructClrJsonMsg(&buffer, clrJsonMsg)
	}

	if strings.Compare(fmt.Sprintf("%.2f", aClrOrder.TransAmt), fmt.Sprintf("%.2f", bClrOrder.TransAmt)) != 0 {
		clrJsonMsg = fmt.Sprintf("\"TransAmt\":\"%s:%.2f,%s:%.2f\"", aClrOrder.SysCode, aClrOrder.TransAmt, bClrOrder.SysCode, bClrOrder.TransAmt)
		ConstructClrJsonMsg(&buffer, clrJsonMsg)
	}
	if strings.Compare(aClrOrder.BankCardNo, bClrOrder.BankCardNo) != 0 {
		clrJsonMsg = fmt.Sprintf("\"BankCardNo\":\"%s:%s,%s:%s\"", aClrOrder.SysCode, aClrOrder.BankCardNo, bClrOrder.SysCode, bClrOrder.BankCardNo)
		ConstructClrJsonMsg(&buffer, clrJsonMsg)
	}

	buffer.WriteString("}")

	if buffer.Len() == 2 {
		buffer.Reset()
	}

	return &buffer
}

func ConstructClrJsonMsg(buffer *bytes.Buffer, append string) {
	if buffer.Len() > 0 {
		buffer.WriteString(",")
	}
	buffer.WriteString(append)
}

func main() {

	err := shim.Start(new(SmartContract))
	if err != nil {
		log.Errorf("Error creating new Smart Contract: %s", err)
	}
}

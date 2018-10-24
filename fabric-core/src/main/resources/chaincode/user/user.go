package main

import (
	"encoding/json"
	"fmt"
	"github.com/hyperledger/fabric/core/chaincode/lib/cid"
	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
	"strings"
	"sync"
)

var log = shim.NewLogger("user")

type SmartContract struct {
}

type FabricUser struct {
	Name        string `json:name`
	Account     string `json:account`
	Affiliation string `json:affiliation`
	MspId       string `json:mspId`
	Roles       Set    `json:roles`
}

type Set struct {
	m map[string]bool
	sync.RWMutex
}

func (s *SmartContract) Init(stub shim.ChaincodeStubInterface) pb.Response {

	return shim.Success(nil)
}

func (s *SmartContract) Invoke(stub shim.ChaincodeStubInterface) pb.Response {
	function, args := stub.GetFunctionAndParameters()
	log.Infof("调用智能合约！%s", function)
	if function == "addUser" {
		return s.addUser(stub, args)
	} else if function == "updateUser" {
		return s.updateUser(stub, args)
	} else if function == "deleteUser" {
		return s.deleteUser(stub, args)
	} else if function == "authUser" {
		return s.authUser(stub, args)
	}
	return shim.Error("Invalid Smart Contract function name.")
}

func (t *SmartContract) getChaincodeUser(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	log.Info(fmt.Sprintf("get args: %s", args))

	//getting submitter of the transaction
	log.Info("begin to read userInfo")
	sinfo, err := cid.New(stub)
	if err != nil {
		log.Error(fmt.Sprintf("get submitter of the transaction: %s", sinfo))
		return shim.Error(err.Error())
	}
	id, _ := sinfo.GetID()
	log.Info(fmt.Sprintf("get clientIdentityId: %s", id))
	mspId, _ := sinfo.GetMSPID()
	log.Info(fmt.Sprintf("get clientIdentityMSPId: %s", mspId))

	//读取dept的相关值
	dv, df, err := sinfo.GetAttributeValue("dept")
	if err != nil {
		log.Error(fmt.Sprintf("get deptAttrVal err: %s", err.Error()))
	} else {
		if df {
			log.Info(fmt.Sprintf("get deptAttrVal: %s", dv))
		} else {
			log.Debug(fmt.Sprintf("not found deptAttrbute"))
		}
	}

	//读取org
	ov, of, err := sinfo.GetAttributeValue("org")
	if err != nil {
		log.Error(fmt.Sprintf("get orgAttrVal err: %s", err.Error()))
	} else {
		if of {
			log.Info(fmt.Sprintf("got orgAttrVal: %s", ov))
		} else {
			log.Debug(fmt.Sprintf("not found orgAttrbute"))
		}
	}

	//读取peer
	pv, pf, err := sinfo.GetAttributeValue("peer")
	if err != nil {
		log.Error(fmt.Sprintf("get peerAttrVal err: %s", err.Error()))
	} else {
		if pf {
			log.Info(fmt.Sprintf("got peerAttrVal: %s", pv))
		} else {
			log.Debug(fmt.Sprintf("not found peerAttrbute"))
		}
	}
	//读取user
	uv, uf, err := sinfo.GetAttributeValue("user")
	if err != nil {
		log.Error(fmt.Sprintf("get userAttrVal err: %s", err.Error()))
	} else {
		if uf {
			log.Info(fmt.Sprintf("got userAttrVal: %s", uv))
		} else {
			log.Debug(fmt.Sprintf("not found userAttrbute"))
		}
	}

	return shim.Success([]byte("请看日志"))
}

func (s *SmartContract) addUser(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	if len(args) != 6 {
		log.Errorf("Incorrect number of arguments. Expecting 6")
		return shim.Error("Incorrect number of arguments. Expecting 6")
	}

	FabricUser := FabricUser{Name: args[1], Account: args[2], Affiliation: args[3], MspId: args[4], Roles: Set{m: map[string]bool{args[5]: true}}}

	collection := args[0]

	userBytes, _ := json.Marshal(FabricUser)

	error := stub.PutPrivateData(collection, FabricUser.Account, userBytes)

	if error != nil {
		log.Errorf("保存数据异常!", error.Error())
		return shim.Error(error.Error())
	}
	return shim.Success(nil)

}
func (s *SmartContract) updateUser(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	if len(args) != 3 {
		log.Errorf("Incorrect number of arguments. Expecting 3")
		return shim.Error("Incorrect number of arguments. Expecting 3")
	}
	collection := args[0]
	key := args[1]
	userBytes, error := stub.GetPrivateData(collection, key)

	if error != nil {
		log.Errorf("查询到数据异常!", error.Error())
		return shim.Error(error.Error())
	}

	if userBytes == nil {
		log.Errorf("未查询到数据!", error.Error())
		return shim.Error(error.Error())
	}

	var FabricUser FabricUser
	_ = json.Unmarshal(userBytes, &FabricUser)

	FabricUser.Account = args[2]

	userBytes, _ = json.Marshal(FabricUser)

	error = stub.PutPrivateData(collection, key, userBytes)
	if error != nil {
		log.Errorf("更新数据异常!", error.Error())
		return shim.Error(error.Error())
	}
	return shim.Success(nil)
}
func (s *SmartContract) deleteUser(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	if len(args) != 2 {
		log.Errorf("Incorrect number of arguments. Expecting 2")
		return shim.Error("Incorrect number of arguments. Expecting 2")
	}

	collection := args[0]
	key := args[1]

	error := stub.DelPrivateData(collection, key)

	if error != nil {
		log.Errorf("删除数据异常!", error.Error())
		return shim.Error(error.Error())
	}
	return shim.Success(nil)
}
func (s *SmartContract) authUser(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	if len(args) != 2 {
		log.Errorf("Incorrect number of arguments. Expecting 2")
		return shim.Error("Incorrect number of arguments. Expecting 2")
	}

	collection := args[0]
	key := args[1]
	userBytes, error := stub.GetPrivateData(collection, key)

	if error != nil {
		log.Errorf("查询到数据异常!", error.Error())
		return shim.Error(error.Error())
	}

	if userBytes == nil {
		log.Errorf("未查询到数据!", error.Error())
		return shim.Error(error.Error())
	}

	var FabricUser FabricUser
	_ = json.Unmarshal(userBytes, &FabricUser)

	if strings.Compare(args[2], FabricUser.Account) == 0 {
		return shim.Success(nil)
	} else {
		return shim.Error("account 不匹配,请核对用户信息")
	}

}

func main() {

	err := shim.Start(new(SmartContract))
	if err != nil {
		log.Errorf("Error creating new Smart Contract: %s", err)
	}
}

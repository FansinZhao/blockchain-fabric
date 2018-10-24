package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	. "github.com/hyperledger/fabric/chaincode/common"
	"strconv"
)

func main() {
	str := "[{\"Key\":\"ClrOrderCTS0123456\", \"Record\":{\"bankCardNo\":\"123456789\",\"custNo\":\"a123456789\",\"orderId\":\"0123456\",\"sysCode\":\"CTS\",\"transAmt\":123.45}},{\"Key\":\"0123456CTS\", \"Record\":{\"bankCardNo\":\"123456789\",\"custNo\":\"a123456789\",\"orderId\":\"0123456\",\"sysCode\":\"CTS\",\"transAmt\":123.45}}]"

	fmt.Println([]byte(str))
	fmt.Println(len(str))
	fmt.Println(len([]byte(str)))
	var queryClrOrders1 []QueryClrOrder
	e := json.Unmarshal([]byte(str), &queryClrOrders1)
	if e != nil {
		fmt.Errorf(e.Error())
	}
	fmt.Println(queryClrOrders1)

	var buffer bytes.Buffer
	buffer.WriteString("[")
	buffer.WriteString("{\"Key\":")
	buffer.WriteString("\"")
	buffer.WriteString("ClrOrderCTS0123456")
	buffer.WriteString("\"")

	buffer.WriteString(", \"Record\":")
	// Record is a JSON object, so we write as-is
	buffer.WriteString(string("{\"bankCardNo\":\"123456789\",\"custNo\":\"a123456789\",\"orderId\":\"0123456\",\"sysCode\":\"CTS\",\"transAmt\":123.45}},{\"Key\":\"0123456CTS\", \"Record\":{\"bankCardNo\":\"123456789\",\"custNo\":\"a123456789\",\"orderId\":\"0123456\",\"sysCode\":\"CTS\",\"transAmt\":123.45}}"))
	buffer.WriteString("]")

	fmt.Printf(">>> %s \n%d\n", buffer.String(), len(buffer.String()))
	fmt.Printf(">>> %s \n", buffer.Bytes())

	var queryClrOrders []*QueryClrOrder
	e = json.Unmarshal(buffer.Bytes(), &queryClrOrders)
	if e != nil {
		fmt.Errorf(e.Error())
	}
	fmt.Printf("\n????<<<< %s\n", queryClrOrders)

	var buffer1 bytes.Buffer
	fmt.Printf("555 %s %d\n", buffer1.String(), buffer1.Len())
	buffer1.WriteString("{}")
	fmt.Printf("555 %s %d", buffer1.String(), buffer1.Len())
	buffer1.Reset()
	fmt.Printf("\n 666 %s %d", buffer1.String(), buffer1.Len())
	ConstructClrJsonMsg(&buffer1, "123")
	TestArray(queryClrOrders)
	fmt.Print("<><><><")
	fmt.Print(queryClrOrders[0].Key)

	fmt.Printf("%s\n", buffer1.String())
	ConstructClrJsonMsg(&buffer1, "123")

	fmt.Printf(buffer1.String())

	fmt.Printf("\nddd %.2f\n", queryClrOrders[0].Record.TransAmt)
	fmt.Println(strconv.FormatFloat(queryClrOrders[0].Record.TransAmt, 'f', -1, 64))

}

func ConstructClrJsonMsg(buffer *bytes.Buffer, append string) {
	if buffer.Len() > 0 {
		buffer.WriteString(",")
	}
	buffer.WriteString(append)
}

func TestArray(queryClrOrders []*QueryClrOrder) {
	queryClrOrders[0].Key = "12313213123"
}

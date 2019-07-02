package main

import (
	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
	"fmt"
	"bytes"
	"encoding/json"
	"github.com/hyperledger/fabric/core/chaincode/lib/cid"
	"strings"
)

const (
	signReqPrefix = "sign_req_"
	signRespPrefix = "sign_resp_"
)

const (
	RequestStatusSigning	= "signing"
	RequestStatusEnd		= "end"
	RequestStatusInvalid	= "invalid"
)

type signRequest struct {
	Id      string   `json:"id"`      // 签名申请发起人构造，时间戳（或其他方式）（不用担心CouchDB中KEY冲突），KEY的构造方式会避免冲突
	From    string   `json:"from"`    // 链码自行填充
	Content string   `json:"content"` // 需要签名的数据
	MSP     []string `json:"msp"`     // 当前通道中有哪些MSP信息
	Desc    string   `json:"desc"`    // 通道配置变更描述
	Version string   `json:"version"` // 通道配置的版本
	Status  string   `json:"status"`  // 请求的状态，标识是否结束
	Time    int64	 `json:"time"`    // 请求发起时间
	Type    string   `json:"type"`    // 请求类型
}

type signResponse struct {
	Id			string `json:"id"`						// 签名申请中的Id，通过事件通知方式，传递给签名方，并和签名一起返回给Fabric
	Reject		string `json:"reject"`					// 是否拒绝，Y/N
	From		string `json:"from"`					// 链码自行构造
	To			string `json:"to"`						// 签名方以参数方式携带，链码校验是否与原始签名请求中From字段一致
	Signature	string `json:"signature,omitempty"`	// 签名
	Reason		string `json:"reason,omitempty"`		// 拒绝原因，同意签名时为空
	Time		int64 `json:"time"`            		// 时间戳
}

func (s *signResponse) check() error {
	if s.To == "" {
		return fmt.Errorf("invalid response with null receiver")
	}
	if s.Reject != "Y" && s.Reject != "N" {
		return fmt.Errorf("the value of reject must be Y or N")
	}
	if s.Reject == "Y" && s.Reason == "" {
		return fmt.Errorf("invalid response with null reject reason")
	}
	if s.Reject == "N" && s.Signature == "" {
		return fmt.Errorf("invalid response with null signature")
	}
	return nil
}

func New() shim.Chaincode {
	return &scc{}
}

type scc struct{}

func (s *scc) Init(stub shim.ChaincodeStubInterface) pb.Response {
	return shim.Success(nil)
}

func (s *scc) Invoke(stub shim.ChaincodeStubInterface) pb.Response {
	function, args := stub.GetFunctionAndParameters()

	switch function {
	case "signRequests":
		return s.signRequests(stub, args)
	case "signResponses":
		return s.signResponses(stub, args)
	case "updateRequestState":
		return s.updateRequestState(stub, args)
	case "getMySignRequests":
		return s.getMySignRequests(stub, args)
	case "getMyAllSignResponses":
		return s.getMyAllSignResponses(stub, args)
	case "getAllSignResponsesById":
		return s.getAllSignResponsesById(stub, args)
	}
	return shim.Error("Invalid invoke function name")
}

/* 签名申请
* args[0]: 本组织内的唯一标识即可
* args[1]: 需要签名的数据
* args[2]: 类型
* args[3]: 版本
* args[4]: 变更内容
* args[5]: destMsp
*/
func (s *scc) signRequests(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	if len(args) != 6 {
		return shim.Error("Incorrect number of arguments. Expecting 6.")
	}

	request := signRequest{
		Id: args[0],
		Content: args[1],
		MSP: strings.Split(args[5], ","),
		Type: args[2],
		Version: args[3],
		Desc: args[4],
		Status: RequestStatusSigning,
	}

	var err error
	if request.From,err = cid.GetMSPID(stub); err != nil {
		return shim.Error(err.Error())
	}

	if tm,err := stub.GetTxTimestamp(); err != nil {
		return shim.Error(err.Error())
	}else{
		request.Time = tm.Seconds
	}

	key := signReqPrefix + request.From + "_" + request.Id
	if asBytes,err := stub.GetState(key); err != nil {
		return shim.Error(err.Error())
	}else if !bytes.Equal(asBytes, nil) {
		return shim.Error("invalid request with duplicated id")
	}

	var payload []byte
	if payload,err = json.Marshal(request); err != nil {
		return shim.Error(err.Error())
	}else if err = stub.PutState(key, payload); err != nil {
		return shim.Error(err.Error())
	}

	if err = stub.SetEvent("SignRequest", payload); err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success(nil)
}

/* 签名应答
* args[0]: 对应的签名申请ID
* args[1]: 签名的接收者
* args[2]: 是否拒绝
* args[3]: 签名
* args[4]: 拒绝原因
*/
func (s *scc) signResponses(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	if len(args) != 5 {
		return shim.Error("Incorrect number of arguments. Expecting 5.")
	}

	var err error
	resp := signResponse{
		Id: args[0],
		To: args[1],
		Reject: args[2],
		Signature: args[3],
		Reason: args[4],
	}

	if err = resp.check(); err != nil {
		return shim.Error(err.Error())
	}

	if resp.From,err = cid.GetMSPID(stub); err != nil {
		return shim.Error(err.Error())
	}

	if tm,err := stub.GetTxTimestamp(); err != nil {
		return shim.Error(err.Error())
	}else{
		resp.Time = tm.Seconds
	}

	// 获取原始签名申请中的发起者
	key := signReqPrefix + resp.To + "_" + resp.Id
	if asBytes,err := stub.GetState(key); err != nil {
		return shim.Error(err.Error())
	}else if !bytes.Equal(asBytes, nil) {
		origRequest := signRequest{}
		if err = json.Unmarshal(asBytes, &origRequest); err != nil{
			return shim.Error(err.Error())
		}
		if origRequest.From != resp.To {
			return shim.Error("invalid response with incorrect receiver")
		}
	}else{
		return shim.Error("invalid response with incorrect id")
	}

	var payload []byte
	key = signRespPrefix + resp.From + "_" + resp.Id
	if asBytes,err := stub.GetState(key); err != nil {
		return shim.Error(err.Error())
	}else if !bytes.Equal(asBytes, nil) {
		return shim.Error("invalid response with duplicated id")
	}

	if payload,err = json.Marshal(resp); err != nil {
		return shim.Error(err.Error())
	}else if err = stub.PutState(key, payload); err != nil {
		return shim.Error(err.Error())
	}

	if err = stub.SetEvent("SignResponse_" + resp.To, payload); err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success(nil)
}

/* 签名应答
* args[0]: 对应的签名申请ID
* args[1]: 状态
*/
func (s *scc) updateRequestState(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	if len(args) != 2 {
		return shim.Error("Incorrect number of arguments. Expecting 2.")
	}

	requestId,state := args[0],args[1]
	if requestId == "" || (state != RequestStatusInvalid && state != RequestStatusEnd) {
		return shim.Error("Invalid arguments.")
	}

	var err error
	from := ""
	if from,err = cid.GetMSPID(stub); err != nil {
		return shim.Error(err.Error())
	}

	key := signReqPrefix + from + "_" + requestId
	if asBytes,err := stub.GetState(key); err != nil {
		return shim.Error(err.Error())
	}else if bytes.Equal(asBytes, nil) {
		return shim.Error("Id is not exist.")
	}else {
		req := signRequest{}
		if err = json.Unmarshal(asBytes, &req); err != nil {
			return shim.Error(err.Error())
		}
		if req.Status != RequestStatusSigning {
			return shim.Error(fmt.Sprintf("Can not modify the sign request state from %s to %s.", req.Status, state))
		}
		req.Status = state

		if data,err := json.Marshal(req); err != nil {
			return shim.Error(err.Error())
		}else if err = stub.PutState(key, data); err != nil {
			return shim.Error(err.Error())
		}
	}

	var buf bytes.Buffer
	buf.WriteString(fmt.Sprintf(`{"from":"%s","id":"%s","state":"%s"}`, from, requestId, state))
	if err = stub.SetEvent("RequestState", buf.Bytes()); err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success(nil)
}

/* 查询自己的签名申请，查询指定状态的申请，""表示全匹配
* args[0]: state
*/
func (s *scc) getMySignRequests(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	if len(args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 1.")
	}

	mspId,err := cid.GetMSPID(stub)
	if err != nil {
		return shim.Error(err.Error())
	}

	reqIterator,err := stub.GetStateByRange(signReqPrefix + mspId + "_", signReqPrefix + mspId + "_:")
	if err != nil {
		return shim.Error(err.Error())
	}
	defer reqIterator.Close()

	var buf bytes.Buffer
	buf.WriteString("[")

	bHasWritten := false
	for reqIterator.HasNext() {
		val,err := reqIterator.Next()
		if err != nil {
			continue
		}

		request := signRequest{}
		if err := json.Unmarshal(val.Value, &request); err != nil {
			continue
		}
		if args[0] != "" && request.Status != args[0] {
			continue
		}

		if bHasWritten {
			buf.WriteString(",")
		}
		bHasWritten = true

		buf.Write(val.Value)
	}
	buf.WriteString("]")

	return shim.Success(buf.Bytes())
}

/* 查询某签名申请的所有签名应答
* args[0]: 签名申请Id
*/
func (s *scc) getAllSignResponsesById(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	if len(args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 1.")
	}

	mspId,err := cid.GetMSPID(stub)
	if err != nil {
		return shim.Error(err.Error())
	}

	origRequest := signRequest{}
	key := signReqPrefix + mspId + "_" + args[0]
	if asBytes,err := stub.GetState(key); err != nil {
		return shim.Error(err.Error())
	}else if !bytes.Equal(asBytes, nil) {
		if err = json.Unmarshal(asBytes, &origRequest); err != nil{
			return shim.Error(err.Error())
		}
	}else{
		return shim.Error("invalid query with incorrect id")
	}

	var buf bytes.Buffer
	buf.WriteString("[")

	bHasWritten := false
	for _,msp := range origRequest.MSP {
		asBytes,err := stub.GetState(signRespPrefix + msp + "_" + args[0])
		if err != nil || bytes.Equal(asBytes,nil) {
			continue
		}

		if bHasWritten {
			buf.WriteString(",")
		}
		bHasWritten = true

		buf.Write(asBytes)
	}
	buf.WriteString("]")

	return shim.Success(buf.Bytes())
}

/* 查询自己做出的所有签名申请应答
* Null
*/
func (s *scc) getMyAllSignResponses(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	mspId,err := cid.GetMSPID(stub)
	if err != nil {
		return shim.Error(err.Error())
	}

	respIterator,err := stub.GetStateByRange(signRespPrefix + mspId + "_", signRespPrefix + mspId + "_:")
	if err != nil {
		return shim.Error(err.Error())
	}
	defer respIterator.Close()

	var buf bytes.Buffer
	buf.WriteString("[")

	bHasWritten := false
	for respIterator.HasNext() {
		resp,err := respIterator.Next()
		if err != nil {
			continue
		}

		if bHasWritten {
			buf.WriteString(",")
		}
		bHasWritten = true

		buf.Write(resp.Value)
	}
	buf.WriteString("]")

	return shim.Success(buf.Bytes())
}

func main() {
	err := shim.Start(new(scc))
	if err != nil {
		fmt.Printf("Error:%s", err)
	}
}



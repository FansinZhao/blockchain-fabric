package common

type ClrOrder struct {
	SysCode        string  `json:"sysCode,omitempty"`
	OrderId        string  `json:"orderId,omitempty"`
	CustNo         string  `json:"custNo,omitempty"`
	BankCardNo     string  `json:"bankCardNo,omitempty"`
	CapitalCode    string  `json:"capitalCode,omitempty"`
	CapitalSubCode string  `json:"capitalSubCode,omitempty"`
	TransAmt       float64 `json:"transAmt,omitempty"`
	Term           int8    `json:"term,omitempty"`
	Status         string  `json:"status,omitempty"`
	AccountDate    string  `json:"accountDate,omitempty"`
	ExtMsg         string  `json:"extMsg,omitempty"`
	Version        string  `json:"version,omitempty"`
}

type ClrOrderResult struct {
	OrderId        string `json:"orderId,omitempty"`
	AccountDate    string `json:"accountDate,omitempty"`
	NccTxId        string `json:"nccTxId,omitempty"`
	CtsTxId        string `json:"ctsTxId,omitempty"`
	Status         string `json:"status,omitempty"`
	ClrCode        string `json:"clrCode,omitempty"`
	ClrMsg         string `json:"clrMsg,omitempty"`
	ClrJsonMsg     string `json:"clrJsonMsg,omitempty"`
	CreateDatetime string `json:"createDatetime,omitempty"`
	UpdateDatetime string `json:"updateDatetime,omitempty"`
	Version        string `json:"version,omitempty"`
	TxID           string `json:"txId,omitempty"`
}

type QueryClrOrder struct {
	Key    string   `json:"key,omitempty"`
	TxID   string   `json:"txId,omitempty"`
	Record ClrOrder `json:"record,omitempty"`
}

type ClrOrderResponse struct {
	TxID         string `json:"txId,omitempty"`
	Key          string `json:"key,omitempty"`
	CompositeKey string `json:"compositeKey,omitempty"`
}

type ClrOrderResultResponse struct {
	Code   string         `json:"txId,omitempty"`
	Msg    string         `json:"txId,omitempty"`
	TxID   string         `json:"txId,omitempty"`
	Record ClrOrderResult `json:"record,omitempty"`
}

// KeyModification -- QueryResult for history query. Holds a transaction ID, value,
// timestamp, and delete marker which resulted from a history query.
type KeyModification struct {
	TxId      string       `json:"tx_id,omitempty"`
	Value     []byte       `json:"value,omitempty"`
	Timestamp KeyTimestamp `json:"timestamp,omitempty"`
	IsDelete  bool         `json:"is_delete,omitempty"`
}

type KeyTimestamp struct {
	Seconds int `json:"seconds,omitempty"`
	Nanos   int `json:"nanos,omitempty"`
}

type KeyModificationList []*KeyModification

func (p KeyModificationList) Swap(i, j int) { p[i], p[j] = p[j], p[i] }
func (p KeyModificationList) Len() int      { return len(p) }
func (p KeyModificationList) Less(i, j int) bool {
	return p[i].Timestamp.Seconds > p[j].Timestamp.Seconds
}

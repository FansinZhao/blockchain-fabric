package common

//消除数组\x00
func GetValidByte(src []byte) []byte {
	var strBufs []byte
	for _, v := range src {
		if v != 0 {
			strBufs = append(strBufs, v)
		}
	}
	return strBufs
}

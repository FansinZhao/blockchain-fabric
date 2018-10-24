package main

import (
	"github.com/Shopify/sarama"
	"log"
	"time"
)

func sendMessage(key string, value string) {
	producer := newAccessLogProducer([]string{"kafka:9092"})

	producer.Input() <- &sarama.ProducerMessage{
		Topic: "chaincode-event",
		Key:   sarama.StringEncoder("update"),
		Value: sarama.StringEncoder(string(value)),
	}
	log.Println("发送事件到消息队列", "chaincode-event")
}

func newAccessLogProducer(brokerList []string) sarama.AsyncProducer {

	// For the access log, we are looking for AP semantics, with high throughput.
	// By creating batches of compressed messages, we reduce network I/O at a cost of more latency.
	config := sarama.NewConfig()
	//tlsConfig := createTlsConfiguration()
	//if tlsConfig != nil {
	//	config.Net.TLS.Enable = true
	//	config.Net.TLS.Config = tlsConfig
	//}
	config.Net.TLS.Enable = false
	config.Producer.RequiredAcks = sarama.WaitForLocal       // Only wait for the leader to ack
	config.Producer.Compression = sarama.CompressionSnappy   // Compress messages
	config.Producer.Flush.Frequency = 500 * time.Millisecond // Flush batches every 500ms

	producer, err := sarama.NewAsyncProducer(brokerList, config)
	if err != nil {
		log.Panic("Failed to start Sarama producer:", err)
	}

	// We will just log to STDOUT if we're not able to produce messages.
	// Note: messages will only be returned here after all retry attempts are exhausted.
	go func() {
		for err := range producer.Errors() {
			log.Panic("Failed to write access log entry:", err)
		}
	}()

	return producer
}

package cn.edu.sjtu.omnilab.livewee.logproducer;


import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.util.Properties;

/**
 * Simple producer with default configurations.
 */
public class DefaultProducer extends AbstractProducer {
    
    protected Producer<Integer, String> worker;
    private String topic;

    public DefaultProducer(ConfLoader conf) {
        super(conf);
        Properties kafka = new Properties();
        kafka.setProperty("metadata.broker.list", conf.brokers);
        kafka.put("serializer.class", conf.serializer);
        worker = new Producer<Integer, String>(new ProducerConfig(kafka));
        topic = conf.topic;
    }

    @Override
    public void sendMessage(String message) {
        worker.send(new KeyedMessage<Integer, String>(topic, message));
    }
}

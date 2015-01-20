package cn.edu.sjtu.omnilab.livewee.logproducer;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * Basic configurations for a producer
 *
 * Created by chenxm on 1/20/15.
 */
public class ProducerSettings {

    public int UDPPort;
    public int bufSize;
    public String brokers;
    public String topic;
    public String serializer;

    public ProducerSettings() throws IOException {

        // Load configuration from file
        Properties props = new Properties();
        URL url = ClassLoader.getSystemResource(Constants.PRODUCER_CONFIG_FILE);
        props.load(url.openStream());

        UDPPort = Integer.parseInt(props.getProperty(Constants.SYSLOG_SOURCE_PORT));
        bufSize = Integer.parseInt(props.getProperty(Constants.SYSLOG_BUFFER_SIZE));
        brokers = props.getProperty(Constants.KAFKA_BROKERS);
        topic = props.getProperty(Constants.KAFKA_TOPIC);
        serializer = props.getProperty(Constants.KAFKA_SERIALIZER);
    }
}

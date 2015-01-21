package cn.edu.sjtu.omnilab.livewee.logproducer;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * Basic configurations for a producer
 *
 * Created by chenxm on 1/20/15.
 */
public class ConfLoader {

    public final int UDPPort;
    public final int bufSize;
    public final String brokers;
    public final String topic;
    public final String serializer;

    public ConfLoader() throws IOException {
        this(Consts.PRODUCER_CONFIG_DEFAULT);
    }
    
    /**
     * Load configuration from file
     */
    public ConfLoader(String confFile) throws IOException {
        Properties props = new Properties();
        URL url = ClassLoader.getSystemResource(confFile);
        props.load(url.openStream());

        UDPPort = Integer.parseInt(props.getProperty(Consts.SYSLOG_SOURCE_PORT));
        bufSize = Integer.parseInt(props.getProperty(Consts.SYSLOG_BUFFER_SIZE));
        brokers = props.getProperty(Consts.KAFKA_BROKERS);
        topic = props.getProperty(Consts.KAFKA_TOPIC);
        serializer = props.getProperty(Consts.KAFKA_SERIALIZER);
    }
}

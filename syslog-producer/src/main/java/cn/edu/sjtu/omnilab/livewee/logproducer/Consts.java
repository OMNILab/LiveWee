package cn.edu.sjtu.omnilab.livewee.logproducer;

/**
 * Global constants used by classes.
 */
public class Consts {

    public final static String PRODUCER_CONFIG_DEFAULT = "conf.properties";

    // Configuration fields
    public final static String SYSLOG_SOURCE_PORT = "syslogproducer.uport";
    public final static String SYSLOG_BUFFER_SIZE = "syslogproducer.buffersize";
    public final static String KAFKA_BROKERS = "syslogproducer.kafka.brokers";
    public final static String KAFKA_TOPIC = "syslogproducer.kafka.topic";
    public final static String KAFKA_SERIALIZER = "syslogproducer.kafka.serializer";
}

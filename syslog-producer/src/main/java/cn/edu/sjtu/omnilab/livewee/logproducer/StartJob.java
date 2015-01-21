package cn.edu.sjtu.omnilab.livewee.logproducer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class StartJob {

    private static final Logger logger =
            LogManager.getLogger(StartJob.class.getName());

    public static void main(String[] args) throws IOException {

        // Producer for raw syslog
        ConfLoader conf = new ConfLoader();
        DefaultProducer producer = new DefaultProducer(conf);
        
        // Producer for ETLed syslog
        ConfLoader confETL = new ConfLoader(Consts.PRODUCER_CONFIG_ETLED);
        DefaultProducer producerETL = new DefaultProducer(confETL);
        
        logger.info("Listening UDP " + conf.UDPPort);
        logger.info("Sending messages to " + conf.brokers);
        
        // Read syslog message from UDP socket
        DatagramSocket socket = new DatagramSocket(conf.UDPPort);
        DatagramPacket packet = new DatagramPacket(
                new byte[conf.bufSize], conf.bufSize);

        // Send message to Kafka
        while (true) {
            socket.receive(packet);
            String msg = new String(packet.getData(), 0, packet.getLength());
            producer.sendMessage(msg);

            String messageETL = LogProcessor.cleanse(msg);
            if (messageETL != null) {
                producerETL.sendMessage(messageETL);
            }
        }
    }
}

package cn.edu.sjtu.omnilab.livewee.logproducer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class StartJob {

    private static final Logger logger =
            LogManager.getLogger(AbstractProducer.class.getName());

    public static void main(String[] args) throws IOException {

        ProducerSettings settings = new ProducerSettings();
        DefaultProducer producer = new DefaultProducer(settings);
        
        logger.info("Listening UDP " + settings.UDPPort);
        logger.info("Sending messages to " + settings.brokers);
        
        // Read syslog message from UDP socket
        DatagramSocket rcvSocket = new DatagramSocket(settings.UDPPort);
        DatagramPacket rcvPacket = new DatagramPacket(
                new byte[settings.bufSize], settings.bufSize);

        // Send message to Kafka
        while (true) {
            rcvSocket.receive(rcvPacket);
            String msg = new String(rcvPacket.getData(), 0, rcvPacket.getLength());
            producer.sendMessage(msg);
        }
    }
}

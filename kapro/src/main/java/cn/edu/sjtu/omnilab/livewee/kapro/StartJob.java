package cn.edu.sjtu.omnilab.livewee.kapro;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class StartJob {

    private static final Logger logger =
            LogManager.getLogger(StartJob.class.getName());

    public static void main(String[] args) throws IOException, ParseException {

        // Params for UDP port, brokers, topic
        Options options = new Options();
        options.addOption("h", "help", false, "print this message");
        options.addOption("p", "port", true, "local UDP port of source message");
        options.addOption("b", "brokers", true,
                "broker servers separated by commas");
        options.addOption("t", "topic", true, "name of Kafka topic " +
                "(ETLed topic is appended with 'etled' automatically)");

        CommandLineParser parser = new GnuParser();
        CommandLine cmd = parser.parse(options, args);

        // Producer with default configuration
        ConfLoader conf = new ConfLoader();
        DefaultProducer producer = new DefaultProducer(conf);
        
        // Update configuration with user defined values
        if (cmd.hasOption("help")){
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( StartJob.class.getName(), options, true);
            System.exit(-1);
        }
        if (cmd.hasOption("port"))
            conf.UDPPort = Integer.parseInt(cmd.getOptionValue("port"));
        if (cmd.hasOption("broker-list"))
            conf.brokers = cmd.getOptionValue("broker-list");
        if (cmd.hasOption("topic"))
            conf.topic = cmd.getOptionValue("topic");

        // Producer for ETLed messages
        ConfLoader confETL = new ConfLoader(conf);
        confETL.topic += ".etled";
        DefaultProducer producerETL = new DefaultProducer(confETL);

        logger.info("Listening UDP " + conf.UDPPort);
        logger.info("Sending messages to " + conf.brokers);

        // Read messages from UDP port
        DatagramSocket socket = new DatagramSocket(conf.UDPPort);
        DatagramPacket packet = new DatagramPacket(
                new byte[conf.bufSize], conf.bufSize);

        // Send message to Kafka
        while (true) {
            socket.receive(packet);
            String msg = new String(packet.getData(), 0, packet.getLength());
            producer.sendMessage(msg);

            String messageETL = WifilogFilter.cleanse(msg);
            if (messageETL != null) {
                producerETL.sendMessage(messageETL);
            }
        }
    }
}
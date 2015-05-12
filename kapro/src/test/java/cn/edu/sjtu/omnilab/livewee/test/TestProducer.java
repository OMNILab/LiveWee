package cn.edu.sjtu.omnilab.livewee.test;

import java.io.*;
import java.util.Properties;

import cn.edu.sjtu.omnilab.livewee.kapro.ConfLoader;
import cn.edu.sjtu.omnilab.livewee.kapro.WifilogFilter;
import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;
import org.junit.Assert;
import org.junit.Test;

public class TestProducer {

    @Test
    public void testSettings() throws IOException {
        ConfLoader settings = new ConfLoader();
        Assert.assertEquals("arubasyslog", settings.topic);
    }

    @Test
    public void testProducerCreation() {
        String topic = "test";
        Properties props = new Properties();
        props.put("metadata.broker.list", "10.50.4.73:9092");
        props.put("serializer.class", "kafka.serializer.StringEncoder");

        Producer<Integer, String> producer =
                new Producer<Integer, String>(new ProducerConfig(props));
    }
    
    @Test
    public void testLogCleanse() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        FileReader ifile = new FileReader(classLoader.getResource("syslog.txt").getFile());
        BufferedReader reader = new BufferedReader(ifile);

        String line = null;
        while ((line = reader.readLine()) != null) {
            String clean = WifilogFilter.cleanse(line);
            if (clean != null)
                System.out.println(clean);
        }
    }
}

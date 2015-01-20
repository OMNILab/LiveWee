package cn.edu.sjtu.omnilab.livewee.logproducer;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by chenxm on 1/20/15.
 */
public abstract class AbstractProducer {
    
    protected static final Logger logger =
            LogManager.getLogger(AbstractProducer.class.getName());

    public AbstractProducer() {}
    
    public AbstractProducer(ProducerSettings settings) {}

    public abstract void sendMessage(String message);
}

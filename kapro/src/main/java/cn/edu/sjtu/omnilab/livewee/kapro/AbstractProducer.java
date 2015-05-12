package cn.edu.sjtu.omnilab.livewee.kapro;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Basic abstract producer definition.
 */
public abstract class AbstractProducer {
    
    protected static final Logger logger =
            LogManager.getLogger(AbstractProducer.class.getName());

    public AbstractProducer() {}
    
    public AbstractProducer(ConfLoader conf) {}

    public abstract void sendMessage(String message);
}

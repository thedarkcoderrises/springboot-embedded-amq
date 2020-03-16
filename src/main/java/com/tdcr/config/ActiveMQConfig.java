package com.tdcr.config;

import org.apache.activemq.broker.BrokerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@EnableJms
@Configuration
public class ActiveMQConfig {

    @Bean
    public BrokerService broker() throws Exception {
        BrokerService broker = new BrokerService();
        broker.setBrokerName("embedded");
        broker.addConnector("tcp://localhost:61616");
        broker.setSchedulerSupport(true);
        broker.setPersistent(false);
        broker.setUseJmx(true);
        return broker;
    }


    @Bean
    public MBeanServerConnection amqConnection(BrokerService brokerService) throws IOException {
        Map<String, String[]> environment = null;
        JMXServiceURL url =
                new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi");
        String[] credentials = new String[2];
        credentials[0] = "admin";
        credentials[1] = "activemq";
        environment = new HashMap<String, String[]>();
        environment.put(JMXConnector.CREDENTIALS, credentials);

        MBeanServerConnection conn = JMXConnectorFactory.connect(url,
                environment).getMBeanServerConnection();
        return conn;
    }
}

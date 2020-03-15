package com.tdcr.config;

import org.apache.activemq.broker.BrokerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;

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
        return broker;
    }
}

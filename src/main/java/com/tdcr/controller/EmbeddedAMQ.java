package com.tdcr.controller;

import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.jms.Session;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

@RestController
public class EmbeddedAMQ {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private MBeanServerConnection amqConnection;

    @PostConstruct()
    void initEvent(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000); // broker initialize time
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                jmsTemplate.convertAndSend("schedule","test");
            }
        });

        thread.start();
    }


    @RequestMapping("/send")
    public String send(@RequestParam("myMessage") String myMessage) {
        jmsTemplate.convertAndSend("taskexchange", myMessage);
        return myMessage;
    }


    @JmsListener(destination = "taskexchange")
    public void receiveMessage(@Payload String myMessage,
                               @Headers MessageHeaders headers,
                               Message message, Session session) {
        System.out.println("received <" + myMessage + ">");

        System.out.println("- - - - - - - - - - - - - - - - - - - - - - - -");
        System.out.println("######          Message Details           #####");
        System.out.println("- - - - - - - - - - - - - - - - - - - - - - - -");
        System.out.println("headers: " + headers);
        System.out.println("message: " + message);
        System.out.println("session: " + session);
        System.out.println("- - - - - - - - - - - - - - - - - - - - - - - -");
    }

    @JmsListener(destination = "destroy")
    public void noteConsumer(@Payload String queueName){

        String scheduleQueue = queueName.split("_")[0];
        String reminderQueue = queueName.split("_")[1];

        try{
            ObjectName activeMQ = new ObjectName("org.apache.activemq:type=Broker,brokerName=embedded");
            BrokerViewMBean mbean = (BrokerViewMBean) MBeanServerInvocationHandler.newProxyInstance(amqConnection,
                    activeMQ, BrokerViewMBean.class, true);

            for (ObjectName name : mbean.getQueues()) {
                QueueViewMBean queueMbean = (QueueViewMBean)
                        MBeanServerInvocationHandler.newProxyInstance(amqConnection, name,
                                QueueViewMBean.class, true);


                if(reminderQueue.equals(queueMbean.getName()) && queueMbean.getConsumerCount() == 0){
                    Thread.sleep(3000);
                        queueMbean.purge();
                }

                if(scheduleQueue.equals(queueMbean.getName()) && queueMbean.getConsumerCount() == 0){
                    jmsTemplate.convertAndSend(scheduleQueue,"test");
                    System.out.println("Scheduled after complete destroy");
                }
            }

        }catch (Exception e){

        }

    }

}

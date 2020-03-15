package com.tdcr.controller;

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

@RestController
public class EmbeddedAMQ {

    @Autowired
    private JmsTemplate jmsTemplate;


    @PostConstruct()
    void initEvent(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
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

}

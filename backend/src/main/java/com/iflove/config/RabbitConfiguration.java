package com.iflove.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 苍镜月
 * @version 1.0
 * @implNote
 */
@Configuration
public class RabbitConfiguration {
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean("emailQueue")
    public Queue emailQueue() {
        return QueueBuilder
                .durable("mail")
                .build();
    }
}

package com.codereview.assistant.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String REVIEW_QUEUE = "code-review-queue";
    public static final String REVIEW_EXCHANGE = "code-review-exchange";
    public static final String REVIEW_ROUTING_KEY = "code.review";

    @Bean
    public Queue reviewQueue() {
        return QueueBuilder.durable(REVIEW_QUEUE)
            .withArgument("x-message-ttl", 3600000) // 1 hour TTL
            .build();
    }

    @Bean
    public DirectExchange reviewExchange() {
        return new DirectExchange(REVIEW_EXCHANGE);
    }

    @Bean
    public Binding reviewBinding(Queue reviewQueue, DirectExchange reviewExchange) {
        return BindingBuilder.bind(reviewQueue)
            .to(reviewExchange)
            .with(REVIEW_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                        MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}

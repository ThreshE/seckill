package com.seckill.stock.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StockMqConfig {

    public static final String EXCHANGE = "seckill.stock.exchange";
    public static final String QUEUE = "seckill.stock.decrease.queue";
    public static final String ROUTING_KEY = "seckill.stock.decrease";

    @Bean
    public DirectExchange stockExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue stockDecreaseQueue() {
        return QueueBuilder.durable(QUEUE)
                .withArgument("x-single-active-consumer", true)
                .build();
    }

    @Bean
    public Binding stockDecreaseBinding(Queue stockDecreaseQueue, DirectExchange stockExchange) {
        return BindingBuilder.bind(stockDecreaseQueue).to(stockExchange).with(ROUTING_KEY);
    }
}

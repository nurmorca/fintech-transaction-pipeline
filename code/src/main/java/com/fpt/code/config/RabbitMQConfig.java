package com.fpt.code.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "transactions.exchange";
    public static final String TRANSACTION_INITIATED_QUEUE = "transaction.initiated.queue";
    public static final String TRANSACTION_INITIATED_KEY = "transaction.initiated";
    public static final String FRAUD_CHECK_FAILED_KEY = "fraud.check.failed";
    public static final String FRAUD_CHECK_FAILED_QUEUE = "fraud.check.failed.queue";
    public static final String FRAUD_CHECK_PASSED_KEY = "fraud.check.passed";
    public static final String FRAUD_CHECK_PASSED_QUEUE = "fraud.check.passed.queue";


    @Bean
    public TopicExchange transactionsExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue transactionInitiatedQueue() {
        return new Queue(TRANSACTION_INITIATED_QUEUE, true);
    }

    @Bean
    public Binding transactionInitiatedBinding(Queue transactionInitiatedQueue, TopicExchange transactionsExchange) {
        return BindingBuilder
                .bind(transactionInitiatedQueue)
                .to(transactionsExchange)
                .with(TRANSACTION_INITIATED_KEY);
    }

    @Bean
    public Queue fraudCheckFailedQueue() {
        return new Queue(FRAUD_CHECK_FAILED_QUEUE, true);
    }

    @Bean
    public Binding fraudCheckFailedBinding(Queue fraudCheckFailedQueue, TopicExchange transactionsExchange) {
        return BindingBuilder
                .bind(fraudCheckFailedQueue)
                .to(transactionsExchange)
                .with(FRAUD_CHECK_FAILED_KEY);
    }

    @Bean
    public Queue fraudCheckPassedQueue() {
        return new Queue(FRAUD_CHECK_PASSED_QUEUE, true);
    }

    @Bean
    public Binding fraudCheckPassedBinding(Queue fraudCheckPassedQueue, TopicExchange transactionsExchange) {
        return BindingBuilder
                .bind(fraudCheckPassedQueue)
                .to(transactionsExchange)
                .with(FRAUD_CHECK_PASSED_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
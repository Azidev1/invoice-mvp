package ch.azidev.invoicemvp.messaging;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.*;

@Configuration
public class RabbitConfig {

    @Bean DirectExchange documentsExchange() { return new DirectExchange("documents.exchange"); }
    @Bean DirectExchange dlx() { return new DirectExchange("documents.dlx"); }

    @Bean Queue ocrQueue() {
        return QueueBuilder.durable("documents.ocr.queue")
                .withArgument("x-dead-letter-exchange", "documents.dlx")
                .withArgument("x-dead-letter-routing-key", "documents.ocr.dlq")
                .build();
    }

    @Bean Queue ocrDlq() { return QueueBuilder.durable("documents.ocr.dlq").build(); }

    @Bean Binding bindOcr(DirectExchange documentsExchange, Queue ocrQueue) {
        return BindingBuilder.bind(ocrQueue).to(documentsExchange).with("documents.uploaded");
    }

    @Bean Binding bindDlq(DirectExchange dlx, Queue ocrDlq) {
        return BindingBuilder.bind(ocrDlq).to(dlx).with("documents.ocr.dlq");
    }
}

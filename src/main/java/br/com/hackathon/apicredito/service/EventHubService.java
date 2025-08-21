package br.com.hackathon.apicredito.service;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.function.Consumer;

@Slf4j
@Service
public class EventHubService {

    private final EventHubProducerAsyncClient producerAsyncClient;

    public EventHubService(@Value("${spring.cloud.azure.eventhubs.connection-string}") String connectionString) {
        this.producerAsyncClient = new EventHubClientBuilder()
                .connectionString(connectionString)
                .buildAsyncProducerClient();
    }

    public void enviarSimulacao(String simulacaoJson, Runnable onSuccess, Consumer<Throwable> onError) {
        EventData eventData = new EventData(simulacaoJson);

        log.info("[EventHub] Agendando envio assíncrono.");

        producerAsyncClient.send(Collections.singletonList(eventData)).subscribe(
                null,
                onError,
                onSuccess
        );
    }
}
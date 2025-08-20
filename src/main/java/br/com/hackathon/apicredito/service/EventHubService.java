package br.com.hackathon.apicredito.service;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
public class EventHubService {

    private final EventHubProducerClient producerClient;
    private final ObjectMapper objectMapper;

    public EventHubService(@Value("${azure.eventhub.connection-string}") String connectionString, ObjectMapper objectMapper) {
        this.producerClient = new EventHubClientBuilder()
                .connectionString(connectionString)
                .buildProducerClient();
        this.objectMapper = objectMapper;
    }

    public void enviarSimulacao(Object simulacaoDto) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(simulacaoDto);
            EventData eventData = new EventData(jsonPayload);
            producerClient.send(Collections.singletonList(eventData));
            log.info("Simulação enviada para o Event Hub.");
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar DTO para JSON", e);
        } catch (Exception e) {
            log.error("Erro ao enviar evento para o Event Hub", e);
        }
    }
}
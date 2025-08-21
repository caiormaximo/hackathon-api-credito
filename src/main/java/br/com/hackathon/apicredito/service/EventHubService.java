package br.com.hackathon.apicredito.service;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventHubService {

    private final EventHubProducerClient producerClient;

    public void enviarSimulacao(String simulacaoJson) {
        EventDataBatch eventDataBatch = producerClient.createBatch();
        EventData eventData = new EventData(simulacaoJson);

        if (!eventDataBatch.tryAdd(eventData)) {
            producerClient.send(eventDataBatch);
            eventDataBatch = producerClient.createBatch();

            if (!eventDataBatch.tryAdd(eventData)) {
                log.error("Evento de simulação é muito grande para um lote vazio. Tamanho: {} bytes.", eventData.getBodyAsBinaryData().getLength());
                return;
            }
        }

        if (eventDataBatch.getCount() > 0) {
            try {
                producerClient.send(eventDataBatch);
                log.info("Evento de simulação enviado com sucesso para o Event Hub.");
            } catch (Exception e) {
                log.error("Erro ao enviar evento para o Event Hub", e);
            }
        }
    }
}
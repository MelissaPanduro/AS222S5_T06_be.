package pe.edu.vallegrande.inteligencia.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import pe.edu.vallegrande.inteligencia.model.ChatResponseModel;
import pe.edu.vallegrande.inteligencia.repository.ChatInteractionRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ChatbotResponseService {

    private final ChatInteractionRepository chatInteractionRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${api.chatgpt.url}")
    private String apiUrl;

    @Value("${api.chatgpt.key}")
    private String apiKey;

    @Value("${api.chatgpt.host}")
    private String apiHost;

    public ChatbotResponseService(ChatInteractionRepository chatInteractionRepository, RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.chatInteractionRepository = chatInteractionRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public Flux<ChatResponseModel> findAllResponses() {
        return chatInteractionRepository.findAll();
    }

    public ChatResponseModel findResponseById(Long id) {
        Mono<ChatResponseModel> response = chatInteractionRepository.findById(id);
        return response.defaultIfEmpty(null).block();
    }
    
    
    public Mono<ChatResponseModel> saveAndGetResponse(String query) {
    String url = "https://chatgpt-ai-chat-bot.p.rapidapi.com/ask";

    // Crear y configurar las cabeceras
    HttpHeaders headers = new HttpHeaders();
    headers.set("x-rapidapi-key", apiKey);
    headers.set("x-rapidapi-host", apiHost);
    headers.set("Content-Type", "application/json");

    // Limpiar el query para evitar caracteres no válidos
    String cleanedQuery = query.replace("\r", "").replace("\n", "").replace("\"", "\\\"");
    String requestBody = String.format("{\"query\":\"%s\"}", cleanedQuery);

    HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

    try {
        // Realizar la solicitud POST
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class);

        // Convertir la respuesta JSON a ChatResponseModel
        String responseBody = responseEntity.getBody();
        if (responseBody != null) {
            // Crear y guardar el ChatResponseModel en la base de datos
            ChatResponseModel chatResponse = objectMapper.readValue(responseBody, ChatResponseModel.class);
            chatResponse.setQuery(cleanedQuery); // Guardar el query original limpio
            chatResponse.setCreatedAt(LocalDateTime.now()); // Establecer la fecha de creación
            return chatInteractionRepository.save(chatResponse); // Guardar en la base de datos
        } else {
            return null;
        }

    } catch (HttpClientErrorException e) {
        // Manejar errores específicos de HTTP
        System.err.println("Error making API call: " + e.getMessage());
        return null;
    } catch (Exception e) {
        // Manejar errores de deserialización
        System.err.println("Error deserializing JSON response: " + e.getMessage());
        return null;
    }
}

 // Método para eliminar una respuesta
public Mono<Boolean> deleteResponseById(Long id) {
    return chatInteractionRepository.findById(id)
        .flatMap(existingResponse -> chatInteractionRepository.delete(existingResponse)
            .then(Mono.just(true))) // Si se elimina, retorna true
        .defaultIfEmpty(false); // Si no se encuentra, retorna false
}

public Mono<ChatResponseModel> editResponse(Long id, String newQuery) {
    return chatInteractionRepository.findById(id)
        .flatMap(existingResponse -> {
            // Asegúrate de que existingResponse no sea null
            if (existingResponse == null) {
                return Mono.error(new Throwable("Response not found for ID: " + id)); // Lanza una excepción personalizada si no se encuentra
            }

            // Actualiza el query
            existingResponse.setQuery(newQuery);

            // Realiza una nueva solicitud a la API para obtener la nueva respuesta
            return saveAndGetResponse(existingResponse.getQuery())
                .flatMap(newChatResponse -> {
                    existingResponse.setResponse(newChatResponse.getResponse());
                    existingResponse.setCreatedAt(LocalDateTime.now()); // Actualiza la fecha
                    return chatInteractionRepository.save(existingResponse);
                });
        })
        .switchIfEmpty(Mono.error(new Throwable("Response not found for ID: " + id))); // Maneja el caso de que no se encuentre la respuesta
}


}

package pe.edu.vallegrande.inteligencia.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pe.edu.vallegrande.inteligencia.model.ChatResponseModel;
import pe.edu.vallegrande.inteligencia.service.ChatbotResponseService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController

@RequestMapping("/api/chatbot")

@CrossOrigin(origins = "https://potential-trout-g9gg6qggj5jcppv4-4200.app.github.dev") //cambiar url del front

public class ChatController {

    private final ChatbotResponseService chatbotResponseService;

    @Autowired
    public ChatController(ChatbotResponseService chatbotResponseService) {
        this.chatbotResponseService = chatbotResponseService;
    }

    @GetMapping("/responses")
    public ResponseEntity<Flux<ChatResponseModel>> getAllResponses() {
        Flux<ChatResponseModel> responses = chatbotResponseService.findAllResponses();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/responses/{id}")
    public ResponseEntity<ChatResponseModel> getResponseById(@PathVariable Long id) {
        ChatResponseModel response = chatbotResponseService.findResponseById(id);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/responses")
    public ResponseEntity<Mono<ChatResponseModel>> createResponse(@RequestBody String query) {
        Mono<ChatResponseModel> chatResponse = chatbotResponseService.saveAndGetResponse(query);
        return ResponseEntity.ok(chatResponse);
    }

    // Método para eliminar una respuesta
    @DeleteMapping("responses/{id}")
public Mono<Void> deleteQuery(@PathVariable Long id) {
    return chatInteractionRepository.deleteById(id)
        .switchIfEmpty(Mono.error(new RuntimeException("Consulta no encontrada")));
}


    @PutMapping("/responses/{id}")
public ResponseEntity<ChatResponseModel> updateChatResponse(@PathVariable Long id, @RequestBody ChatResponseModel updatedResponse) {
    Optional<ChatResponseModel> existingResponse = chatResponseRepository.findById(id);
    if (existingResponse.isPresent()) {
        ChatResponseModel response = existingResponse.get();
        response.setQuery(updatedResponse.getQuery());
        response.setResponse(updatedResponse.getResponse());
        response.setCreatedAt(updatedResponse.getCreatedAt()); // Si necesitas actualizar la fecha
        chatResponseRepository.save(response);
        return ResponseEntity.ok(response);
    }
    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
}



}

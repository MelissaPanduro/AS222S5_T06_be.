package pe.edu.vallegrande.inteligencia.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pe.edu.vallegrande.inteligencia.model.ChatResponseModel;
import pe.edu.vallegrande.inteligencia.service.ChatbotResponseService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


@CrossOrigin("*")
@RestController
@RequestMapping("/api/chatbot")
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
    @DeleteMapping("/responses/{id}")
    public Mono<ResponseEntity<Void>> deleteResponse(@PathVariable Long id) {
        return chatbotResponseService.deleteResponseById(id)
            .map(isDeleted -> {
                if (isDeleted) {
                    return ResponseEntity.noContent().build(); // 204 No Content
                } else {
                    return ResponseEntity.notFound().build(); // 404 Not Found
                }
            });
    }

    @PutMapping("/responses/{id}")
public Mono<ResponseEntity<ChatResponseModel>> editResponse(
        @PathVariable Long id,
        @RequestBody String newQuery) {
    return chatbotResponseService.editResponse(id, newQuery)
        .map(updatedResponse -> ResponseEntity.ok(updatedResponse))
        .defaultIfEmpty(ResponseEntity.notFound().build());
}

   
    
}


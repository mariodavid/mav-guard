package com.example.demo;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Einfache Demo-Anwendung für das MavGuard-Beispielprojekt.
 */
@Configuration
public class SimpleDemoApplication {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public MessageService messageService() {
        return new MessageService();
    }

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SimpleDemoApplication.class);
        
        MessageService messageService = context.getBean(MessageService.class);
        String message = messageService.getMessage();
        
        System.out.println(StringUtils.repeat("=", 50));
        System.out.println(message);
        System.out.println(StringUtils.repeat("=", 50));
        
        context.close();
    }

    /**
     * Einfacher Service für Nachrichten.
     */
    public static class MessageService {
        public String getMessage() {
            return "Willkommen zum MavGuard-Beispielprojekt!";
        }
    }
}
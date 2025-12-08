package com.example.authenticationservice.mapper;

import com.example.authenticationservice.dto.SessionData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JsonConverter {

    private final ObjectMapper mapper = new ObjectMapper();

    public <T> T toObject(String json, Class<T> clazz)  {
        try{
            return mapper.readValue(json,clazz);
        }  catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public <T> String toJson(T value){
        try{
            return mapper.writeValueAsString(value);
        }  catch (JsonProcessingException e) {
            log.error("Failed to serialize object of type {}: ", e.getMessage());
            throw new RuntimeException(e);
        }

    }



}

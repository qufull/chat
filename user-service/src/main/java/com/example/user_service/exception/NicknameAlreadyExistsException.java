package com.example.user_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class NicknameAlreadyExistsException extends RuntimeException{

    private final String nickname;

    public NicknameAlreadyExistsException(String nickname) {
        super("Nickname already taken: " + nickname);
        this.nickname = nickname;
    }

}

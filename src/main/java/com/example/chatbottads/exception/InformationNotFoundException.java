package com.example.chatbottads.exception;

public class InformationNotFoundException extends RuntimeException {

    public InformationNotFoundException(String id) {
        super(id);
    }
}

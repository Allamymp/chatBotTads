package com.example.chatbottads.exception;

public class SectorNotFoundException extends RuntimeException {

    public SectorNotFoundException(String id) {
        super(id);
    }
}

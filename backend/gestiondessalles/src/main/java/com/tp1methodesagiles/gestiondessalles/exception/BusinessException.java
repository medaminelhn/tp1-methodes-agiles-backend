package com.tp1methodesagiles.gestiondessalles.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
package com.affiliateLink.common;

public class InvalidStatusTransitionException extends RuntimeException {
    
    public InvalidStatusTransitionException(String message){
        super(message);
    }
}

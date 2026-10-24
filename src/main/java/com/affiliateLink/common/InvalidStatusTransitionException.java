package com.affiliateLink.common;

public class InvalidStatusTransitionException extends RuntimeException {// 409
    
    public InvalidStatusTransitionException(String message){
        super(message);
    }
}

package com.affiliateLink.common;

public class DuplicateEmailException extends RuntimeException {// 409
    public DuplicateEmailException(String message){
        super(message);
    }
}

package com.affiliateLink.common;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(){
        super("Email déjà utilisé");
    }
}

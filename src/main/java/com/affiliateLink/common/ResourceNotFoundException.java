package com.affiliateLink.common;

public class ResourceNotFoundException extends  RuntimeException{
    public ResourceNotFoundException(String msg){
        super(msg);
    }
}

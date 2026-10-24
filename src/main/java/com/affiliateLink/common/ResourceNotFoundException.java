package com.affiliateLink.common;

public class ResourceNotFoundException extends  RuntimeException{// 404
    public ResourceNotFoundException(String msg){
        super(msg);
    }
}

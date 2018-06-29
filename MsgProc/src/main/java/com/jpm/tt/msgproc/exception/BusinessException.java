package com.jpm.tt.msgproc.exception;

public class BusinessException extends Exception {
    
    private static final long serialVersionUID = -8648979639304197953L;
    
    private String message;

    
    public BusinessException(final String errorMessage) {
        this.message = errorMessage;
    }
    
    
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    
    
}

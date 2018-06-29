package com.jpm.tt.msgproc.model;

import java.math.BigDecimal;

public class Adjustment {

    private String productName;
    private BigDecimal priceBf;
    private String operation;
    private BigDecimal adjValue;
    
    public Adjustment(final String productName, final BigDecimal priceBf, final String operation, final BigDecimal adjValue){
        this.productName = productName;
        this.priceBf = priceBf;
        this.operation = operation;
        this.adjValue = adjValue;
    }
    
    /**
     * GETTERS / SETTERS
     * */

    public String getProductName() {
        return productName;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getPriceBf() {
        return priceBf;
    }
    public void setPriceBf(BigDecimal priceBf) {
        this.priceBf = priceBf;
    }

    public String getOperation() {
        return operation;
    }
    public void setOperation(String operation) {
        this.operation = operation;
    }

    public BigDecimal getAdjValue() {
        return adjValue;
    }
    public void setAdjValue(BigDecimal adjValue) {
        this.adjValue = adjValue;
    }
    
}

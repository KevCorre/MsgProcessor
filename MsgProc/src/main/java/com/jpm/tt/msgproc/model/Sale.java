package com.jpm.tt.msgproc.model;

import java.math.BigDecimal;

public class Sale {
    private String productType;
    private BigDecimal value;
    
    public Sale(final String productType, final BigDecimal value){
        this.productType = productType;
        this.value = value;
    }
    
    /**
     * GETTERS / STTERS
     */
    public String getProductType() {
        return productType;
    }
    public void setProductType(String productType) {
        this.productType = productType;
    }
    
    public BigDecimal getValue() {
        return value;
    }
    public void setValue(BigDecimal value) {
        this.value = value;
    }
    
    
}

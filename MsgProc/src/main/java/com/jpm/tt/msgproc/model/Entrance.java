package com.jpm.tt.msgproc.model;

/**
 * Object to contains entrance messages in historic
 * */
public class Entrance {

    private Sale sale;
    private int occurrence;
    
    public Entrance (final Sale sale, final int occurrence){
        this.sale = sale;
        this.occurrence = occurrence;
    }

    /**
     * GETTERS / SETTERS
     * */
    public Sale getSale() {
        return sale;
    }
    public void setSale(Sale sale) {
        this.sale = sale;
    }

    public int getOccurrence() {
        return occurrence;
    }
    public void setOccurrence(int occurrence) {
        this.occurrence = occurrence;
    }
}

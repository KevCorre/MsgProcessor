package com.jpm.tt.msgproc.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.jpm.tt.msgproc.MsgProcessor;
import com.jpm.tt.msgproc.exception.BusinessException;
import com.jpm.tt.msgproc.exception.ExceptionMessage;
import com.jpm.tt.msgproc.model.Adjustment;
import com.jpm.tt.msgproc.model.Entrance;
import com.jpm.tt.msgproc.model.Sale;
import com.jpm.tt.msgproc.util.MsgProcConstants;

public class MsgProcessorImpl implements MsgProcessor {
    
    private final static String OP_ADD = "add";
    private final static String OP_SUB = "substract";
    private final static String OP_MUL = "multiply";
    
    private int msgCounterTot = 0;
    private int msgCounterReport = 0;
    
    public List<Entrance> entranceHistoric = new ArrayList<Entrance>();
    public HashMap<String, BigDecimal> totalByProduct = new HashMap<String, BigDecimal>();
    public HashMap<String, BigDecimal> pricesByProduct = new HashMap<String, BigDecimal>();
    public List<Adjustment> adjustmentsReceived = new ArrayList<Adjustment>();
    public HashMap<String, String> adjByProduct = new HashMap<String, String>();
    
    public int notifySale(final Sale sale) throws BusinessException {
        return notifySale(sale, 1);
    }

    public int notifySale(final Sale receicedSale, final int occurrence) throws BusinessException{

        // Control entrance limit
        if (msgCounterTot == MsgProcConstants.MSG_ENTRANCE_LIMIT) return -1;

        // Control occurrence
        if(occurrence < 1){
            throw new BusinessException(ExceptionMessage.BAD_OCCURRENCE);
        }

        // Add entrance to history
        final String product = receicedSale.getProductType().toLowerCase();
        Sale formattedSale = new Sale(product, receicedSale.getValue());
        Entrance e = new Entrance(formattedSale, occurrence);
        entranceHistoric.add(e);

        // Add product to processed memory
        BigDecimal productPrice = pricesByProduct.get(product);
        if(null == productPrice){
            productPrice = formattedSale.getValue();
            pricesByProduct.put(product, productPrice);
            totalByProduct.put(product, BigDecimal.ZERO);
        }

        // Compute values
        BigDecimal totForReceived = computeReceivedSale(productPrice, occurrence);
        BigDecimal totForProduct = totalByProduct.get(product).add(totForReceived);
        totalByProduct.replace(product, totForProduct);
        
        logProcess();

        return 0;
    }

    public int notifySale(final Sale sale,final String operation,final BigDecimal adjustment) throws BusinessException {

        // Control entrance limit
        if (msgCounterTot == MsgProcConstants.MSG_ENTRANCE_LIMIT) return -1;
        
        // Control operation
        controlOperation(operation);

        final String prodName = sale.getProductType().toLowerCase();
        if(null == pricesByProduct.get(prodName)){
            throw new BusinessException(ExceptionMessage.BAD_ADJUSTMENT_NOPROD);
        }
        
        Adjustment receivedAdj = new Adjustment(prodName, pricesByProduct.get(prodName), operation.toLowerCase(), adjustment);
        
        BigDecimal newPrice = computeAdjustmentForProduct(operation, receivedAdj, adjustment);
        
        BigDecimal newTotal = computeNewTot(pricesByProduct.get(prodName), totalByProduct.get(prodName), newPrice);
        
        String adjTxt = initAdjustmentTxtForProduct(prodName, operation, adjustment.toPlainString());
        
        adjByProduct.put(prodName, adjTxt);
        adjustmentsReceived.add(receivedAdj);
        pricesByProduct.replace(prodName, newPrice);
        totalByProduct.replace(prodName, newTotal);

        logProcess();
        
        return 0;
    }

    private BigDecimal computeReceivedSale(final BigDecimal prodPrice, final int prodOccur){
        
        BigDecimal prodOccurBD = new BigDecimal(prodOccur);
        
        return prodPrice.multiply(prodOccurBD);
    }
    
    private BigDecimal computeNewTot(final BigDecimal oldPrice, final BigDecimal oldTotal, final BigDecimal newPrice){
        BigDecimal newTotal = BigDecimal.ZERO;
        
        BigDecimal qtt = oldTotal.divide(oldPrice);
        newTotal = newPrice.multiply(qtt);
        
        return newTotal;
    }
    
    /**
     * Control if operation field is null or empty
     * If yes, throws an Exception
     * */
    private void controlOperation(final String operation) throws BusinessException{
        if(null == operation || "" == operation){
            throw new BusinessException(ExceptionMessage.BAD_OPERATION_EMPTY);
        }
        
    }
    
    private void logProcess(){
        
        msgCounterTot ++;
        msgCounterReport ++;
        
        if(msgCounterReport == MsgProcConstants.REPORT_LOG_STAGE) {
            printSaleReport();
            
            msgCounterReport = 0;
            if(msgCounterTot == MsgProcConstants.MSG_ENTRANCE_LIMIT) {
                pauseSystem();
            }
        }
    }
    
    private void printSaleReport(){

        String prodName;
        BigDecimal total;
        BigDecimal unitPrice;
        BigDecimal nbSale;

        System.out.println("\nStep Report:");
        for(Entry<String, BigDecimal> entry: totalByProduct.entrySet()) {
            prodName = entry.getKey();
            total = entry.getValue();
            unitPrice = pricesByProduct.get(prodName);
            nbSale = total.divide(unitPrice);

            System.out.println("For " + prodName + " - " + nbSale + " sales - for a total of: " + total + " £");
        }

    }
    
    private void pauseSystem(){
        System.out.println("\nSystem starting pause step ...");
        
        String prodName;
        
        for(Entry<String, String> entry: adjByProduct.entrySet()){
            prodName = entry.getKey();
            System.out.println(entry.getValue() + "for final price: " + totalByProduct.get(prodName));
        }
    }
    
    private String initAdjustmentTxtForProduct(final String productName, final String operation, final String adjVal){
        String adjTxt = adjByProduct.get(productName);
        
        if(null == adjTxt) {
            adjTxt = "Adjustments For ";
            adjTxt = adjTxt.concat(productName);
            adjTxt = adjTxt.concat("are :\n");
        }
        
        adjTxt = adjTxt.concat(operation + " " + adjVal + ", ");
        
        return adjTxt;
    }
    
    private BigDecimal computeAdjustmentForProduct(final String operation, final Adjustment receivedAdj, final BigDecimal adjustmentVal) throws BusinessException{
        BigDecimal newPrice = BigDecimal.ZERO;
        
        if(OP_ADD.equalsIgnoreCase(operation)){
            newPrice = receivedAdj.getPriceBf().add(adjustmentVal);
        }
        else if (OP_SUB.equalsIgnoreCase(operation)){
            newPrice = receivedAdj.getPriceBf().subtract(adjustmentVal);
        }
        else if (OP_MUL.equalsIgnoreCase(operation)){
            newPrice = receivedAdj.getPriceBf().multiply(adjustmentVal);
        }
        else {
            throw new BusinessException(ExceptionMessage.BAD_OPERATION_NOTVALID);
        }
        
        return newPrice;
    }
}

package com.jpm.tt.msgproc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import com.jpm.tt.msgproc.exception.BusinessException;
import com.jpm.tt.msgproc.exception.ExceptionMessage;
import com.jpm.tt.msgproc.impl.MsgProcessorImpl;
import com.jpm.tt.msgproc.model.Sale;

public class TUMsgProcessor {

    private MsgProcessorImpl msgProc = new MsgProcessorImpl();
    
    private Sale apple;
    private Sale banana;
    private Sale kiwi;
    
    @Before
    public void initCases(){
        String prodName = "apple";
        BigDecimal prodPrice = new BigDecimal("0.1");
        apple = new Sale(prodName, prodPrice);
        
        prodName = "banana";
        prodPrice = new BigDecimal("0.12");
        banana = new Sale(prodName, prodPrice);
        
        prodName = "kiwi";
        prodPrice = new BigDecimal("0.3");
        kiwi = new Sale(prodName, prodPrice);
    }
    

    @Test
    public void testMsgTp1_Ok() throws BusinessException{

        // -- Act --
        int response = msgProc.notifySale(apple);

        // -- Assert --
        assertEquals(0, response);
        assertEquals(1, msgProc.entranceHistoric.size());
        assertEquals(apple.getValue(), msgProc.totalByProduct.get(apple.getProductType()));
    }
    
    @Test
    public void testMsgTp2_Ok() throws BusinessException{

        // -- Prepare --
        int occurences = 3;
        BigDecimal totalExpected = new BigDecimal("0.3");

        // -- Act --
        int response = msgProc.notifySale(apple, occurences);

        // -- Assert --
        assertEquals(0, response);
        assertEquals(1, msgProc.entranceHistoric.size());
        assertEquals(totalExpected, msgProc.totalByProduct.get(apple.getProductType()));
    }
    
    @Test
    public void testMsgTp2_Ko(){

        // -- Prepare --
        int occurences = -1;

        // -- Act --
        try {
            msgProc.notifySale(apple, occurences);
            fail();
        } catch (BusinessException catchedBe) {
            assertEquals(ExceptionMessage.BAD_OCCURRENCE, catchedBe.getMessage());
        }

        // -- Assert --
        assertEquals(0, msgProc.entranceHistoric.size());
        assertEquals(null,  msgProc.totalByProduct.get(apple.getProductType()));
    }
    
    @Test
    public void testMsgTp3_Ok() throws BusinessException{

        // -- Prepare --
        BigDecimal adjVal = new BigDecimal("0.05");
        BigDecimal expectedTotal = new BigDecimal("0.15");
        String operation = "add";

        // -- Act --
        msgProc.notifySale(apple);
        msgProc.notifySale(apple, operation, adjVal);

        // -- Assert --
        final String prodName = apple.getProductType();
        assertEquals(1, msgProc.entranceHistoric.size());
        assertEquals(expectedTotal,  msgProc.totalByProduct.get(prodName));
        assertNotNull(msgProc.adjByProduct.get(prodName));
        assertTrue(msgProc.adjByProduct.get(prodName).contains(operation));
    }
    
    @Test
    public void testMsgTp3_KoNoProduct() throws BusinessException{

        try {
            // -- Act --
            msgProc.notifySale(apple, "add", new BigDecimal("0.05"));
            fail();
        } catch (BusinessException be) {
            assertEquals(ExceptionMessage.BAD_ADJUSTMENT_NOPROD, be.getMessage());
        }
    }
    
    @Test
    public void testMsgTp3_KoBadOp() throws BusinessException{

        try {
            // -- Act --
            msgProc.notifySale(apple);
            msgProc.notifySale(apple, "azere", new BigDecimal("0.05"));
            fail();
        } catch (BusinessException be) {
            assertEquals(ExceptionMessage.BAD_OPERATION_NOTVALID, be.getMessage());
        }
    }
    
    @Test
    public void testMsgTp3_KoNoOp() throws BusinessException{

        try {
            // -- Act --
            msgProc.notifySale(apple);
            msgProc.notifySale(apple, "", new BigDecimal("0.05"));
            fail();
        } catch (BusinessException be) {
            assertEquals(ExceptionMessage.BAD_OPERATION_EMPTY, be.getMessage());
        }
    }
    
    @Test
    public void testMsgTp1_11times_Ok() throws BusinessException{

        // -- Act --
        for(int i=0; i<5; i++){
            msgProc.notifySale(apple);
        }
        for(int i=0; i<6; i++){
            msgProc.notifySale(banana);
        }

        // -- Asserts --
        final BigDecimal totForApples = new BigDecimal("0.5");
        final BigDecimal totForBananas = new BigDecimal("0.72");
        assertEquals((11), msgProc.entranceHistoric.size());
        assertEquals(totForApples, msgProc.totalByProduct.get(apple.getProductType()));
        assertEquals(apple.getValue(), msgProc.pricesByProduct.get(apple.getProductType()));
        assertEquals(totForBananas, msgProc.totalByProduct.get(banana.getProductType()));
        assertEquals(banana.getValue(), msgProc.pricesByProduct.get(banana.getProductType()));
    }
    
    @Test
    public void testMsgTp1n2_21times_Ok() throws BusinessException{
        
        // -- Act --
        for(int i=0; i<11; i++){
            msgProc.notifySale(apple);
        }
        msgProc.notifySale(banana,6);
        for(int i=0; i<7; i++){
            msgProc.notifySale(banana);
        }
        msgProc.notifySale(apple,4);
        msgProc.notifySale(banana,3);

        // -- Asserts --
        final BigDecimal totForApples = new BigDecimal("1.5");
        final BigDecimal totForBananas = new BigDecimal("1.92");
        assertEquals((21), msgProc.entranceHistoric.size());
        assertEquals(totForApples, msgProc.totalByProduct.get(apple.getProductType()));
        assertEquals(apple.getValue(), msgProc.pricesByProduct.get(apple.getProductType()));
        assertEquals(totForBananas, msgProc.totalByProduct.get(banana.getProductType()));
        assertEquals(banana.getValue(), msgProc.pricesByProduct.get(banana.getProductType()));
    }

    @Test
    public void testMsgMix_Ok() throws BusinessException{
        
        // -- Act --
        for(int i=0; i<11; i++){
            msgProc.notifySale(apple);
        }
        msgProc.notifySale(apple,4);
        msgProc.notifySale(apple, "substract", new BigDecimal("0.02"));
        msgProc.notifySale(banana,6);
        for(int i=0; i<7; i++){
            msgProc.notifySale(banana);
        }
        msgProc.notifySale(banana,3);

        msgProc.notifySale(apple, "multiply", new BigDecimal("2"));

        // -- Asserts --
        final BigDecimal totForApples = new BigDecimal("2.40");
        final BigDecimal totForBananas = new BigDecimal("1.92");
        assertEquals((21), msgProc.entranceHistoric.size());
        assertEquals(totForApples, msgProc.totalByProduct.get(apple.getProductType()));
        assertEquals(new BigDecimal("0.16"), msgProc.pricesByProduct.get(apple.getProductType()));
        assertEquals(totForBananas, msgProc.totalByProduct.get(banana.getProductType()));
        assertEquals(banana.getValue(), msgProc.pricesByProduct.get(banana.getProductType()));
    }
    
    @Test
    public void testMsgMix_PauseSystem_Ok() throws BusinessException{
        
        // -- Act --
        for(int i=0; i<11; i++){
            msgProc.notifySale(apple);
        }
        msgProc.notifySale(apple,4);
        msgProc.notifySale(apple, "substract", new BigDecimal("0.02"));
        msgProc.notifySale(banana,6);
        for(int i=0; i<7; i++){
            msgProc.notifySale(banana);
        }
        msgProc.notifySale(banana,3);

        msgProc.notifySale(apple, "multiply", new BigDecimal("2"));
        
        for(int i=0; i<28; i++){
            msgProc.notifySale(kiwi);
        }

        int lastResp = msgProc.notifySale(kiwi);

        // -- Asserts --
        assertEquals(-1, lastResp);
        assertEquals(48, msgProc.entranceHistoric.size());
        
        final BigDecimal totForApples = new BigDecimal("2.40");
        assertEquals(totForApples, msgProc.totalByProduct.get(apple.getProductType()));
        assertEquals(new BigDecimal("0.16"), msgProc.pricesByProduct.get(apple.getProductType()));

        final BigDecimal totForBananas = new BigDecimal("1.92");
        assertEquals(totForBananas, msgProc.totalByProduct.get(banana.getProductType()));
        assertEquals(banana.getValue(), msgProc.pricesByProduct.get(banana.getProductType()));

        final BigDecimal totForKiwis = new BigDecimal("8.1");
        assertEquals(totForKiwis, msgProc.totalByProduct.get(kiwi.getProductType()));
        assertEquals(kiwi.getValue(), msgProc.pricesByProduct.get(kiwi.getProductType()));
    }
}

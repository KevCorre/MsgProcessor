package com.jpm.tt.msgproc;

import java.math.BigDecimal;

import com.jpm.tt.msgproc.exception.BusinessException;
import com.jpm.tt.msgproc.model.Sale;

public interface MsgProcessor {

    public int notifySale(final Sale sale) throws BusinessException;
    
    public int notifySale(final Sale sale, final int occurrence) throws BusinessException;
    
    public int notifySale(final Sale sale, final String operation, final BigDecimal adjustment) throws BusinessException;
}

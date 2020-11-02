package com.example.demo.model;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
public class Transfer {

    private Long paymentId;
    private BigDecimal amount;
    private String paymentAccount;
    private String receiverAccount;
    private String paymentDocument;
}

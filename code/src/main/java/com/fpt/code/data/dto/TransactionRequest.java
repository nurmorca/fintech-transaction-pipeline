package com.fpt.code.data.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {
    @NotBlank
    private String sender;
    @NotBlank
    private String receiver;
    @NotNull
    @Positive
    private BigDecimal amount;
}

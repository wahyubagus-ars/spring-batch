package com.spring.batch.domain.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class Transaction(
    val id: Long,
    val accountNumber: String,
    val amount: BigDecimal,
    val transactionDate: LocalDateTime
)
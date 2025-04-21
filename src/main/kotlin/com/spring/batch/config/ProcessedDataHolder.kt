package com.spring.batch.config

import com.spring.batch.domain.dto.Transaction
import org.springframework.stereotype.Component

@Component
class ProcessedDataHolder {
    val processedChunks = mutableListOf<List<Transaction>>()

    fun getFlattenedData(): List<Transaction> {
        return processedChunks.flatten()
    }
}

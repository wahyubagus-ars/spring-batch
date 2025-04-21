package com.spring.batch.config

import org.springframework.batch.core.partition.support.Partitioner
import org.springframework.batch.item.ExecutionContext
import org.springframework.stereotype.Component

@Component
class TransactionPartitioner : Partitioner {

    override fun partition(gridSize: Int): Map<String, ExecutionContext> {
        val partitionMap = mutableMapOf<String, ExecutionContext>()

        // Example: Split data into `gridSize` partitions based on ID ranges
        val totalRecords = 100L // Assume 100 total records
        val recordsPerPartition = totalRecords / gridSize
        var startId = 1L

        for (i in 0 until gridSize) {
            val partitionName = "partition$i"
            val endId = if (i == gridSize - 1) totalRecords else startId + recordsPerPartition - 1

            val context = ExecutionContext()
            context.putLong("minId", startId)
            context.putLong("maxId", endId)

            partitionMap[partitionName] = context
            startId = endId + 1
        }

        return partitionMap
    }
}

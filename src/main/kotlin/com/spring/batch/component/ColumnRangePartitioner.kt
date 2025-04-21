package com.spring.batch.component

import org.springframework.batch.core.partition.support.Partitioner
import org.springframework.batch.item.ExecutionContext
import javax.sql.DataSource
import kotlin.math.min

class ColumnRangePartitioner(private val dataSource: DataSource) : Partitioner {

    lateinit var column: String
    lateinit var table: String

    override fun partition(gridSize: Int): Map<String, ExecutionContext> {
        val result = mutableMapOf<String, ExecutionContext>()
        dataSource.connection.use { connection ->
            val ps = connection.prepareStatement("SELECT MIN($column), MAX($column) FROM $table")
            val rs = ps.executeQuery()
            if (rs.next()) {
                val min = rs.getLong(1)
                val max = rs.getLong(2)
                val targetSize = (max - min) / gridSize + 1

                for (i in 0 until gridSize) {
                    val start = min + i * targetSize
                    val end = min(start + targetSize - 1, max)

                    val context = ExecutionContext().apply {
                        putLong("minValue", start)
                        putLong("maxValue", end)
                    }
                    result["partition$i"] = context
                }
            }
        }
        return result
    }
}

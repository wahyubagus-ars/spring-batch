package com.spring.batch.config.job

import com.spring.batch.config.ProcessedDataHolder
import com.spring.batch.domain.dto.Transaction
import mu.KotlinLogging
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.listener.JobExecutionListenerSupport
import org.springframework.stereotype.Component

@Component
class JobCompletionListener(
    private val processedDataHolder: ProcessedDataHolder
) : JobExecutionListener {

    private val log = KotlinLogging.logger {}

    override fun afterJob(jobExecution: JobExecution) {
        if (jobExecution.exitStatus.exitCode == ExitStatus.COMPLETED.exitCode) {
            val processedData = processedDataHolder.getFlattenedData()
            log.info { "Batch processing complete. Generating report for ${processedData.size} transactions." }
            generateReport(processedData)
        }
    }

    private fun generateReport(data: List<Transaction>) {
        // Example report generation logic
        data.forEach {
            log.info { "Report Entry: ID=${it.id}, Account=${it.accountNumber}, Amount=${it.amount}, Date=${it.transactionDate}" }
        }

        log.info { "Successfully generate the report" }
    }
}


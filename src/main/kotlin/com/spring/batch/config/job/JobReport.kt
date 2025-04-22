package com.spring.batch.config.job

import com.spring.batch.config.ProcessedDataHolder
import com.spring.batch.domain.dto.Transaction
import mu.KotlinLogging
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JdbcCursorItemReader
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.RetryListener
import org.springframework.transaction.PlatformTransactionManager
import java.math.BigDecimal
import javax.sql.DataSource

@Configuration
@EnableBatchProcessing
class JobReport(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val dataSource: DataSource,
    private val processedDataHolder: ProcessedDataHolder,
) {
    private val processedChunks = mutableListOf<List<Transaction>>()
    private val log = KotlinLogging.logger {}

    @Bean
    fun reportJob(jobCompletionListener: JobCompletionListener): Job {
        return JobBuilder("reportJob", jobRepository)
            .listener(jobCompletionListener)
            .start(chunkStep())
            .build()
    }

    @Bean
    fun chunkStep(): Step {
        return StepBuilder("chunkStep", jobRepository)
            .chunk<Transaction, Transaction>(20)
            .reader(itemReader())
            .processor(itemProcessor())
            .writer(itemWriter(processedDataHolder))
            .faultTolerant()
            .retryLimit(3)
            .retry(IllegalArgumentException::class.java)
            .skipLimit(3)
            .skip(IllegalArgumentException::class.java)
            .listener(retryListener())
            .transactionManager(transactionManager)
            .build()
    }

    @Bean
    fun retryListener(): RetryListener {
        return object : RetryListener {
            override fun <T, E : Throwable> open(context: RetryContext, callback: RetryCallback<T, E>): Boolean {
                // Open is called at the beginning of every retry attempt
                if (context.retryCount > 0) { // Only log if it's an actual retry
                    log.info { "Starting retry operation for attempt ${context.retryCount}" }
                }
                return true
            }

            override fun <T, E : Throwable> onError(context: RetryContext, callback: RetryCallback<T, E>, throwable: Throwable) {
                // Log each retry attempt due to an error
                log.warn { "Retry attempt ${context.retryCount} failed due to: ${throwable.message}" }
            }

            override fun <T, E : Throwable> close(context: RetryContext, callback: RetryCallback<T, E>, throwable: Throwable?) {
                // Log completion of the retry operation, either success or failure
                if (throwable == null) {
                    if (context.retryCount > 0) { // Only log if there were retries
                        log.info { "Retry operation continued after executed ${context.retryCount} attempts" }
                    }
                } else {
                    log.error { "Retry operation failed after ${context.retryCount} attempts due to: ${throwable.message}" }
                }
            }
        }
    }


    @Bean
    fun itemReader(): JdbcCursorItemReader<Transaction> {
        return JdbcCursorItemReaderBuilder<Transaction>()
            .name("transactionItemReader")
            .dataSource(dataSource)
            .sql(
                """
                SELECT id, account_number, amount, transaction_date
                FROM transactions
                """
            )
            .rowMapper { rs, _ ->
                Transaction(
                    id = rs.getLong("id"),
                    accountNumber = rs.getString("account_number"),
                    amount = rs.getBigDecimal("amount"),
                    transactionDate = rs.getTimestamp("transaction_date").toLocalDateTime()
                )
            }
            .build()
    }

    @Bean
    fun itemProcessor(): ItemProcessor<Transaction, Transaction> {
        return ItemProcessor { transaction ->
            if (transaction.amount.compareTo(BigDecimal.ZERO) == 0) {
                // Throw exception to trigger retry
                throw IllegalArgumentException("Transaction amount is zero, retrying...")
            }
            // Example logic to adjust data, if necessary
            transaction.copy(amount = transaction.amount.multiply(1.1.toBigDecimal()))
        }
    }


    @Bean
    fun itemWriter(processedDataHolder: ProcessedDataHolder): ItemWriter<Transaction> {
        return ItemWriter { chunk ->
            val items = chunk.toList()
            processedDataHolder.processedChunks.add(items)
            log.info {"Chunk processed: $items"}
        }
    }


    @Bean
    fun getProcessedChunks(): List<List<Transaction>> {
        return processedChunks // Expose the collected chunks for further use
    }
}
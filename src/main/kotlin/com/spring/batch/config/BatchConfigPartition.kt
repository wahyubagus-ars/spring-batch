//package com.spring.batch.config
//
//import com.spring.batch.domain.dto.Transaction
//import mu.KotlinLogging
//import org.springframework.batch.core.Job
//import org.springframework.batch.core.Step
//import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
//import org.springframework.batch.core.configuration.annotation.StepScope
//import org.springframework.batch.core.job.builder.JobBuilder
//import org.springframework.batch.core.repository.JobRepository
//import org.springframework.batch.core.step.builder.StepBuilder
//import org.springframework.batch.item.ItemProcessor
//import org.springframework.batch.item.ItemReader
//import org.springframework.batch.item.ItemWriter
//import org.springframework.batch.item.database.JdbcCursorItemReader
//import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.core.task.SimpleAsyncTaskExecutor
//import org.springframework.core.task.TaskExecutor
//import org.springframework.transaction.PlatformTransactionManager
//import javax.sql.DataSource
//
//@Configuration
//@EnableBatchProcessing
//class BatchConfigPartition (
//    private val jobRepository: JobRepository,
//    private val transactionManager: PlatformTransactionManager,
//    private val dataSource: DataSource,
//    private val processedDataHolder: ProcessedDataHolder,
//    private val jobCompletionListener: JobCompletionListener,
//    private val transactionPartitioner: TransactionPartitioner
//    ) {
//
//    private val log = KotlinLogging.logger {}
//
//    @Bean
//    fun partitionedStep(): Step {
//        return StepBuilder("partitionedStep", jobRepository)
//            .chunk<Transaction, Transaction>(20)
//            .reader(partitionedReader(null, null)) // Inject minId, maxId at runtime
//            .processor(itemProcessor())
//            .writer(itemWriter(processedDataHolder))
//            .transactionManager(transactionManager)
//            .build()
//    }
//
//    @Bean
//    fun itemProcessor(): ItemProcessor<Transaction, Transaction> {
//        return ItemProcessor { transaction ->
//            // Example processing logic
//            transaction.amount.multiply(1.1.toBigDecimal())?.let {
//                transaction.copy(
//                    amount = it // Increase amount by 10%
//                )
//            }
//        }
//    }
//
//    @Bean
//    fun itemWriter(processedDataHolder: ProcessedDataHolder): ItemWriter<Transaction> {
//        return ItemWriter { chunk ->
//            val items = chunk.toList() // Convert Chunk to List
//            processedDataHolder.processedChunks.add(items)
//            println("Chunk processed: $items")
//        }
//    }
//
//    @Bean
//    @StepScope
//    fun partitionedReader(
//        @Value("#{stepExecutionContext['minId']}") minId: Long?,
//        @Value("#{stepExecutionContext['maxId']}") maxId: Long?
//    ): JdbcCursorItemReader<Transaction> {
//        log.info { "minandmx $minId and $maxId" }
//        return JdbcCursorItemReaderBuilder<Transaction>()
//            .name("transactionItemReader")
//            .dataSource(dataSource)
//            .sql(
//                """
//            SELECT id, account_number, amount, transaction_date
//            FROM transactions
//            WHERE id BETWEEN ? AND ?
//            """
//            )
//            .rowMapper { rs, _ ->
//                Transaction(
//                    id = rs.getLong("id"),
//                    accountNumber = rs.getString("account_number"),
//                    amount = rs.getBigDecimal("amount"),
//                    transactionDate = rs.getTimestamp("transaction_date").toLocalDateTime()
//                )
//            }
//            .queryArguments(listOf(minId, maxId)) // Using positional parameters
//            .build()
//    }
//
//
//    @Bean
//    fun partitionStep(transactionPartitioner: TransactionPartitioner): Step {
//        return StepBuilder("partitionStep", jobRepository)
//            .partitioner("partitionedStep", transactionPartitioner)
//            .step(partitionedStep()) // Define the step for each partition
//            .gridSize(5) // Number of partitions
//            .taskExecutor(taskExecutor()) // Enable parallel processing
//            .build()
//    }
//
//    @Bean
//    fun taskExecutor(): TaskExecutor {
//        return SimpleAsyncTaskExecutor().apply {
//            setConcurrencyLimit(5) // Number of threads
//        }
//    }
//
//    @Bean
//    fun partitionedJob(): Job {
//        return JobBuilder("partitionedJob", jobRepository)
//            .start(partitionStep(transactionPartitioner))
//            .build()
//    }
//
//}
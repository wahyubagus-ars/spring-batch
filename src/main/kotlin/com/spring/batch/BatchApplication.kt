package com.spring.batch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.task.configuration.EnableTask

@SpringBootApplication
@EnableTask
class BatchApplication

fun main(args: Array<String>) {
	runApplication<BatchApplication>(*args)
}

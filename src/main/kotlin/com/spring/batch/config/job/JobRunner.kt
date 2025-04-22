package com.spring.batch.config.job;

import mu.KotlinLogging
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.configuration.JobRegistry
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.NoSuchJobException
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class JobRunner(
    private val jobLauncher: JobLauncher,
    private val jobRegistry: JobRegistry,
    private val applicationArguments: ApplicationArguments
) : CommandLineRunner {

    private val log = KotlinLogging.logger {}

    override fun run(vararg args: String?) {
        log.info { "Available jobs: ${jobRegistry.jobNames.joinToString()}" }

        val jobToRun = determineJobToRun()

        val jobParameters = JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .addString("job.name", jobToRun.name)
            .toJobParameters()

        log.info { "Launching job: ${jobToRun.name}" }
        jobLauncher.run(jobToRun, jobParameters)
    }

    private fun determineJobToRun(): Job {
        val requestedJobName = applicationArguments.getOptionValues("job.name")
            ?.firstOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: run {
                log.warn { "No job specified, defaulting to reportJob" }
                "reportJob"
            }

        // Append "Job" suffix if not present (optional)
        val normalizedJobName = if (requestedJobName.endsWith("Job")) requestedJobName else "${requestedJobName}Job"

        return try {
            jobRegistry.getJob(requestedJobName).also {
                log.debug { "Successfully retrieved job: ${it.name}" }
            }
        } catch (e: NoSuchJobException) {
            log.error { "Job '$requestedJobName' not found. Available jobs: ${jobRegistry.jobNames}" }
            throw e
        }
    }
}


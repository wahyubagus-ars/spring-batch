//package com.spring.batch.component.scheduler
//
//@Component
//class JobScheduler(private val jobLauncher: JobLauncher, private val reportJob: Job) {
//
//    @Scheduled(cron = "0 0 1 * * ?") // Run at 1 AM every day
//    fun runJob() {
//        val jobParams = JobParametersBuilder()
//            .addLong("timestamp", System.currentTimeMillis())
//            .toJobParameters()
//
//        jobLauncher.run(reportJob, jobParams)
//    }
//}

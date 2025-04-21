package com.spring.batch.domain.dto

import mu.KotlinLogging
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import java.io.File
import java.math.BigDecimal

data class PdfDocument(
    private val transactions: MutableList<Transaction> = mutableListOf(),
    private var totalAmount: BigDecimal = BigDecimal.ZERO
) {

    private val log = KotlinLogging.logger {}

    // Add a transaction and update total amount
    fun addTransaction(transaction: Transaction) {
        log.info { "transaction: $transaction" }
        transactions.add(transaction)
        totalAmount = totalAmount.add(transaction.amount)
    }

    // Add summary of transactions at the end
    fun addSummary(contentStream: PDPageContentStream) {
        val summary = """
            Summary:
            Total Transactions: ${transactions.size}
            Total Amount: $totalAmount
        """.trimIndent()

        contentStream.showText("Summary:")
        contentStream.newLine()
        contentStream.showText("Total Transactions: ${transactions.size}")
        contentStream.newLine()
        contentStream.showText("Total Amount: $totalAmount")
        contentStream.newLine()
    }

    fun saveToFile(filePath: String) {
        log.info { "filepath: $filePath" }
        log.info { "transactions: $transactions" }
//        // Create a new PDF document
//        val document = PDDocument()
//
//        // Start a new page
//        var page = PDPage()
//        document.addPage(page)
//
//        // Create content stream for the first page
//        var contentStream = PDPageContentStream(document, page)
//        contentStream.use { stream ->
//            stream.beginText()  // Always start with beginText before showText
//            stream.setFont(PDType1Font.HELVETICA_BOLD, 14f)
//            stream.setLeading(16.5f)  // Adjust leading for better readability
//            stream.newLineAtOffset(50f, 750f) // Starting position of the text
//
//            // Title
//            stream.showText("PDF Report for Transactions:")
//            stream.newLine()
//
//            // Transactions
//            var yPosition = 730f
//            transactions.forEach { transaction ->
//                // Check if content fits on current page, if not, add a new page
//                if (yPosition < 100f) {
//                    // Create a new page and reset the y-position
//                    page = PDPage()
//                    document.addPage(page)
//                    yPosition = 750f
//                    stream.endText() // Close the current content stream
//
//                    // Create a new stream for the new page
//                    contentStream = PDPageContentStream(document, page)
//                    contentStream.use { newStream ->
//                        newStream.beginText() // Begin text for the new page
//                        newStream.setFont(PDType1Font.HELVETICA, 12f)
//                        newStream.setLeading(14.5f)
//                        newStream.newLineAtOffset(50f, yPosition)
//
//                        // Write the transaction to the new page
//                        newStream.showText(transaction.toString())
//                        newStream.newLine()
//                        yPosition -= 15f  // Move down after each transaction
//                    }
//                } else {
//                    // Write the transaction to the current page
//                    stream.showText(transaction.toString())
//                    stream.newLine()
//                    yPosition -= 15f  // Move down after each transaction
//                }
//            }
//
//            // Add Summary Section
//            if (yPosition < 100f) {
//                // Create a new page for the summary
//                page = PDPage()
//                document.addPage(page)
//                yPosition = 750f
//                stream.endText() // End the previous content stream
//
//                // Start a new content stream for the summary
//                contentStream = PDPageContentStream(document, page)
//                contentStream.use { newStream ->
//                    newStream.beginText()  // Ensure beginText() is called here
//                    newStream.setFont(PDType1Font.HELVETICA_BOLD, 14f)
//                    newStream.setLeading(16.5f)
//                    newStream.newLineAtOffset(50f, yPosition)
//
//                    // Add the summary
//                    addSummary(newStream) // Use the addSummary method to add the summary content
//                    newStream.endText() // End the summary section text
//                }
//            }
//
//            stream.endText() // End the content stream after writing all transactions
//        }
//
//        // Save the document
//        val file = File(filePath)
//        val parentDir = file.parentFile
//        if (parentDir != null && !parentDir.exists()) {
//            parentDir.mkdirs()
//        }
//
//        document.save(file)
//        document.close() // Always close the document after saving
    }

}

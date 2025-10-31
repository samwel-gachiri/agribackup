package com.agriconnect.farmersportalapis.buyers.application.util

import com.lowagie.text.DocumentException
import freemarker.template.Configuration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.xhtmlrenderer.pdf.ITextRenderer
import java.io.*
import java.nio.file.Files

@Service
class BSPdfUtils(private val configuration: Configuration) {
    var logger: Logger = LoggerFactory.getLogger(BSPdfUtils::class.java)

    fun createPdfFile(template: String, destinationFileName: String) {
        try {
            val fileOutputStream = FileOutputStream(destinationFileName)
            val renderer = ITextRenderer()
//            renderer.fontResolver.addFontDirectory("fonts", BaseFont.NOT_EMBEDDED)
//            renderer.fontResolver.addFont("fonts/trebuc.ttf", BaseFont.NOT_EMBEDDED)
            renderer.setDocumentFromString(template)
            renderer.layout()
            renderer.createPDF(fileOutputStream)
            renderer.finishPDF()
        } catch (e: FileNotFoundException) {
            logger.error(e.message)
        } catch (e: DocumentException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun downloadReportPdf(data: List<*>, fileName: String, title: String): ByteArray {
        val filePath = File(fileName).path
        val model: MutableMap<String, Any> = HashMap()
        model["orders"] = data
        model["orderTitle"] = title
        val stringWriter = StringWriter()
        val temp = configuration.getTemplate("revenue_report.ftlh")
        temp.process(model, stringWriter)
        val template = stringWriter.buffer.toString()
        createPdfFile(template, filePath)
        val file = File(filePath)
        val result: ByteArray = Files.readAllBytes(file.toPath())
        file.delete()
        return result
    }
}
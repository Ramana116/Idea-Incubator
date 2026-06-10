package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.model.StartupReport
import java.io.OutputStream

object PdfExporter {

    // standard A4 dimensions in PostScript points: 595 x 842
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    
    fun exportBusinessPlanToPdf(
        context: Context,
        report: StartupReport,
        outputStream: OutputStream
    ) {
        val pdfDocument = PdfDocument()
        
        // Define clean typographic paints for professional branding
        val textPaint = Paint().apply {
            color = Color.rgb(33, 37, 41) // Dark charcoal
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        
        val boldPaint = Paint().apply {
            color = Color.rgb(33, 37, 41)
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val titlePaint = Paint().apply {
            color = Color.rgb(10, 132, 255) // Electric blue
            textSize = 22f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(100, 116, 139) // Slate grey
            textSize = 12f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val sectionHeadingPaint = Paint().apply {
            color = Color.rgb(10, 132, 255)
            textSize = 14f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val footerPaint = Paint().apply {
            color = Color.rgb(148, 163, 184)
            textSize = 8f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
            isAntiAlias = true
        }
        
        val linePaint = Paint().apply {
            color = Color.rgb(226, 232, 240) // Slate200
            strokeWidth = 1f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val accentLinePaint = Paint().apply {
            color = Color.rgb(10, 132, 255)
            strokeWidth = 2f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val margin = 50f
        val contentWidth = PAGE_WIDTH - (margin * 2)
        val startY = 70f
        val endY = PAGE_HEIGHT - 60f
        
        var currentPageNumber = 0
        var currentPdfPage: PdfDocument.Page? = null
        var canvas: Canvas? = null
        var currentY = startY

        fun startNewPage() {
            currentPdfPage?.let {
                canvas?.let { c ->
                    val pageNumStr = "Page $currentPageNumber"
                    val rightAlignX = PAGE_WIDTH - margin - footerPaint.measureText(pageNumStr)
                    c.drawText(pageNumStr, rightAlignX, PAGE_HEIGHT - 35f, footerPaint)
                    c.drawText("AI Idea Validator — Business Plan Report", margin, PAGE_HEIGHT - 35f, footerPaint)
                    c.drawLine(margin, PAGE_HEIGHT - 45f, PAGE_WIDTH - margin, PAGE_HEIGHT - 45f, linePaint)
                }
                pdfDocument.finishPage(it)
            }

            currentPageNumber++
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            currentPdfPage = page
            canvas = page.canvas
            currentY = startY
            
            canvas?.let { c ->
                if (currentPageNumber > 1) {
                    c.drawText("Startup Business Plan: ${report.name.uppercase()}", margin, 40f, footerPaint)
                    c.drawLine(margin, 46f, PAGE_WIDTH - margin, 46f, linePaint)
                    currentY = 65f
                }
            }
        }

        startNewPage()
        
        canvas?.let { c ->
            c.drawText("BUSINESS PLAN BLUEPRINT", margin, currentY, subtitlePaint)
            currentY += 15f
            
            c.drawText(report.name, margin, currentY + 15f, titlePaint)
            currentY += 45f
            
            c.drawLine(margin, currentY, PAGE_WIDTH - margin, currentY, accentLinePaint)
            currentY += 25f
            
            c.drawText("Sector: ${report.detectedIndustry} | Model: ${report.recommendedModel}", margin, currentY, boldPaint)
            currentY += 15f
            c.drawText("Target Profile: ${report.targetUsers}", margin, currentY, textPaint)
            currentY += 15f
            c.drawText("Market Potential: ${report.potentialMarkets}", margin, currentY, textPaint)
            currentY += 35f
        }

        fun drawParagraph(text: String, isBold: Boolean = false, heading: String? = null) {
            val paint = if (isBold) boldPaint else textPaint
            
            if (heading != null) {
                if (currentY + 35f > endY) {
                    startNewPage()
                } else {
                    currentY += 15f
                }
                canvas?.drawText(heading.uppercase(), margin, currentY, sectionHeadingPaint)
                currentY += 8f
                canvas?.drawLine(margin, currentY, margin + 40f, currentY, accentLinePaint)
                currentY += 18f
            }

            val words = text.split(" ")
            var lineBuilder = StringBuilder()
            
            for (word in words) {
                val testLine = if (lineBuilder.isEmpty()) word else "${lineBuilder} $word"
                val lineWidth = paint.measureText(testLine)
                if (lineWidth > contentWidth) {
                    if (currentY + 14f > endY) {
                        startNewPage()
                    }
                    canvas?.drawText(lineBuilder.toString(), margin, currentY, paint)
                    currentY += 15f
                    lineBuilder = StringBuilder(word)
                } else {
                    lineBuilder.append(if (lineBuilder.isEmpty()) word else " $word")
                }
            }
            
            if (lineBuilder.isNotEmpty()) {
                if (currentY + 14f > endY) {
                    startNewPage()
                }
                canvas?.drawText(lineBuilder.toString(), margin, currentY, paint)
                currentY += 20f
            }
        }

        drawParagraph(
            text = report.businessPlan.executiveSummary, 
            heading = "1. Executive Summary"
        )

        drawParagraph(
            text = report.businessPlan.marketAnalysisDetail, 
            heading = "2. Deep Market Sizing"
        )

        drawParagraph(
            text = report.businessPlan.customerSegments, 
            heading = "3. Customer Segments & Demographics"
        )

        drawParagraph(
            text = report.businessPlan.revenueStreams, 
            heading = "4. Revenue Streams & Unit Economics"
        )

        drawParagraph(
            text = report.businessPlan.costStructure, 
            heading = "5. Cost Structures & Allocation"
        )

        drawParagraph(
            text = report.businessPlan.marketingPlan, 
            heading = "6. Digital Acquisition & Marketing Strategy"
        )

        drawParagraph(
            text = report.businessPlan.growthStrategy, 
            heading = "7. 5-Year Scaling & Long-Term Vision"
        )

        if (currentY + 40f > endY) {
            startNewPage()
        }
        
        startNewPage()
        
        canvas?.let { c ->
            c.drawText("APPENDICES & FINANCIAL DIAGNOSTICS", margin, currentY, subtitlePaint)
            currentY += 15f
            c.drawText("Market Sizing Benchmarks:", margin, currentY, sectionHeadingPaint)
            currentY += 20f
            
            c.drawText("TAM (Total Market)", margin, currentY, boldPaint)
            c.drawText(report.tamValue, margin + 250f, currentY, boldPaint)
            currentY += 15f
            c.drawText("SAM (Segment Serviceable)", margin, currentY, textPaint)
            c.drawText(report.samValue, margin + 250f, currentY, textPaint)
            currentY += 15f
            c.drawText("SOM (Obtainable Market)", margin, currentY, textPaint)
            c.drawText(report.somValue, margin + 250f, currentY, textPaint)
            currentY += 15f
            c.drawText("CAGR Project Sizing Rate", margin, currentY, textPaint)
            c.drawText(report.yearlyGrowthRate, margin + 250f, currentY, textPaint)
            currentY += 25f
            
            c.drawLine(margin, currentY, PAGE_WIDTH - margin, currentY, linePaint)
            currentY += 20f
            
            c.drawText("Assessed Startup Risk Matrix:", margin, currentY, sectionHeadingPaint)
            currentY += 20f
        }
        
        for (risk in report.risks) {
            if (currentY + 45f > endY) {
                startNewPage()
            }
            canvas?.drawText("${risk.riskName.uppercase()} (${risk.category}) — SEVERITY: ${risk.severity.uppercase()}", margin, currentY, boldPaint)
            currentY += 15f
            
            val words = risk.solution.split(" ")
            var lineBuilder = StringBuilder()
            val indent = 15f
            for (word in words) {
                val testLine = if (lineBuilder.isEmpty()) word else "${lineBuilder} $word"
                val lineWidth = textPaint.measureText(testLine)
                if (lineWidth > (contentWidth - indent)) {
                    if (currentY + 14f > endY) {
                        startNewPage()
                    }
                    canvas?.drawText(lineBuilder.toString(), margin + indent, currentY, textPaint)
                    currentY += 15f
                    lineBuilder = StringBuilder(word)
                } else {
                    lineBuilder.append(if (lineBuilder.isEmpty()) word else " $word")
                }
            }
            if (lineBuilder.isNotEmpty()) {
                if (currentY + 14f > endY) {
                    startNewPage()
                }
                canvas?.drawText(lineBuilder.toString(), margin + indent, currentY, textPaint)
                currentY += 22f
            }
        }

        currentPdfPage?.let {
            canvas?.let { c ->
                val pageNumStr = "Page $currentPageNumber"
                val rightAlignX = PAGE_WIDTH - margin - footerPaint.measureText(pageNumStr)
                c.drawText(pageNumStr, rightAlignX, PAGE_HEIGHT - 35f, footerPaint)
                c.drawText("AI Idea Validator — Business Plan Report", margin, PAGE_HEIGHT - 35f, footerPaint)
                c.drawLine(margin, PAGE_HEIGHT - 45f, PAGE_WIDTH - margin, PAGE_HEIGHT - 45f, linePaint)
            }
            pdfDocument.finishPage(it)
        }

        try {
            pdfDocument.writeTo(outputStream)
        } finally {
            pdfDocument.close()
        }
    }
}

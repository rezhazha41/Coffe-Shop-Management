package com.apps.coffeeshop.ui.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.apps.coffeeshop.data.Transaction
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import android.graphics.ImageDecoder
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RectF
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

object ReportUtils {

    fun generateReport(
        context: Context, 
        transactions: List<Transaction>, 
        totalIncome: Double, 
        totalExpense: Double,
        logoUri: String? = null
    ) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        
        val margin = 50f
        var yPos = 50f

        // 0. Logo (Black & White)
        val logoBitmap: Bitmap? = try {
            if (logoUri != null) {
                if (Build.VERSION.SDK_INT >= 28) {
                    val source = ImageDecoder.createSource(context.contentResolver, android.net.Uri.parse(logoUri))
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, android.net.Uri.parse(logoUri))
                }
            } else {
                BitmapFactory.decodeResource(context.resources, com.apps.coffeeshop.R.drawable.logo)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        if (logoBitmap != null) {
            val logoPaint = Paint()
            val colorMatrix = ColorMatrix()
            colorMatrix.setSaturation(0f) // Grayscale
            logoPaint.colorFilter = ColorMatrixColorFilter(colorMatrix)
            
            val targetSize = 60f
            val scale = targetSize / logoBitmap.width.toFloat()
            val destHeight = logoBitmap.height * scale
            
            // Center logo
            val left = (595f - targetSize) / 2
            val destRect = RectF(left, yPos, left + targetSize, yPos + destHeight)
            
            canvas.drawBitmap(logoBitmap, null, destRect, logoPaint)
            yPos += destHeight + 20
        }

        // --- Header Section ---
        paint.color = Color.DKGRAY
        paint.textSize = 28f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("LAPORAN KEUANGAN", 297.5f, yPos, paint)
        
        yPos += 30
        paint.textSize = 18f
        paint.color = Color.GRAY
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        canvas.drawText("APPS COFFEE SHOP", 297.5f, yPos, paint)
        
        yPos += 40
        paint.strokeWidth = 2f
        paint.color = Color.LTGRAY
        canvas.drawLine(margin, yPos, 595f - margin, yPos, paint) // Horizontal Line
        
        // --- Summary Box ---
        yPos += 30
        drawSummaryBox(canvas, totalIncome, totalExpense, margin, yPos)
        yPos += 120 // Height of summary box + padding

        // --- Transaction Table Header ---
        yPos += 20
        paint.color = Color.BLACK
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("DAFTAR TRANSAKSI:", margin, yPos, paint)
        
        yPos += 20
        
        // Draw Table Header Background
        val headerHeight = 30f
        val tableWidth = 595f - (2 * margin)
        paint.color = Color.rgb(224, 224, 224) // Light Grey
        paint.style = Paint.Style.FILL
        canvas.drawRect(margin, yPos, margin + tableWidth, yPos + headerHeight, paint)
        
        // Draw Table Header Text
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        paint.textSize = 12f
        val col1 = margin + 10
        val col2 = margin + 120
        val col3 = margin + 350
        
        val textY = yPos + 20
        canvas.drawText("WAKTU", col1, textY, paint)
        canvas.drawText("KETERANGAN", col2, textY, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("JUMLAH (RP)", margin + tableWidth - 10, textY, paint)
        
        yPos += headerHeight
        
        // --- Transaction Rows ---
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textAlign = Paint.Align.LEFT
        
        val recentTx = transactions.sortedByDescending { it.date }.take(30) // Limit items per page
        val timeFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
        
        var isOdd = false
        val rowHeight = 25f
        
        for (tx in recentTx) {
            // Alternating Row Color
            if (isOdd) {
                paint.color = Color.rgb(245, 245, 245)
                canvas.drawRect(margin, yPos, margin + tableWidth, yPos + rowHeight, paint)
            }
            isOdd = !isOdd
            
            val isIncome = tx.type == "IN" || tx.type == "CAPITAL"
            val time = timeFormat.format(Date(tx.date))
            val desc = if (tx.itemsJson.isNotEmpty()) tx.itemsJson.take(35) else tx.note.take(35)
            val amountStr = (if(isIncome) "+ " else "- ") + com.apps.coffeeshop.CurrencyUtils.toRupiah(tx.totalAmount)
            
            val rowTextY = yPos + 18
            
            paint.color = Color.DKGRAY
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(time, col1, rowTextY, paint)
            canvas.drawText(desc, col2, rowTextY, paint)
            
            paint.textAlign = Paint.Align.RIGHT
            paint.color = if(isIncome) Color.rgb(46, 125, 50) else Color.rgb(198, 40, 40)
            canvas.drawText(amountStr, margin + tableWidth - 10, rowTextY, paint)
            
            yPos += rowHeight
        }

        pdfDocument.finishPage(page)

        // Save File
        val fileName = "Laporan_Keuangan_Professional_${System.currentTimeMillis()}.pdf"
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, "Laporan disimpan di Downloads/$fileName", Toast.LENGTH_LONG).show()
            
            // Try to open/share immediately? For now just toast.
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal menyimpan laporan: ${e.message}", Toast.LENGTH_LONG).show()
        }

        pdfDocument.close()
    }

    private fun drawSummaryBox(canvas: Canvas, income: Double, expense: Double, x: Float, y: Float) {
        val paint = Paint()
        val width = 495f // 595 - 100
        val height = 90f
        val balance = income - expense
        
        // Box Border/Bg
        paint.color = Color.rgb(250, 250, 250)
        paint.style = Paint.Style.FILL
        canvas.drawRect(x, y, x + width, y + height, paint)
        
        paint.color = Color.LTGRAY
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(x, y, x + width, y + height, paint)
        
        // Content
        paint.style = Paint.Style.FILL
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        
        val colWidth = width / 3
        
        // Income
        paint.color = Color.rgb(46, 125, 50) // Green
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Total Pemasukan", x + (colWidth * 0.5f), y + 30, paint)
        canvas.drawText(com.apps.coffeeshop.CurrencyUtils.toRupiah(income), x + (colWidth * 0.5f), y + 60, paint)
        
        // Expense
        paint.color = Color.rgb(198, 40, 40) // Red
        canvas.drawText("Total Pengeluaran", x + (colWidth * 1.5f), y + 30, paint)
        canvas.drawText(com.apps.coffeeshop.CurrencyUtils.toRupiah(expense), x + (colWidth * 1.5f), y + 60, paint)

        // Balance
        paint.color = Color.rgb(21, 101, 192) // Blue
        canvas.drawText("Saldo Bersih", x + (colWidth * 2.5f), y + 30, paint)
        canvas.drawText(com.apps.coffeeshop.CurrencyUtils.toRupiah(balance), x + (colWidth * 2.5f), y + 60, paint)
    }
}

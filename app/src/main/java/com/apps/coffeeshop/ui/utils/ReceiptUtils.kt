package com.apps.coffeeshop.ui.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.provider.MediaStore
import android.widget.Toast
import com.apps.coffeeshop.viewmodel.CartItem
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.ImageDecoder
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RectF
import android.graphics.BitmapFactory
import android.net.Uri

object ReceiptUtils {

    // Configuration
    private const val PAGE_WIDTH = 300 // Small receipt width (approx 58mm thermal printer scale)
    private const val PADDING = 20f

    fun generateReceiptBitmap(
        context: Context,
        items: List<CartItem>, 
        total: Double,
        storeName: String = "Apps Coffee Shop",
        storeAddress: String = "Jl. Coffee Shop No. 1",
        logoUri: String? = null
    ): Bitmap {
        // Configuration
        val width = 400
        val padding = 24f
        
        // Calculate estimated height
        val headerHeight = 160
        val itemHeight = 60 // 2 lines per item
        val footerHeight = 150
        val contentHeight = headerHeight + (items.size * itemHeight) + footerHeight
        val sawtoothHeight = 20
        val totalHeight = contentHeight + sawtoothHeight
        
        val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Draw Background (White Paper)
        // Check if we want transparent background for the "cut" effect? 
        // Let's fill top part white, and draw teeth at bottom.
        val paint = Paint()
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, width.toFloat(), contentHeight.toFloat(), paint)
        
        // Draw Sawtooth Edge at the bottom
        val path = Path()
        path.moveTo(0f, contentHeight.toFloat())
        val toothWidth = 20f
        val teethCount = (width / toothWidth).toInt()
        
        for (i in 0 until teethCount) {
            val x = i * toothWidth
            path.lineTo(x + (toothWidth / 2), contentHeight.toFloat() + sawtoothHeight)
            path.lineTo(x + toothWidth, contentHeight.toFloat())
        }
        path.lineTo(width.toFloat(), 0f) // Close loop to top if needed, but we just want to fill the teeth
        path.lineTo(0f, 0f) // actually we just drew the bottom edge. 
        // Let's construct a full shape for the paper including teeth
        val paperPath = Path()
        paperPath.moveTo(0f, 0f)
        paperPath.lineTo(width.toFloat(), 0f)
        paperPath.lineTo(width.toFloat(), contentHeight.toFloat())
        
        // Teeth
        var currentX = width.toFloat()
        while (currentX > 0) {
            paperPath.lineTo(currentX - (toothWidth / 2), contentHeight.toFloat() + sawtoothHeight)
            paperPath.lineTo(currentX - toothWidth, contentHeight.toFloat())
            currentX -= toothWidth
        }
        
        paperPath.close()
        canvas.drawPath(paperPath, paint)

        drawReceiptContent(canvas, items, total, width.toFloat(), contentHeight.toFloat(), storeName, storeAddress, logoUri, context)
        
        return bitmap
    }
    
    // ... Print Function remains similar but calling drawReceiptContent ...
    // Note: Print usually doesn't want the jagged edge on paper (printer does the cutting).
    // So for print, we might want a straighter version.
    // But consistent content.
    
    fun printReceipt(
        context: Context, 
        items: List<CartItem>, 
        total: Double,
        storeName: String = "Apps Coffee Shop",
        storeAddress: String = "Jl. Coffee Shop No. 1",
        logoUri: String? = null
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "Struk_Apps_${System.currentTimeMillis()}"
        
        printManager.print(jobName, object : android.print.PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: android.os.CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: android.os.Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }
                val builder = android.print.PrintDocumentInfo.Builder(jobName)
                    .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()
                callback?.onLayoutFinished(builder, true)
            }

            override fun onWrite(
                pages: Array<out android.print.PageRange>?,
                destination: android.os.ParcelFileDescriptor?,
                cancellationSignal: android.os.CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                 if (cancellationSignal?.isCanceled == true) {
                    callback?.onWriteCancelled()
                    return
                }
                
                val pdfDocument = PdfDocument()
                val width = 400
                // Estimate height
                val height = 400 + (items.size * 60)
                val pageInfo = PdfDocument.PageInfo.Builder(width, height, 1).create()
                val page = pdfDocument.startPage(pageInfo)
                
                // Draw white bg
                page.canvas.drawColor(Color.WHITE)
                drawReceiptContent(page.canvas, items, total, width.toFloat(), height.toFloat(), storeName, storeAddress, logoUri, context)
                
                pdfDocument.finishPage(page)
                
                try {
                    pdfDocument.writeTo(FileOutputStream(destination?.fileDescriptor))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.toString())
                    return
                } finally {
                    pdfDocument.close()
                }
                callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
            }
        }, null)
    }

    // Save Image Function (Same as before)
    fun saveReceiptImage(
        context: Context, 
        items: List<CartItem>, 
        total: Double,
        storeName: String = "Apps Coffee Shop",
        storeAddress: String = "Jl. Coffee Shop No. 1",
        logoUri: String? = null
    ) {
         val bitmap = generateReceiptBitmap(context, items, total, storeName, storeAddress, logoUri)
        val filename = "RECEIPT_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CoffeeShop")
                }
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = resolver.openOutputStream(imageUri!!)
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val image = File(imagesDir, filename)
                fos = FileOutputStream(image)
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos!!)
            Toast.makeText(context, "Struk tersimpan di Galeri!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal menyimpan: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            fos?.close()
        }
    }

    private fun drawReceiptContent(
        canvas: Canvas, 
        items: List<CartItem>, 
        total: Double, 
        width: Float, 
        height: Float,
        storeName: String = "Apps Coffee Shop",
        storeAddress: String = "Jl. Coffee Shop No. 1",
        logoUri: String? = null,
        context: Context? = null
    ) {
        val paint = Paint()
        paint.color = Color.BLACK
        val padding = 24f
        
        // Fonts
        val fontHeader = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        val fontRegular = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        val fontMono = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        val fontBold = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        
        var y = 40f
        
        // 0. Logo (Black & White) - resize to max 100x100 approx
        val logoBitmap: Bitmap? = try {
            if (logoUri != null && context != null) {
                if (Build.VERSION.SDK_INT >= 28) {
                    val source = ImageDecoder.createSource(context.contentResolver, android.net.Uri.parse(logoUri))
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, android.net.Uri.parse(logoUri))
                }
            } else if (context != null) {
                BitmapFactory.decodeResource(context.resources, com.apps.coffeeshop.R.drawable.logo)
            } else {
                null
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
            
            val targetSize = 80f
            val scale = targetSize / logoBitmap.width.toFloat()
            val destHeight = logoBitmap.height * scale
            
            val left = (width - targetSize) / 2
            val destRect = RectF(left, y, left + targetSize, y + destHeight)
            
            canvas.drawBitmap(logoBitmap, null, destRect, logoPaint)
            y += destHeight + 10
        }
        
        // 1. Header: Store Name (Dynamic)
        paint.typeface = fontHeader
        paint.textSize = 28f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(storeName, width / 2, y, paint)
        y += 26
        
        // Subheader: Address (Dynamic)
        paint.typeface = fontRegular
        paint.textSize = 14f
        canvas.drawText(storeAddress, width / 2, y, paint)
        y += 20
        // removed hardcoded "Apps" / "Coffee Shop" lines to double check logic
        // The original had "Apps" then "Coffee Shop". Now we use storeName.
        // We might want to split or just show storeName.
        // Let's assume storeName covers the main title. 
        // If the user entered "Apps Coffee Shop", it shows that.

        
        // Dash Line
        drawDashLine(canvas, width, y)
        y += 25
        
        // 2. Date/Time Info
        paint.typeface = fontMono
        paint.textSize = 12f
        paint.textAlign = Paint.Align.LEFT
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val now = Date()
        
        canvas.drawText(dateFormat.format(now), padding, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(timeFormat.format(now), width - padding, y, paint)
        y += 16
        
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("No. Order: #${(100..999).random()}", padding, y, paint) // Random ID for demo
        y += 8
        
        // Dash Line
        y += 12
        drawDashLine(canvas, width, y)
        y += 25
        
        // 3. Items List
        // Style: 
        // Product Name (Bold)
        // 1 x 7.000 (Regular) ........ 7.000 (Right)
        
        items.forEach { item ->
            // Product Name
            paint.typeface = fontBold
            paint.textSize = 14f
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(item.product.name, padding, y, paint)
            y += 20
            
            // Qty & Price
            paint.typeface = fontMono
            paint.textSize = 12f
            
            val qtyPrice = "${item.quantity} x ${toCompactRupiah(item.product.price)}"
            canvas.drawText(qtyPrice, padding, y, paint)
            
            // Total Item Price
            val totalItem = item.quantity * item.product.price
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(com.apps.coffeeshop.CurrencyUtils.toRupiah(totalItem), width - padding, y, paint)
            
            y += 30 // Spacing between items
        }
        
        y -= 10
        drawDashLine(canvas, width, y)
        y += 25
        
        // 4. Totals
        paint.typeface = fontBold
        paint.textSize = 16f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Total", padding, y, paint)
        
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(com.apps.coffeeshop.CurrencyUtils.toRupiah(total), width - padding, y, paint)
        y += 25
        
        paint.typeface = fontRegular
        paint.textSize = 12f
        paint.textAlign = Paint.Align.LEFT // Reset alignment
        canvas.drawText("Bayar (Cash)", padding, y, paint) // Simulation text
        
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(com.apps.coffeeshop.CurrencyUtils.toRupiah(total), width - padding, y, paint) // Assume exact for now
        
        y += 40
        
        // 5. Footer
        paint.typeface = fontRegular
        paint.textSize = 12f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Terima Kasih!", width / 2, y, paint)
        y += 20
        paint.color = Color.GRAY
        paint.textSize = 10f
        canvas.drawText("Simpan dan bagikan struk ini", width / 2, y, paint)
    }

    private fun drawDashLine(canvas: Canvas, width: Float, y: Float) {
        val paint = Paint()
        paint.color = Color.LTGRAY
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f)
        
        val path = Path()
        path.moveTo(20f, y)
        path.lineTo(width - 20f, y)
        canvas.drawPath(path, paint)
    }

    
    private fun toCompactRupiah(value: Double): String {
        return com.apps.coffeeshop.CurrencyUtils.toRupiah(value)
            .replace("Rp", "")
            .replace(",00", "")
            .trim()
    }
}

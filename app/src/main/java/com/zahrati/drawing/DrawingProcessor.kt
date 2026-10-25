package com.zahrati.drawing

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object DrawingProcessor {

    fun makeLineArt(source: Bitmap, stage: Int): Bitmap {
        val maxSide = 1200
        val scale = min(
            1f,
            maxSide.toFloat() / max(source.width, source.height)
        )

        val bitmap = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                source,
                max(1, (source.width * scale).toInt()),
                max(1, (source.height * scale).toInt()),
                true
            )
        } else {
            source
        }

        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)

        bitmap.getPixels(
            pixels, 0, width, 0, 0, width, height
        )

        val gray = IntArray(pixels.size)

        for (i in pixels.indices) {
            val c = pixels[i]
            gray[i] = (
                Color.red(c) * 299 +
                Color.green(c) * 587 +
                Color.blue(c) * 114
            ) / 1000
        }

        val output = IntArray(pixels.size) { Color.WHITE }

        val threshold = when (stage.coerceIn(1, 4)) {
            1 -> 115
            2 -> 82
            3 -> 54
            else -> 32
        }

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val i = y * width + x

                val gx =
                    -gray[i - width - 1] +
                    gray[i - width + 1] -
                    2 * gray[i - 1] +
                    2 * gray[i + 1] -
                    gray[i + width - 1] +
                    gray[i + width + 1]

                val gy =
                    -gray[i - width - 1] -
                    2 * gray[i - width] -
                    gray[i - width + 1] +
                    gray[i + width - 1] +
                    2 * gray[i + width] +
                    gray[i + width + 1]

                val strength = (abs(gx) + abs(gy)) / 4

                if (strength >= threshold) {
                    output[i] = Color.BLACK
                }
            }
        }

        return Bitmap.createBitmap(
            output,
            width,
            height,
            Bitmap.Config.ARGB_8888
        )
    }
}

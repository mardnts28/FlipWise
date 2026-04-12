package com.flipwise.app.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

object TotpManager {

    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    fun generateSecretKey(): String {
        val random = SecureRandom()
        val bytes = ByteArray(20)
        random.nextBytes(bytes)
        return bytes.toBase32()
    }

    fun getQrCodeBitmap(uri: String, size: Int = 512): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(uri, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    fun generateTotpUri(email: String, secret: String): String {
        return "otpauth://totp/FlipWise:$email?secret=$secret&issuer=FlipWise"
    }

    fun verifyOtp(secret: String, code: String): Boolean {
        if (code.length != 6) return false
        val secretBytes = secret.fromBase32()
        val currentInterval = System.currentTimeMillis() / 1000 / 30
        
        // Check current, previous, and next interval for clock skew
        for (i in -1..1) {
            if (generateCode(secretBytes, currentInterval + i) == code) return true
        }
        return false
    }

    private fun generateCode(secret: ByteArray, interval: Long): String {
        val data = ByteBuffer.allocate(8).putLong(interval).array()
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(secret, "HmacSHA1"))
        val hash = mac.doFinal(data)
        val offset = hash[hash.size - 1].toInt() and 0xf
        val truncatedHash = (hash[offset].toInt() and 0x7f shl 24) or
                           (hash[offset + 1].toInt() and 0xff shl 16) or
                           (hash[offset + 2].toInt() and 0xff shl 8) or
                           (hash[offset + 3].toInt() and 0xff)
        val otp = truncatedHash % 10.0.pow(6).toInt()
        return otp.toString().padStart(6, '0')
    }

    private fun ByteArray.toBase32(): String {
        var i = 0
        var index = 0
        var digit = 0
        var currByte: Int
        var nextByte: Int
        val base32 = StringBuilder((this.size + 7) * 8 / 5)

        while (i < this.size) {
            currByte = if (this[i] >= 0) this[i].toInt() else this[i] + 256
            if (index > 3) {
                if (i + 1 < this.size) {
                    nextByte = if (this[i + 1] >= 0) this[i + 1].toInt() else this[i + 1] + 256
                } else {
                    nextByte = 0
                }
                digit = currByte and (0xFF shr index)
                index = (index + 5) % 8
                digit = digit shl index
                digit = digit or (nextByte shr (8 - index))
                i++
            } else {
                digit = (currByte shr (8 - (index + 5))) and 0x1F
                index = (index + 5) % 8
                if (index == 0) i++
            }
            base32.append(ALPHABET[digit])
        }
        return base32.toString()
    }

    private fun String.fromBase32(): ByteArray {
        val bytes = ByteArray(this.length * 5 / 8)
        var i = 0
        var index = 0
        var digit: Int
        var currByte = 0
        for (char in this.uppercase()) {
            digit = ALPHABET.indexOf(char)
            if (digit == -1) continue
            if (index <= 3) {
                index = (index + 5) % 8
                if (index == 0) {
                    currByte = currByte or digit
                    bytes[i++] = currByte.toByte()
                    currByte = 0
                } else {
                    currByte = currByte or (digit shl (8 - index))
                }
            } else {
                index = (index + 5) % 8
                currByte = currByte or (digit shr index)
                bytes[i++] = currByte.toByte()
                currByte = (digit shl (8 - index)) and 0xFF
            }
        }
        return bytes
    }
}

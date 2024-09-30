import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor
import java.util.Random

object hellEncryptor {
    var key: ByteArray = byteArrayOf(
            121.toByte(), 111.toByte(), 107.toByte(), 105.toByte(),
            79.toByte(), 102.toByte(), 90.toByte(), 101.toByte(),
            122.toByte(), 101.toByte()
    )

    var seed: Long = 245

    val xorEnc: (String) -> ByteArray = {inputStr ->
        val cypherBytes = inputStr.encodeToByteArray()
        var j = 0
        for (i in cypherBytes.indices) {
            cypherBytes[i] = cypherBytes[i] xor key[j]

            j = (j + 1) % key.size
        }
        cypherBytes
    }

    val mathEnc: (String) -> String = { inputStr ->
        var result = ""

        inputStr.toCharArray().forEach { char ->
            val intValue = char.code
            val plus = (intValue + 10) - (30 % 26);
            val newChar = plus.toChar()
            result += newChar
        }

        result
    }

    val randomEnc: (ByteArray) -> ByteArray = {inputStr ->
        val random = Random(seed)
        ByteArray(inputStr.size) { i ->
            (inputStr[i] xor (random.nextInt(255) % (i + 10)).toByte()).toByte()
        }
    }

    val encrypt: (String) -> ByteArray  = {plainText ->
        val cypherText = Base64.getEncoder().encode(randomEnc(xorEnc(mathEnc(plainText))))
        cypherText
    }
}
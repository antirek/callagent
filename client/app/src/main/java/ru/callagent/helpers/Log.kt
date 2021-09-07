package ru.callagent.helpers

import android.os.Environment
import timber.log.Timber
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*

object Log {

    @JvmStatic
    fun i(tag: String, message: String?) {
        Timber.i("""$tag ${message ?: "log message is null"}""")
        this.file(tag, message.toString())
    }

    @JvmStatic
    fun i(message: String?) {
        Timber.i(message ?: "log message is null")
        this.file(message.toString())
    }

    @JvmStatic
    fun e(throwable: Throwable, message: String = "") {
        Timber.e(throwable)
        this.file("E:", throwable.message.toString(), message)
    }

    @JvmStatic
    fun file(tag: String, message: String = "", message2: String = "") {
        val date = Date()
        this.file("$date [$tag] $message $message2")
    }

    @JvmStatic
    fun file(message: String) {
        val fileName = "log.txt"
        val path = Environment.getExternalStoragePublicDirectory("CallAgent")
        val file = File(path, fileName)
        var startLogInFile = true
        if (!file.exists()) {
            try {
                if (!file.parentFile.exists()) {
                    file.parentFile.mkdirs()
                }
                file.createNewFile()
            } catch (err: IOException) {
                startLogInFile = false
                Timber.e(err)
            }
        }
        try {
            if (startLogInFile) {
                val buf = BufferedWriter(FileWriter(file, true))
                buf.append(message)
                buf.newLine()
                buf.close()
            }
        } catch (err: IOException) {
            Timber.e(err)
        }
    }
} 
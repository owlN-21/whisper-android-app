package com.whispercppdemo.logger

import android.app.Application
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class FileLogger(
    application: Application,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val logsDir = File(application.filesDir, "logs").apply{ mkdirs() }

    private val logFile: File = run{
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        File(logsDir, "whisper_log_$ts.txt")
    }

    suspend fun log(line: String) = withContext(ioDispatcher) {
        val msg = if (line.endsWith("\n")) line else "$line\n"
        logFile.appendText(msg)
    }

    fun getLogFile(): File = logFile
}
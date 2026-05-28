package com.js.nowakelock.shizuku

import android.content.Context
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object ShizukuManager {

    private const val REQUEST_CODE_SHIZUKU = 1001

    /**
     * Checks if the Shizuku service is running.
     */
    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if we have permission to use Shizuku.
     */
    fun hasPermission(): Boolean {
        if (!isShizukuAvailable()) return false
        return try {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Requests permission to use Shizuku.
     */
    fun requestPermission() {
        if (isShizukuAvailable() && !hasPermission()) {
            Shizuku.requestPermission(REQUEST_CODE_SHIZUKU)
        }
    }

    /**
     * Executes a shell command using Shizuku.
     * If [lineProcessor] is provided, it will process the output line-by-line to prevent OutOfMemoryError.
     * Otherwise, it returns the full output as a String.
     */
    fun executeCommand(command: String, lineProcessor: ((String) -> Unit)? = null): String {
        if (!hasPermission()) return "Shizuku permission not granted."

        val builder = StringBuilder()
        try {
            // Shizuku.newProcess might be private or restricted in this API version
            // Use reflection to call it
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true

            // Execute via shell to support pipes and avoid manual split issues
            val cmdArray = arrayOf("sh", "-c", command)

            val process = method.invoke(
                null,
                cmdArray,
                null,
                null
            ) as Process

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (lineProcessor != null) {
                    lineProcessor(line!!)
                } else {
                    builder.append(line).append("\n")
                }
            }
            process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
            if (lineProcessor == null) {
                builder.append("Error executing command: ").append(e.message)
            }
        }
        return if (lineProcessor != null) "" else builder.toString()
    }
}

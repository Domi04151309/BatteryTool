package io.github.domi04151309.batterytool.helpers

import android.util.Log
import java.io.DataOutputStream
import java.io.IOException
import java.util.Scanner
import kotlin.collections.HashSet
import kotlin.text.StringBuilder

internal object Root {
    fun request(): Boolean =
        try {
            val process = Runtime.getRuntime().exec("su")
            val output = DataOutputStream(process.outputStream)
            output.writeBytes("echo access granted\n")
            output.writeBytes("exit\n")
            output.flush()
            true
        } catch (exception: IOException) {
            Log.w(this::class.simpleName, exception)
            false
        }

    fun shell(command: String) {
        try {
            Runtime.getRuntime().exec(arrayOf("su", "-c", command)).waitFor()
        } catch (exception: IOException) {
            Log.w(this::class.simpleName, exception)
        }
    }

    fun shell(commands: Array<String>) {
        try {
            val process = Runtime.getRuntime().exec("su")
            val output = DataOutputStream(process.outputStream)
            for (command in commands) output.writeBytes("$command\n")
            output.writeBytes("exit\n")
            output.flush()
        } catch (exception: IOException) {
            Log.e(this::class.simpleName, exception.toString())
        }
    }

    fun getFocusedApps(): HashSet<String> {
        val services = StringBuilder()
        try {
            val inputStream =
                Runtime.getRuntime().exec(
                    arrayOf(
                        "su",
                        "-c",
                        "dumpsys activity activities | " +
                            "grep -E 'CurrentFocus|ResumedActivity|FocusedApp' |  " +
                            "cut -d '{' -f2 | " +
                            "cut -d ' ' -f3 | " +
                            "cut -d '/' -f1",
                    ),
                ).inputStream
            Scanner(inputStream).useDelimiter("\\A").use { scanner ->
                services.append(if (scanner.hasNext()) scanner.next() else "")
            }
        } catch (exception: IOException) {
            Log.e(this::class.simpleName, exception.toString())
        }
        return HashSet(services.lines())
    }

    fun getServices(): HashSet<String> {
        val services = StringBuilder()
        try {
            val inputStream =
                Runtime.getRuntime().exec(
                    arrayOf(
                        "su",
                        "-c",
                        "dumpsys activity services",
                    ),
                ).inputStream
            Scanner(inputStream).useDelimiter("\\A").use { scanner ->
                services.append(if (scanner.hasNext()) scanner.next() else "")
            }
        } catch (exception: IOException) {
            Log.e(this::class.simpleName, exception.toString())
        }
        return parseServices(services.toString())
    }

    private fun parseServices(services: String): HashSet<String> {
        val set = HashSet<String>()
        var temp: String
        for (line in services.lines()) {
            if (line.contains("* ServiceRecord")) {
                temp = line.substring(line.indexOf('{') + 1, line.indexOf('/'))
                repeat(2) {
                    temp = temp.substring(temp.indexOf(' ') + 1)
                }
                set.add(temp)
            } else if (line.contains('#') && line.contains(':')) {
                set.add(line.substring(line.indexOf(": ") + 2))
            }
        }
        return set
    }
}

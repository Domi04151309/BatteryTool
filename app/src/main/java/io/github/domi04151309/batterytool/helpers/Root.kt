package io.github.domi04151309.batterytool.helpers

import android.util.Log
import java.io.DataOutputStream
import java.util.*
import kotlin.collections.HashSet

internal object Root {

    fun request(): Boolean {
        val p: Process
        return try {
            p = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(p.outputStream)
            os.writeBytes("echo access granted\n")
            os.writeBytes("exit\n")
            os.flush()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun shell(command: String) {
        try {
            val p = Runtime.getRuntime()
                .exec(arrayOf("su", "-c", command))
            p.waitFor()
        } catch (e: Exception) {
            Log.e("Superuser", e.toString())
        }
    }

    fun shell(commands: Array<String>) {
        val p: Process
        try {
            p = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(p.outputStream)
            for (command in commands) os.writeBytes("$command\n")
            os.writeBytes("exit\n")
            os.flush()
        } catch (e: Exception) {
            Log.e("Superuser", e.toString())
        }
    }

    fun getFocusedApps(): HashSet<String> {
        var result = ""
        try {
            Runtime.getRuntime().exec(
                arrayOf(
                    "su",
                    "-c",
                    "dumpsys activity activities | grep -E 'CurrentFocus|ResumedActivity|FocusedApp' |  cut -d '{' -f2 | cut -d ' ' -f3 | cut -d '/' -f1"
                )
            ).inputStream.use { inputStream ->
                Scanner(inputStream).useDelimiter("\\A").use { s ->
                    result = if (s.hasNext()) s.next() else ""
                }
            }
        } catch (e: Exception) {
            Log.e("Superuser", e.toString())
        }
        return parseFocusedApps(result)
    }

    private fun parseFocusedApps(services: String): HashSet<String> {
        val set = HashSet<String>()
        for (line in services.lines()) set.add(line)
        return set
    }

    fun getServices(): HashSet<String> {
        var result = ""
        try {
            Runtime.getRuntime().exec(
                arrayOf(
                    "su",
                    "-c",
                    "dumpsys activity services"
                )
            ).inputStream.use { inputStream ->
                Scanner(inputStream).useDelimiter("\\A").use { s ->
                    result = if (s.hasNext()) s.next() else ""
                }
            }
        } catch (e: Exception) {
            Log.e("Superuser", e.toString())
        }
        return parseServices(result)
    }

    private fun parseServices(services: String): HashSet<String> {
        val set = HashSet<String>()
        var temp: String
        for (line in services.lines()) {
            if (line.contains("* ServiceRecord")) {
                temp = line.substring(line.indexOf('{') + 1, line.indexOf('/'))
                for (i in 0 until 2) temp = temp.substring(temp.indexOf(' ') + 1)
                set.add(temp)
            } else if (line.contains('#') && line.contains(':')) {
                set.add(line.substring(line.indexOf(": ") + 2))
            }
        }
        return set
    }
}

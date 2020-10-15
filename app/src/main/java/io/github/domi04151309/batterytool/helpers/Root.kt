package io.github.domi04151309.batterytool.helpers

import android.util.Log
import java.io.DataOutputStream
import java.util.*

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

    fun getServices(): String {
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
        return result
    }
}

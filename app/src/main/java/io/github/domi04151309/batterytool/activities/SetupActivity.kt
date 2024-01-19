package io.github.domi04151309.batterytool.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import io.github.domi04151309.batterytool.R
import io.github.domi04151309.batterytool.helpers.Root

class SetupActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        window.statusBarColor = SurfaceColors.SURFACE_0.getColor(this)

        findViewById<Button>(R.id.button).setOnClickListener {
            if (Root.request()) {
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putBoolean("setup_complete", true)
                    .apply()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.action_failed)
                    .setMessage(R.string.action_failed_summary)
                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                    .show()
            }
        }

        findViewById<Button>(R.id.demo).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.setup_demo)
                .setMessage(R.string.setup_demo_summary)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    demoMode = true
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .show()
        }
    }

    companion object {
        var demoMode: Boolean = false
            private set
    }
}

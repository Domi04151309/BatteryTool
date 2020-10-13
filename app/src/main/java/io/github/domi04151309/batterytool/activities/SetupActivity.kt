package io.github.domi04151309.batterytool.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.preference.PreferenceManager
import io.github.domi04151309.batterytool.R
import io.github.domi04151309.batterytool.helpers.Root
import io.github.domi04151309.batterytool.helpers.Theme

class SetupActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        findViewById<Button>(R.id.button).setOnClickListener {
            if (Root.request()) {
                PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("setup_complete", true).apply()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                AlertDialog.Builder(this, R.style.DialogThemeLight)
                    .setTitle(R.string.action_failed)
                    .setMessage(R.string.action_failed_summary)
                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                    .show()
            }
        }
    }
}

package io.github.domi04151309.batterytool.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import io.github.domi04151309.batterytool.R
import io.github.domi04151309.batterytool.helpers.AppHelper

class StopActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.action == Intent.ACTION_CREATE_SHORTCUT) {
            setupShortcut()
        } else {
            AppHelper.hibernate(this)
            Toast.makeText(this, R.string.toast_stopped_all, Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    private fun setupShortcut() {
        setResult(
            RESULT_OK,
            Intent().putExtra(Intent.EXTRA_SHORTCUT_INTENT, Intent(this, this::class.java))
                .putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.shortcut))
                .putExtra(
                    Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher),
                ),
        )
    }
}

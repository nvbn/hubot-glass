package com.nvbn.hubotforglass

import android.app.Activity
import android.view.Menu
import org.jetbrains.anko.intentFor

class MenuActivity: Activity() {
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        openOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add("Dismiss").setOnMenuItemClickListener {
            stopService(intentFor<RecognizerService>())
        }
        return true
    }

    override fun onOptionsMenuClosed(menu: Menu) {
        finish()
    }
}

package com.example.runninglog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log.d
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.settings_screen.backButton

class StravaScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Set the color theme
        when (SettingsObject.color) {
            "1" -> setTheme(R.style.blueTheme)
            "2" -> setTheme(R.style.darkTheme)
            "3" -> setTheme(R.style.goldTheme)
            "4" -> setTheme(R.style.darkGoldTheme)
            "5" -> setTheme(R.style.redTheme)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.strava_screen)

        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        d("filip","before creating alert")
        val alert = AlertDialog.Builder(this)
        alert.setMessage("Click continue to sync running log with Strava")
        alert.setCancelable(false)
        alert.setPositiveButton("Proceed") { dialog, id ->
//            val internalStorage : File = getFilesDir()
//            val csv = File(internalStorage, "runs.csv")
//            val title = File(internalStorage, "titles.txt")
//            val desc = File(internalStorage, "descriptions.txt")
            val client_id = "42279"
            val intentUri = Uri.parse("https://www.strava.com/oauth/mobile/authorize")
                .buildUpon()
                .appendQueryParameter("client_id", client_id)
                .appendQueryParameter("redirect_uri", "http://running_log_app.com/")
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("approval_prompt", "auto")
                .appendQueryParameter("scope", "read,activity:read_all")
                .build()

            val intent = Intent(Intent.ACTION_VIEW, intentUri)
            startActivity(intent)
        }
        alert.setNegativeButton("Cancel") { dialog, id ->
            startActivity(Intent(this, MainActivity::class.java))
        }
        d("filip","showing the alert now")
        alert.show()
    }
}
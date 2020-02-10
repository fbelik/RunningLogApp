package com.example.runninglog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.settings_screen.*
import java.io.File
import java.lang.StringBuilder

class SettingsScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        when (SettingsObject.color) {
            "1" -> setTheme(R.style.blueTheme)
            "2" -> setTheme(R.style.darkTheme)
            "3" -> setTheme(R.style.goldTheme)
            "4" -> setTheme(R.style.darkGoldTheme)
            "5" -> setTheme(R.style.redTheme)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_screen)

        val internalStorage : File = filesDir
        val settingsCsv = File(internalStorage, "settings.csv")

        if (SettingsObject.unitsSetting == "km") {
            kmRatio.isChecked = true
        }

        if (SettingsObject.firstDay == "sun") {
            sundayRadio.isChecked = true
        }

        when (SettingsObject.color) {
            "3" -> color3Radio.isChecked = true
            "4" -> color4Radio.isChecked = true
            "5" -> color5Radio.isChecked = true
        }

        clearButton.setOnClickListener {
            val alert = AlertDialog.Builder(this)
            alert.setTitle("Check")
            alert.setMessage("Are you sure you want to delete all runs?")
            alert.setCancelable(false)
            alert.setPositiveButton("Proceed") { dialog, id ->
                val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
                val gson = Gson()
                val allRuns = mutableListOf<Run>()
                val json = gson.toJson(allRuns)
                val editor = sharedPreferences.edit()
                editor.putString("run_data", json)
                editor.apply()
            }
            alert.setNegativeButton("Cancel") { dialog, id ->
                Log.d("filipbelik", "canceled deletion")
            }
            alert.show()
        }

        backButton.setOnClickListener {
            settingsCsv.writeText("${SettingsObject.unitsSetting},${SettingsObject.firstDay},${SettingsObject.color}")
            startActivity(Intent(this, MainActivity::class.java))
        }

        milesRadio.setOnClickListener {
            SettingsObject.unitsSetting = "miles"
        }

        kmRatio.setOnClickListener {
            SettingsObject.unitsSetting = "km"
        }

        sundayRadio.setOnClickListener {
            SettingsObject.firstDay = "sun"
        }

        mondayRadio.setOnClickListener {
            SettingsObject.firstDay = "mon"
        }
        color1Radio.setOnClickListener {
            SettingsObject.color = "1"
            settingsCsv.writeText("${SettingsObject.unitsSetting},${SettingsObject.firstDay},${SettingsObject.color}")
            startActivity(Intent(this, SettingsScreen::class.java))
        }
//        color2Radio.setOnClickListener {
//            SettingsObject.color = "2"
//            settingsCsv.writeText("${SettingsObject.unitsSetting},${SettingsObject.firstDay},${SettingsObject.color}")
//            startActivity(Intent(this, SettingsScreen::class.java))
//        }
        color3Radio.setOnClickListener {
            SettingsObject.color = "3"
            settingsCsv.writeText("${SettingsObject.unitsSetting},${SettingsObject.firstDay},${SettingsObject.color}")
            startActivity(Intent(this, SettingsScreen::class.java))
        }
        color4Radio.setOnClickListener {
            SettingsObject.color = "4"
            settingsCsv.writeText("${SettingsObject.unitsSetting},${SettingsObject.firstDay},${SettingsObject.color}")
            startActivity(Intent(this, SettingsScreen::class.java))
        }
        color5Radio.setOnClickListener {
            SettingsObject.color = "5"
            settingsCsv.writeText("${SettingsObject.unitsSetting},${SettingsObject.firstDay},${SettingsObject.color}")
            startActivity(Intent(this, SettingsScreen::class.java))
        }
    }
}
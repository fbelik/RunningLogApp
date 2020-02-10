package com.example.runninglog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log.d
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*

object SettingsObject {
    var unitsSetting = "miles"
    var firstDay = "mon"
    var color = "1"
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Read in settings CSV
        val internalStorage : File = filesDir
        val settingsCsv = File(internalStorage, "settings.csv")
        settingsCsv.createNewFile()
        val settingsScanner = Scanner(settingsCsv)
        settingsScanner.useDelimiter(",")
        if (settingsScanner.hasNext()) {
            SettingsObject.unitsSetting = settingsScanner.next()
            d("settings = ", SettingsObject.unitsSetting)
            if (settingsScanner.hasNext()) {
                SettingsObject.firstDay = settingsScanner.next()
                if (settingsScanner.hasNext()) {
                    SettingsObject.color = settingsScanner.next()
                }
                else {
                    settingsCsv.writeText("${SettingsObject.unitsSetting},${SettingsObject.firstDay},${SettingsObject.color}")
                }
            }
            else {
                settingsCsv.writeText("${SettingsObject.unitsSetting},${SettingsObject.firstDay},${SettingsObject.color}")
            }
        }
        else {
            settingsCsv.writeText("${SettingsObject.unitsSetting},${SettingsObject.firstDay},${SettingsObject.color}")
        }
        settingsScanner.close()

        // Set the color theme
        when (SettingsObject.color) {
            "1" -> setTheme(R.style.blueTheme)
            "2" -> setTheme(R.style.darkTheme)
            "3" -> setTheme(R.style.goldTheme)
            "4" -> setTheme(R.style.darkGoldTheme)
            "5" -> setTheme(R.style.redTheme)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get run data
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val gson = Gson()

        val orig = sharedPreferences.getString("run_data", null)
        val type = object : TypeToken<MutableList<Run>>() {}.type
        val theRuns : MutableList<Run>? = gson.fromJson(orig, type)
        var runs = mutableListOf<Run>()
        if (theRuns != null) {
            runs = theRuns
        }

//        val spinnerList = arrayOf<String>("Last Run","Last Week","Last 2 Weeks","Last Month","Last 3 Months","Last Year","All Time")
//        val spinnerAdapter = ArrayAdapter<String>(this, R.layout.spinner_layout, spinnerList)
//        timeSpinner.adapter = spinnerAdapter

        timeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val weekMillis = 604800000L
                val week2Millis = 1209600000L
                val monthMillis = 2419200000L
                val month3Millis = 7257600000L
                val yearMillis = 31449600000L
                when (position) {
                    // Key for contents:
                    // dist, total_time_mins, month, day, year, pace_mins, pace_secs, speed
                    0 -> {// Last Run
                        if (runs.size == 0) {
                            statsText.text = "No runs yet to display"
                        }
                        else if (SettingsObject.unitsSetting == "miles"){
                            val dist = runs[0].distance
                            val timeH = "%.2f".format(runs[0].minutes / 60f)
                            statsText.text = "Total distance = %.2f miles\nTotal time = %.2f hours".format(runs[0].distance, (runs[0].minutes / 60f))
                        }
                        else {
                            val dist = "%.2f".format(runs[0].distance * 1.609f)
                            val timeH = "%.2f".format(runs[0].minutes / 60f)
                            statsText.text = "Total distance = $dist km\nTotal time = $timeH hours"
                        }
                    }
                    1 -> { // Last week
                        var dist = 0f
                        var timeM = 0f
                        val date = Date()
                        for (run in runs) {
                            val difference = date.time - Date(run.year-1900, run.month-1, run.day).time
                            d("filip", "looking at a run with a difference of $difference")
                            if (difference in 0..weekMillis) {
                                dist += run.distance
                                timeM += run.minutes
                            }
                            else if (difference > 0) {
                                break
                            }
                        }
                        val timeH = timeM / 60f
                        val timeHStr = "%.2f".format(timeH)
                        if (dist == 0f) {
                            statsText.text = "No runs here to display"
                        }
                        else if (SettingsObject.unitsSetting == "miles") {
                            val distStr = "%.2f".format(dist)
                            statsText.text = "Total distance = $distStr miles\nTotal time = $timeHStr hours"
                        }
                        else {
                            val distStr = "%.2f".format(dist / 1.609f)
                            statsText.text = "Total distance = $distStr km\nTotal time = $timeHStr hours"
                        }
                    }
                    2 -> { // Last 2 weeks
                        var dist = 0f
                        var timeM = 0f
                        val date = Date()
                        for (run in runs) {
                            val difference = date.time - Date(run.year-1900, run.month-1, run.day).time
                            d("filip", "looking at a run with a difference of $difference")
                            if (difference in 0..week2Millis) {
                                dist += run.distance
                                timeM += run.minutes
                            }
                            else if (difference > 0) {
                                break
                            }
                        }
                        val timeH = timeM / 60f
                        val timeHStr = "%.2f".format(timeH)
                        if (dist == 0f) {
                            statsText.text = "No runs here to display"
                        }
                        else if (SettingsObject.unitsSetting == "miles") {
                            val distStr = "%.2f".format(dist)
                            statsText.text = "Total distance = $distStr miles\nTotal time = $timeHStr hours"
                        }
                        else {
                            val distStr = "%.2f".format(dist * 1.609f)
                            statsText.text = "Total distance = $distStr km\nTotal time = $timeHStr hours"
                        }
                    }
                    3 -> { // Last month
                        var dist = 0f
                        var timeM = 0f
                        val date = Date()
                        for (run in runs) {
                            val difference = date.time - Date(run.year-1900, run.month-1, run.day).time
                            d("filip", "looking at a run with a difference of $difference")
                            if (difference in 0..monthMillis) {
                                dist += run.distance
                                timeM += run.minutes
                            }
                            else if (difference > 0) {
                                break
                            }
                        }
                        val timeH = timeM / 60f
                        val timeHStr = "%.2f".format(timeH)
                        if (dist == 0f) {
                            statsText.text = "No runs here to display"
                        }
                        else if (SettingsObject.unitsSetting == "miles") {
                            val distStr = "%.2f".format(dist)
                            statsText.text = "Total distance = $distStr miles\nTotal time = $timeHStr hours"
                        }
                        else {
                            val distStr = "%.2f".format(dist * 1.609f)
                            statsText.text = "Total distance = $distStr km\nTotal time = $timeHStr hours"
                        }
                    }
                    4 -> { // Last 3 months
                        var dist = 0f
                        var timeM = 0f
                        val date = Date()
                        for (run in runs) {
                            val difference = date.time - Date(run.year-1900, run.month-1, run.day).time
                            d("filip", "looking at a run with a difference of $difference")
                            if (difference in 0..month3Millis) {
                                dist += run.distance
                                timeM += run.minutes
                            }
                            else if (difference > 0) {
                                break
                            }
                        }
                        val timeH = timeM / 60f
                        val timeHStr = "%.2f".format(timeH)
                        if (dist == 0f) {
                            statsText.text = "No runs here to display"
                        }
                        else if (SettingsObject.unitsSetting == "miles") {
                            val distStr = "%.2f".format(dist)
                            statsText.text = "Total distance = $distStr miles\nTotal time = $timeHStr hours"
                        }
                        else {
                            val distStr = "%.2f".format(dist * 1.609f)
                            statsText.text = "Total distance = $distStr km\nTotal time = $timeHStr hours"
                        }
                    }
                    5 -> { // Last year
                        var dist = 0f
                        var timeM = 0f
                        val date = Date()
                        for (run in runs) {
                            val difference = date.time - Date(run.year-1900, run.month-1, run.day).time
                            d("filip", "looking at a run with a difference of $difference")
                            if (difference in 0..yearMillis) {
                                dist += run.distance
                                timeM += run.minutes
                            }
                            else if (difference > 0) {
                                break
                            }
                        }
                        val timeH = timeM / 60f
                        val timeHStr = "%.2f".format(timeH)
                        if (dist == 0f) {
                            statsText.text = "No runs here to display"
                        }
                        else if (SettingsObject.unitsSetting == "miles") {
                            val distStr = "%.2f".format(dist)
                            statsText.text = "Total distance = $distStr miles\nTotal time = $timeHStr hours"
                        }
                        else {
                            val distStr = "%.2f".format(dist * 1.609f)
                            statsText.text = "Total distance = $distStr km\nTotal time = $timeHStr hours"
                        }
                    }
                    6 -> { // All time
                        var dist = 0f
                        var timeM = 0f
                        val date = Date()
                        for (run in runs) {
                            dist += run.distance
                            timeM += run.minutes
                        }
                        val timeH = timeM / 60f
                        val timeHStr = "%.2f".format(timeH)
                        if (dist == 0f) {
                            statsText.text = "No runs here to display"
                        }
                        else if (SettingsObject.unitsSetting == "miles") {
                            val distStr = "%.2f".format(dist)
                            statsText.text = "Total distance = $distStr miles\nTotal time = $timeHStr hours"
                        }
                        else {
                            val distStr = "%.2f".format(dist * 1.609f)
                            statsText.text = "Total distance = $distStr km\nTotal time = $timeHStr hours"
                        }
                    }
                }
            }
        }

        runButton.setOnClickListener {
            startActivity(Intent(this, EnterRun::class.java))
        }

        viewButton.setOnClickListener {
            startActivity(Intent(this, ViewRuns::class.java))
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsScreen::class.java))
        }

        stravaButton.setOnClickListener {
            d("flilip","switching to stravascreen")
            startActivity(Intent(this, StravaScreen::class.java))
        }

        weeklyButton.setOnClickListener {
            startActivity(Intent(this, WeeklyOverview::class.java))
        }

        monthlyOverviewButton.setOnClickListener {
            startActivity(Intent(this, MonthlyOverview::class.java))
        }
    }
}

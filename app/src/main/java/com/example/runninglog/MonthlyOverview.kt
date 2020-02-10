package com.example.runninglog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.view_runs.*
import kotlinx.android.synthetic.main.weekly_overview.*
import kotlinx.android.synthetic.main.weekly_overview.backButton
import java.text.SimpleDateFormat
import java.util.*

class MonthlyOverview : AppCompatActivity() {
    fun monthStr(a: Int) : String {
        when (a) {
            0 -> return "January"
            1 -> return "February"
            2 -> return "March"
            3 -> return "April"
            4 -> return "May"
            5 -> return "June"
            6 -> return "July"
            7 -> return "August"
            8 -> return "September"
            9 -> return "October"
            10 -> return "November"
            11 -> return "December"
        }
        return "error"
    }

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
        setContentView(R.layout.monthly_overview)

        // Get run data
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val gson = Gson()

        val orig = sharedPreferences.getString("run_data", null)
        val type = object : TypeToken<MutableList<Run>>() {}.type
        val theRuns : MutableList<Run>? = gson.fromJson(orig, type)
        var allRuns = mutableListOf<Run>()
        if (theRuns != null) {
            allRuns = theRuns
        }

        val layout = linearInScroll

        var month = 1
        var ctr = 0

        var currentDay = Date()
        currentDay = Date(currentDay.year, currentDay.month, 1)

        var dist = 0f
        var time = 0f
        var runs = 0

        while (month < 50 && ctr < allRuns.size) {
            val run = allRuns[ctr]
            if (run.year > currentDay.year+1900 || run.year+1900 == currentDay.year && run.month > currentDay.month+1) {
                ctr++
            }
            else if (run.year == currentDay.year+1900 && run.month == currentDay.month+1) {
                dist += allRuns[ctr].distance
                time += allRuns[ctr].minutes
                runs++
                ctr++
            }
            else {
                val txt = TextView(this)
                when (SettingsObject.color) {
                    "1" -> txt.setTextColor(resources.getColor(R.color.textColor1))
                    "2" -> txt.setTextColor(resources.getColor(R.color.textColor2))
                    "3" -> txt.setTextColor(resources.getColor(R.color.textColor3))
                    "4" -> txt.setTextColor(resources.getColor(R.color.textColor4))
                    "5" -> txt.setTextColor(resources.getColor(R.color.textColor5))
                }
                txt.textSize = 24f
                time /= 60
                if (SettingsObject.unitsSetting == "miles") {
                    txt.text = "Month of ${monthStr(currentDay.month)}, ${currentDay.year+1900}\nTotal Distance of %.2f miles, time of %.2f hours, $runs runs\n-------------------".format(dist, time)
                }
                else {
                    txt.text = "Week of ${monthStr(currentDay.month)}, ${currentDay.year+1900}\nTotal Distance of %.2f km, time of %.2f hours, $runs runs".format(dist*1.609f, time)
                }
                layout.addView(txt)
                dist = 0f
                time = 0f
                runs = 0
                currentDay = Date(currentDay.year, currentDay.month-1, currentDay.date)
                month++
            }
        }

        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
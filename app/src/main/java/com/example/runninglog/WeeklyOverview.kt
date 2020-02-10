package com.example.runninglog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log.d
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.view_runs.*
import kotlinx.android.synthetic.main.view_runs.backButton
import kotlinx.android.synthetic.main.weekly_overview.*
import java.text.SimpleDateFormat
import java.util.*

class WeeklyOverview : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Set the color theme
        when (SettingsObject.color) {
            "1" -> setTheme(R.style.blueTheme)
            "2" -> setTheme(R.style.darkTheme)
            "3" -> setTheme(R.style.goldTheme)
            "4" -> setTheme(R.style.darkGoldTheme)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.weekly_overview)

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

        val weekMillis = 604800000L
        val dayMillis = 86400000L
        var week = 1
        var ctr = 0

        val c = Calendar.getInstance()
        if (SettingsObject.firstDay == "mon") {
            c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        }
        else {
            c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
        }
        var currentDate = c.time


        var dist = 0f
        var time = 0f
        var runs = 0

        val ft = SimpleDateFormat("MM/dd/yy")

        while (week < 100 && ctr < allRuns.size) {
            val diff = allRuns[ctr].getDate().time - currentDate.time
            // d("diff", "for run on date ${allRuns[ctr].getDate()}, currentSunday = $currentSunday, diff == $diff")
            if (diff > weekMillis) {
                ctr++
            }
            else if (diff > 0) {
                dist += allRuns[ctr].distance
                time += allRuns[ctr].minutes
                runs++
                ctr++
            }
            else if (Date().compareTo(currentDate) < 0) {
                currentDate = Date(currentDate.time - weekMillis)
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
                val dayAfter = Date(currentDate.time + dayMillis)
                if (SettingsObject.unitsSetting == "miles") {
                    txt.text = "Week of ${ft.format(dayAfter)}\nTotal Distance of %.2f miles, time of %.2f hours, $runs runs\n-------------------".format(dist, time)
                }
                else {
                    txt.text = "Week of ${ft.format(dayAfter)}\nTotal Distance of %.2f km, time of %.2f hours, $runs runs".format(dist*1.609f, time)
                }
                layout.addView(txt)
                dist = 0f
                time = 0f
                runs = 0
                currentDate = Date(currentDate.time - weekMillis)
                week++
            }
        }

        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
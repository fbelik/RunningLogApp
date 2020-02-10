package com.example.runninglog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log.d
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.enter_run.*
import java.io.File
import java.lang.StringBuilder
import java.util.*

// dist, total_time_mins, month, day, year, pace_mins, pace_secs, speed
class Run(dist: Float, total_mins: Float, month: Int, day: Int, year: Int, title: String, description: String) : Comparable<Run> {
    var distance = 0f
    var minutes = 0f
    var month = 0
    var day = 0
    var year = 0
    var pace_mins = 0
    var pace_secs = 0
    var speed = 0f
    var title = ""
    var description = ""
    var stravaId = ""

    init {
        distance = dist
        minutes = total_mins
        this.month = month
        this.day = day
        this.year = year
        val pace = minutes / dist
        pace_mins = pace.toInt()
        pace_secs = ((pace - pace_mins) * 60).toInt()
        speed = 60f * dist / minutes
        this.title = title
        this.description = description
    }

    override fun toString(): String {
        val hrs = (minutes / 60).toInt()
        val mins_rem = minutes - hrs*60
        val mins = mins_rem.toInt()
        val secs = ((mins_rem - mins) * 60).toInt()
        return "Date - %d/%d/%d\nDistance - %.2f miles\nTotal Time - %02d:%02d:%02d\nPace - %d:%02d minutes/mile\nAverage Speed - %.2f mi/hr\nDescription - %s".format(month,day,year,distance,hrs,mins,secs,pace_mins,pace_secs,speed,description)
    }

    fun toStringKm(): String {
        val hrs = (minutes / 60).toInt()
        val mins_rem = minutes - hrs*60
        val mins = mins_rem.toInt()
        val secs = ((mins_rem - mins) * 60).toInt()
        val dst = distance * 1.609f
        val paceKm = (pace_mins + pace_secs/60f) / 1.609f
        val pmins = paceKm.toInt()
        val psecs = ((paceKm - pmins) * 60).toInt()
        val speedKm = speed / 1.609f
        return "Date - %d/%d/%d\nDistance - %.2f km\nTotal Time - %02d:%02d:%02d\nPace - %d:%02d minutes/km\nAverage Speed - %.2f km/hr\nDescription - %s".format(month,day,year,dst,hrs,mins,secs,pmins,psecs,speedKm,description)
    }

    fun buttonTitle(): String {
        return "%s %d/%d/%d - %.2f miles".format(title, month, day, year%1000, distance)
    }

    fun buttonTitleKm(): String {
        return "%s %d/%d/%d - %.2f km".format(title, month, day, year%1000, distance * 1.609f)
    }

    override fun compareTo(other: Run) : Int {
        return other.getDate().compareTo(this.getDate())
    }

    fun getDate() : Date {
        return Date(year-1900,month-1,day)
    }
}

class EnterRun : AppCompatActivity() {

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
        setContentView(R.layout.enter_run)

        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val gson = Gson()

        val orig = sharedPreferences.getString("run_data", null)
        val type = object : TypeToken<MutableList<Run>>() {}.type
        val theRuns : MutableList<Run>? = gson.fromJson(orig, type)
        var allRuns = mutableListOf<Run>()
        if (theRuns != null) {
            allRuns = theRuns
        }

        val currentDate = Date()
        dateEntryM.setText("%02d".format(currentDate.month + 1))
        dateEntryD.setText("%02d".format(currentDate.date))
        dateEntryY.setText("%04d".format(currentDate.year + 1900))

        if (SettingsObject.unitsSetting == "km") {
            toggleKm.isChecked = true
        }

        homeButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        submitButton.setOnClickListener {
            try {
                var dist = milesEntry.text.toString().toFloat()
                if (toggleKm.isChecked) {
                    dist /= 1.60934f // Convert to miles
                }
                val hour = timeEntryH.text.toString().toInt()
                val mins = timeEntryM.text.toString().toInt()
                val secs = timeEntryS.text.toString().toInt()
                val month = dateEntryM.text.toString().toInt()
                val day = dateEntryD.text.toString().toInt()
                val year = dateEntryY.text.toString().toInt()
                val total_time_mins = hour * 60 + mins + (secs / 60f)
                var title = titleText.text.toString().replace("\n", " ")
                if (title.isEmpty()) {
                    title = "none"
                }
                var description = descriptionText.text.toString().replace("\n", " ")
                if (description.isEmpty()) {
                    description = "none"
                }
                val run = Run(dist,total_time_mins,month,day,year,title,description)
                allRuns.add(run)
                allRuns.sort()

                val json = gson.toJson(allRuns)
                val editor = sharedPreferences.edit()
                editor.putString("run_data", json)
                editor.apply()

                val alert = AlertDialog.Builder(this)
                alert.setCancelable(false)
                alert.setTitle("Success")
                alert.setMessage("Run was successfully saved!")
                alert.setPositiveButton("Proceed") { dialog, id ->
                    startActivity(Intent(this, MainActivity::class.java))
                }
                alert.show()
            }
            catch (e : java.lang.NumberFormatException) {
                val alert = AlertDialog.Builder(this)
                alert.setTitle("Error")
                alert.setMessage("One of the entries was not filled properly")
                alert.setCancelable(false)
                alert.setPositiveButton("Ok") { dialog, id ->
                    d("filip","invalid entry")
                }
                alert.show()
            }
        }

        timeEntryH.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p: Editable?) {
                if (timeEntryH.text.length == 1) {
                    timeEntryM.requestFocus()
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        timeEntryM.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p: Editable?) {
                if (timeEntryM.text.length == 2) {
                    timeEntryS.requestFocus()
                }
                else if (timeEntryM.text.length > 2) {
                    timeEntryM.setText(timeEntryM.text.slice(0 until 2))
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        timeEntryS.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p: Editable?) {
                if (timeEntryS.text.length == 2) {
                    dateEntryM.requestFocus()
                }
                else if (timeEntryS.text.length > 2) {
                    timeEntryM.setText(timeEntryS.text.slice(0 until 2))
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        dateEntryM.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p: Editable?) {
                if (dateEntryM.text.length == 2) {
                    dateEntryD.requestFocus()
                }
                else if (dateEntryM.text.length > 2) {
                    dateEntryM.setText(dateEntryM.text.slice(0 until 2))
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        dateEntryD.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p: Editable?) {
                if (dateEntryD.text.length == 2) {
                    dateEntryY.requestFocus()
                }
                else if (dateEntryD.text.length > 2) {
                    dateEntryD.setText(dateEntryD.text.slice(0 until 2))
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        dateEntryY.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p: Editable?) {
                if (dateEntryY.text.length == 4) {
                    descriptionText.requestFocus()
                }
                if (dateEntryY.text.length > 4) {
                    dateEntryY.setText(dateEntryY.text.slice(0 until 4))
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
    }
}
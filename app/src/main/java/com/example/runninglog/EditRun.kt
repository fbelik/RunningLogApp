package com.example.runninglog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.edit_run.editBackButton
import kotlinx.android.synthetic.main.edit_run.dateEntryD
import kotlinx.android.synthetic.main.edit_run.dateEntryM
import kotlinx.android.synthetic.main.edit_run.dateEntryY
import kotlinx.android.synthetic.main.edit_run.descriptionText
import kotlinx.android.synthetic.main.edit_run.milesEntry
import kotlinx.android.synthetic.main.edit_run.submitButton
import kotlinx.android.synthetic.main.edit_run.timeEntryH
import kotlinx.android.synthetic.main.edit_run.timeEntryM
import kotlinx.android.synthetic.main.edit_run.timeEntryS
import kotlinx.android.synthetic.main.edit_run.titleText
import kotlinx.android.synthetic.main.edit_run.toggleKm
import java.io.File

class EditRun : AppCompatActivity() {
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
        setContentView(R.layout.edit_run)

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

        val internalStorage : File = filesDir
        val toEdit = File(internalStorage, "toEdit.txt")
        val editing = toEdit.readText().toInt()

        val run = allRuns[editing]

        titleText.setText(run.title)
        milesEntry.setText(run.distance.toString())
        val totalMins = run.minutes
        val hours = (totalMins / 60f).toInt()
        val minsLeft = totalMins - hours
        val mins = minsLeft.toInt()
        val secs = ((minsLeft - mins) * 60).toInt()
        timeEntryH.setText("%02d".format(hours))
        timeEntryM.setText("%02d".format(mins))
        timeEntryS.setText("%02d".format(secs))
        dateEntryM.setText("%02d".format(run.month))
        dateEntryD.setText("%02d".format(run.day))
        dateEntryY.setText(run.year.toString())
        descriptionText.setText(run.description)

        editBackButton.setOnClickListener {
            startActivity(Intent(this, ViewRuns::class.java))
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
                val newRun = Run(dist,total_time_mins,month,day,year,title,description)
                allRuns.removeAt(editing)
                allRuns.add(newRun)
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
                    startActivity(Intent(this, ViewRuns::class.java))
                }
                alert.show()
            }
            catch (e : java.lang.NumberFormatException) {
                val alert = AlertDialog.Builder(this)
                alert.setTitle("Error")
                alert.setMessage("One of the entries was not filled properly")
                alert.setCancelable(false)
                alert.setPositiveButton("Ok") { dialog, id ->
                    Log.d("filip", "invalid entry")
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
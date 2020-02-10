package com.example.runninglog

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.provider.Settings
import android.util.Log.d
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.view_runs.*
import java.io.File
import java.lang.StringBuilder
import java.util.*

class ViewRuns : AppCompatActivity() {

    private fun readRun(scanner: Scanner) : MutableList<String> {
        val result = mutableListOf<String>()
        for (i in 0 until 8) {
            result.add(scanner.next())
            d("errorCheck","added ${result[result.size-1]}")
        }
        // Read in the description
        // result.add(scanner.next().replace("_", " "))
        return result
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
        setContentView(R.layout.view_runs)

        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val gson = Gson()

        val orig = sharedPreferences.getString("run_data", null)
        val type = object : TypeToken<MutableList<Run>>() {}.type
        val theRuns : MutableList<Run>? = gson.fromJson(orig, type)
        var allRuns = mutableListOf<Run>()
        if (theRuns != null) {
            allRuns = theRuns
        }

        if (allRuns.size == 1)
            numRuns.text = "Total of 1 run"
        else
            numRuns.text = "Total of ${allRuns.size} runs"

        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Get the layout to place buttons for each previous run
        val layout = layoutInScroll
        val buttons = mutableListOf<Button>()
        val statusTexts = mutableListOf<String>()
        for (ct in allRuns.indices) {
            val run = allRuns[ct]
            // Key for contents:
            // dist, total_time_mins, month, day, year, pace_mins, pace_secs, speed
            val btn = Button(this)
            btn.tag = "run$ct"
            val hours = "%02d".format((run.minutes / 60f).toInt())
            val mins = "%02d".format((run.minutes - hours.toInt()).toInt())
            val secs = "%02d".format(((run.minutes - mins.toInt()) * 60).toInt())
            if (SettingsObject.unitsSetting == "miles") {
                btn.text = run.buttonTitle()
                statusTexts.add(run.toString())
            } else {
                btn.text = run.buttonTitleKm()
                statusTexts.add(run.toStringKm())
            }
            when (SettingsObject.color) {
                "1" -> {
                    btn.setTextColor(resources.getColor(R.color.buttonTextColor1))
                    btn.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.buttonColor1))
                }
                "2" -> {
                    btn.setTextColor(resources.getColor(R.color.buttonTextColor2))
                    btn.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.buttonColor2))
                }
                "3" -> {
                    btn.setTextColor(resources.getColor(R.color.buttonTextColor3))
                    btn.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.buttonColor3))
                }
                "4" -> {
                    btn.setTextColor(resources.getColor(R.color.buttonTextColor4))
                    btn.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.buttonColor4))
                }
                "5" -> {
                    btn.setTextColor(resources.getColor(R.color.buttonTextColor5))
                    btn.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.buttonColor5))
                }
            }
            buttons.add(btn)
            layout.addView(btn)
        }

        var toDelete = -1

        for (i in 0 until buttons.size) {
            val btn = buttons[i]
            btn.setOnClickListener {
                for (j in allRuns.indices) {
                    buttons[j].visibility = View.INVISIBLE
                }
                runsBackButton.visibility = View.VISIBLE
                deleteButton.visibility = View.VISIBLE
                editButton.visibility = View.VISIBLE
                headerText.text = btn.text
                statsText.text = statusTexts[i]
                toDelete = i
            }
        }

        deleteButton.setOnClickListener {
            if (toDelete > -1 && toDelete < allRuns.size) {
                // Send alert to double check
                val alert = AlertDialog.Builder(this)
                alert.setTitle("Check")
                alert.setMessage("Are you sure you want to delete this run")
                alert.setCancelable(false)
                alert.setPositiveButton("Proceed") { dialog, id ->
                    allRuns.removeAt(toDelete)
                    toDelete = -1
                    val json = gson.toJson(allRuns)
                    val editor = sharedPreferences.edit()
                    editor.putString("run_data", json)
                    editor.apply()
//                    val toAdd = StringBuilder()
//                    for (i in allContents) {
//                        toAdd.append(i.joinToString(","))
//                        toAdd.append(",")
//                    }
//                    csv.writeText(toAdd.toString())
                    startActivity(Intent(this, ViewRuns::class.java))
                }
                alert.setNegativeButton("Cancel") { dialog, id ->
                    d("filipbelik", "canceled deletion")
                }
                alert.show()
            }
        }

        runsBackButton.setOnClickListener {
            for (i in allRuns.indices) {
                buttons[i].visibility = View.VISIBLE
            }
            runsBackButton.visibility = View.INVISIBLE
            deleteButton.visibility = View.INVISIBLE
            editButton.visibility = View.INVISIBLE
            headerText.text = ""
            statsText.text = ""
            toDelete = -1
        }

        editButton.setOnClickListener {
            val internalStorage : File = filesDir
            val toEdit = File(internalStorage, "toEdit.txt")
            toEdit.writeText(toDelete.toString())
            startActivity(Intent(this, EditRun::class.java))
        }
        d("filip","finished with total of ${allRuns.size} runs")
    }
}
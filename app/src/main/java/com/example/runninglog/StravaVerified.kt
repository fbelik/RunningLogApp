package com.example.runninglog

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Log.d
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.view_runs.*
import okhttp3.*
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.*

class StravaVerified : AppCompatActivity() {

    val http_client = OkHttpClient()
    var allRuns = mutableListOf<Run>()
    var stravaIds = sortedSetOf<String>()

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
        setContentView(R.layout.strava_verified)
        val internalStorage: File = filesDir
        val access_file = File(internalStorage, "strava_access.txt")
        var scanner = Scanner(access_file)
        if (!scanner.hasNext()) {
            d("error", "no access token found")
            startActivity(Intent(this, MainActivity::class.java))
        }
        val access_token = scanner.next()
        d("access in verified", "access == $access_token")
        scanner.close()

        // Get run data
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val gson = Gson()

        val orig = sharedPreferences.getString("run_data", null)
        val type = object : TypeToken<MutableList<Run>>() {}.type
        val theRuns : MutableList<Run>? = gson.fromJson(orig, type)
        if (theRuns != null) {
            allRuns = theRuns
        }

        for (run in allRuns) {
            if (run.stravaId.isNotEmpty()) {
                stravaIds.add(run.stravaId)
            }
        }

        getListOfRuns(1, access_token)
    }

    fun getListOfRuns(page: Int, access_token: String) {
        val url = "https://www.strava.com/api/v3/activities"
        d("url visit", "visiting url ${"$url?access_token=$access_token&per_page=50&page=$page"}")

        val request = Request.Builder()
            .url("$url?access_token=$access_token&per_page=50&page=$page")
            .build()

        http_client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                d("api_response", body)
                val gson = GsonBuilder().create()

                val activities = gson.fromJson(body, Array<Activity>::class.java).toList()

                for (activity in activities) {
                    if (activity.type == "Run" && !stravaIds.contains(activity.id)) {
                        val distance = activity.distance / 1609.34f
                        val minutes = activity.elapsed_time / 60f
                        val date = activity.start_date
                        val year = date.slice(0 until 4).toInt()
                        val month = date.slice(5 until 7).toInt()
                        val day = date.slice(8 until 10).toInt()
                        val title = activity.name
                        var description = "none"
                        if (!activity.description.isNullOrBlank()) {
                            description = activity.description
                        }
                        val run = Run(distance,minutes,month,day,year,title,description)
                        run.stravaId = activity.id
                        allRuns.add(run)
                    }
                }

                if (activities.size > 0) {
                    getListOfRuns(page + 1, access_token)
                }

                else {
                    runOnUiThread {
                        allRuns.sort()
                        val json = gson.toJson(allRuns)
                        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("run_data", json)
                        editor.apply()
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                d("failure", "failed to access run data, returning to home screen")

                runOnUiThread {
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                }
            }
        })
    }
}

class Activity(val name: String, val distance: Float, val moving_time: Int, val elapsed_time: Int, val type: String, val start_date: String, val id: String, val description: String?)

// Example output:
// {'resource_state': 2,
// 'athlete': {'id': 43525224, 'resource_state': 1},
// 'name': 'Afternoon Run',
// 'distance': 6974.2,
// 'moving_time': 2335,
// 'elapsed_time': 2533,
// 'total_elevation_gain': 4    9.0,
// 'type': 'Run',
// 'workout_type': None,
// 'id': 2478001434,
// 'external_id': 'garmin_push_3781033269',
// 'upload_id': 2632560793,
// 'start_date': '2016-02-25T23:04:27Z',
// 'start_date_local': '    2016-02-25T17:04:27Z',
// 'timezone': '(GMT-06:00) America/Chicago',
// 'utc_offset': -21600.0,
// 'start_latlng': [44.908351, -92.87464],
// 'end_latlng': [44.905941, -92.87384],
// 'location_city':     None,
// 'location_state': None,
// 'location_country': None,
// 'start_latitude': 44.908351,
// 'start_longitude': -92.87464,
// 'achievement_count': 2,
// 'kudos_count': 0,
// 'comment_count': 0,
// 'athlete    _count': 1,
// 'photo_count': 0,
// 'map': {'id': 'a2478001434', 'summary_polyline': 'edbqGnqzuPc@Gc@Ae@FMPHv@CjB@lBANiB_Ac@ImBKgAQi@Dc@Hc@T_ErAg@LiAPgA\\mARc@DoA\\a@Hg@BkAVe@DkAVqBVyDIyCJe@D    g@?kACe@EqA@yD]e@CKDCp@?z@C`DCv@Iv@?fBGlBUp@q@nAWhB@~FAt@Et@_AjCWl@Yd@_@d@_@Rg@?e@Ge@QgAq@g@SmAMiAJe@[a@S_@[kBeAcAs@[a@Ms@W{@~@y@|B_ClB}CXi@~@eCPu@|@qCLo@Xo@Vi@\\e@~@_A^YfAg@hAMl@?zDJHH    EpBEr@EfBe@nEEjBMx@Yb@Ul@Ot@Er@@nBGvEi@pCUj@Ud@o@z@c@Ra@@OAABWCaAm@a@O]]e@IWDe@Eg@Hc@KeA{@]e@qCsAc@]Ug@_@uABM`Au@`BcBz@iAr@uAfAkCb@}ATm@Ps@d@}Al@wAXi@Zc@\\_@^YdAq@jAQh@Ef@@tBNrBBfAFf@Af    @Hd@BnADb@Dd@?d@BjAAd@BhFGf@Bd@AlBe@d@CpB]hAMd@Ab@IhA]d@GlAa@hASpCs@dA_@hA[b@Ud@Mf@Bb@FpBDf@HhA^b@TJ@BI?y@Ew@@u@Pm@Hs@\\[`@Kb@Db@R`Ax@b@T^]`@Wx@cA^Ud@Q^w@\\Y|@EG?', 'resource_state': 2}    ,
// 'trainer': False,
// 'commute': False,
// 'manual': False,
// 'private': False,
// 'visibility': 'everyone',
// 'flagged': False,
// 'gear_id': None,
// 'from_accepted_tag': False,
// 'upload_id_str': '26325    60793',
// 'average_speed': 2.987,
// 'max_speed': 4.4,
// 'has_heartrate': False,
// 'heartrate_opt_out': False,
// 'display_hide_heartrate_option': False,
// 'elev_high': 308.1,
// 'elev_low': 281.4,
// 'pr_    count': 1,
// 'total_photo_count': 0,
// 'has_kudoed': False}

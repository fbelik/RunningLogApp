package com.example.runninglog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log.d
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.google.gson.internal.GsonBuildConfig
import okhttp3.*
import java.io.File
import java.io.IOException

class StravaGetAccess : AppCompatActivity() {

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
        val data = intent.toUri(0)
        var i = 27
        while (!(data[i] == '=' && data.slice(i - 4 until i) == "code")) {
            i++
        }
        i++
        var j = i
        while (data[j] != '&') {
            j++
        }
        val code = data.slice(i until j)

        d("access_code", "code = $code")
        d("full_url", "url = $data")

        // Use code with client ID and secret to get an access token that lasts 6hrs
        val client_id = "42279"
        val client_secret = "ff647c31dd58f2a510c91f941184ab22cab2b17d"

        val http_client = OkHttpClient()

        val formBody : RequestBody = FormBody.Builder()
            .add("client_id", client_id)
            .add("client_secret", client_secret)
            .add("code", code)
            .add("grant_type", "authorization_code")
            .build()

        val request = Request.Builder()
            .url("https://www.strava.com/oauth/token")
            .post(formBody)
            .build()
        http_client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                d("api_response", body)
                val gson = GsonBuilder().create()

                val output = gson.fromJson(body, AccessResponse::class.java)
                if (output == null) {
                    d("error", "output was null, returning home")
                }

                // Get internal csv file
                val internalStorage: File = getFilesDir()
                val stravaFile = File(internalStorage, "strava_access.txt")
                stravaFile.createNewFile()
                stravaFile.writeText(output.access_token)
                d("access token", "the access token was ${output.access_token}")

                runOnUiThread {
                    startActivity(Intent(applicationContext, StravaVerified::class.java))
                }
            }
            override fun onFailure(call: Call, e: IOException) {
                d("failure","failed to access web to get access token")
                runOnUiThread {
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                }
            }
        })
    }
}

class Athlete(val id: String, val username: String, val resource_state: Int, val firstname: String, val lastname: String)

class AccessResponse(val token_type: String, val expires_at: String, val expires_in: String, val refresh_token: String, val access_token: String, val athlete: Athlete)



// Example API access token response
// {"token_type":"Bearer",
// "expires_at":1580011294,
// "expires_in":21600,
// "refresh_token":"e107806805cdc5f507446677beaa1fbc5b91bd67",
// "access_token":"be3e14b8b24889f549106e06397dcbecd793b7cb",
// "athlete":{"id":43525224,
//            "username":null,
//            "resource_state":2,
//            "firstname":"Filip",
//            "lastname":"Belik",
//            "city":"",
//            "state":"",
//            "country":null,
//            "sex":"M",
//            "premium":false,
//            "summit":false,
//            "created_at":"2019-06-22T15:59:25Z",
//            "updated_at":"2020-01-25T21:51:13Z",
//            "badge_type_id":0,
//            "profile_medium":"https://dgalywyr863hv.cloudfront.net/pictures/athletes/43525224/12303820/1/medium.jpg",
//            "profile":"https://dgalywyr863hv.cloudfront.net/pictures/athletes/43525224/12303820/1/large.jpg",
//            "friend":null,
//            "follower":null}}
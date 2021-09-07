package ru.callagent.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import ru.callagent.BuildConfig
import ru.callagent.CallAgentManager
import ru.callagent.R
import ru.callagent.helpers.Log
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private lateinit var callAgentManager: CallAgentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        callAgentManager = CallAgentManager.Builder(this)
                .setLogEnable(true)
                .build()
        refreshTextInfo()
    }

    fun startCallAgentServiceClick(view: View) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val accountId = sharedPref.getString("account_id", "")
        val phoneNumber = sharedPref.getString("number", "")
        val uploadUrl = sharedPref.getString("uploadUrl", "")
        Log.i("MainActivity.StartCallRecordClick", "accountId $accountId phoneNumber $phoneNumber uploadUrl $uploadUrl")
        if (accountId != "" && phoneNumber != "" && uploadUrl != "") {
            checkAndRequestPermissions()
            Log.i(TAG, "StartCallAgentServiceClick")
            callAgentManager.startCallAgentService()
        } else {
            Toast.makeText(applicationContext, "Не заданы настройки", Toast.LENGTH_LONG).show()
        }

    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.PROCESS_OUTGOING_CALLS,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.CALL_PHONE
            ), 1)
        }
    }

    private fun refreshTextInfo() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val textInfo: TextView = findViewById(R.id.text_info)
        val accountId = sharedPref.getString("account_id", "")
        val phoneNumber = sharedPref.getString("number", "")
        val uploadUrl = sharedPref.getString("uploadUrl", "")
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        textInfo.setText("Аккаунт: $accountId\nТелефон: $phoneNumber\nАдрес загрузки данных:\n$uploadUrl\n")

        val buildInfo: TextView = findViewById(R.id.build_info)
        val buildDateString = formatter.format(Date(BuildConfig.BUILD_TIME))
        buildInfo.setText("Cборка от $buildDateString")
    }

    fun stopCallAgentServiceClick(view: View) {
        Log.i(TAG, "StopCallRecordClick")
        callAgentManager.stopCallAgentService()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i("onOptionsItemSelected", item.title.toString())
        return when (item.itemId) {
            R.id.action_settings -> {
                Log.i(TAG, "Menu taknuto")
                val intent = Intent()
                intent.setClass(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.action_view_history -> {
                Log.i(TAG, "History tuknato")
                val intent = Intent()
                intent.setClass(this, HistoryActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    override fun onResume() {
        super.onResume()
        refreshTextInfo()
    }

}

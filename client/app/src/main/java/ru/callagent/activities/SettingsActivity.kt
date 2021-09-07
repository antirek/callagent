package ru.callagent.activities

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import ru.callagent.R
import ru.callagent.helpers.Log
import ru.callagent.helpers.NetworkHelper
import java.io.File
import java.io.PrintWriter


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun logClearButtonClick(view: View) {
        val logFile = getLogFile()
        if (logFile.exists()) {
            val writer = PrintWriter(logFile)
            writer.print("")
            writer.close()
            Toast.makeText(applicationContext, "Лог файл очищен", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(applicationContext, "Лог файл не создан", Toast.LENGTH_LONG).show()
        }
    }

    fun logSendButtonClick(view: View) {
        val logFile = getLogFile()
        if (logFile.exists()) {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            NetworkHelper.sendLogFile(applicationContext, sharedPref, logFile)
            Toast.makeText(applicationContext, "Файл отправлен", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(applicationContext, "Лог файл не создан", Toast.LENGTH_LONG).show()
        }
    }

    private fun getLogFile(): File {
        val fileName = "log.txt"
        val path = Environment.getExternalStoragePublicDirectory("CallAgent")
        return File(path, fileName)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            Log.i("SettingsActivity.onCreatePreference.rootkey", rootKey.toString())
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val onSharedPreferenceChangeListener = OnSharedPreferenceChangeListener { sharedPreferences, s ->
                if (s == "templateForModel") {
                    val newValue = sharedPreferences.getString(s, "default")
                    if (newValue=="default") {
                        findPreference<androidx.preference.EditTextPreference>("pathToRecordDir")?.setEnabled(true)
                    }else{
                        findPreference<androidx.preference.EditTextPreference>("pathToRecordDir")?.setEnabled(false)
                    }
                }
            }
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
            val value = sharedPref.getString("templateForModel", "default");
            if (value == "default") {
                findPreference<androidx.preference.EditTextPreference>("pathToRecordDir")?.setEnabled(true)
            } else {
                findPreference<androidx.preference.EditTextPreference>("pathToRecordDir")?.setEnabled(false)
            }
            sharedPref.registerOnSharedPreferenceChangeListener(
                    onSharedPreferenceChangeListener)
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
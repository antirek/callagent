package ru.callagent.activities

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import ru.callagent.R
import ru.callagent.helpers.DBHelper
import ru.callagent.helpers.Log


class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        fillHistoryTable()
    }

    fun fillHistoryTable() {
        try {
            val dbh = DBHelper(this, 2)
            val db: SQLiteDatabase = dbh.writableDatabase // Открываем базу для записи
            val calls = dbh.getCalls(db)
            val callsMaped = ArrayList<HashMap<String, String>>()
            for (call in calls) {
                val c = HashMap<String, String>()
                c.put("startDate", call.startDate.toString())
                c.put("direction", call.direction.toString())
                c.put("number", call.number.toString())
                c.put("uploaded", call.uploaded.toString())
                c.put("answered", call.answered.toString())
                callsMaped.add(c)
            }
            Log.i("historyActivity", calls.size.toString())
            val listView = findViewById<ListView>(R.id.historyActivityTable)
            val adapter = SimpleAdapter(
                    this,
                    callsMaped,
                    R.layout.history_list_row,
                    arrayOf("startDate", "direction", "number", "uploaded"),
                    intArrayOf(R.id.startDate, R.id.direction, R.id.number, R.id.historyListUploaded)
            )
            listView.adapter = adapter
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "Ошибка произошла", Toast.LENGTH_LONG).show()
        }
    }

    fun clearCallsButtonClick(view: View) {
        Log.i("SettingsActivity", "clearCallButtonClick")
        val dbh = DBHelper(this, 2)
        val db: SQLiteDatabase = dbh.writableDatabase // Открываем базу для записи
        dbh.clearCalls(db)
        Toast.makeText(applicationContext, "История звонков очищена", Toast.LENGTH_LONG).show()
        fillHistoryTable()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

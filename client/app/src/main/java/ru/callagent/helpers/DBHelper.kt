package ru.callagent.helpers

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*
import kotlin.collections.ArrayList

class DBHelper(context: Context, version: Int)// myDB – имя базы данных
    : SQLiteOpenHelper(context, "calls.db", null, version) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("create table Calls (id integer primary key autoincrement, startDate text,endDate text, number text, recordUrl text,direction text,uuid text,answered integer,uploaded integer)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS Calls")
        onCreate(db)
    }

    fun getCalls(db: SQLiteDatabase): List<Call> {
        val c = db.rawQuery("select * from Calls order by startDate desc limit 10", null)
        val result = ArrayList<Call>()
        if (c.moveToFirst()) {
            do {
                result.add(Call(c))
            } while (c.moveToNext())
        }
        c.close()
        return result
    }

    fun clearCalls(db: SQLiteDatabase){
        db.execSQL("DELETE FROM Calls")
        db.close()
    }

    fun getNotUploadedCalls(db: SQLiteDatabase): List<Call> {
        val c = db.rawQuery("select * from Calls where uploaded=0 OR uploaded='0'", null)
        val result = ArrayList<Call>()
        if (c.moveToFirst()) {
            do {
                result.add(Call(c))
            } while (c.moveToNext())
        }
        c.close()
        return result
    }

    fun addCall(db: SQLiteDatabase, call: Call) {
        Log.i("DBHELPER", "insert call")
        db.execSQL(String.format(
                "INSERT INTO Calls ( 'startDate','endDate', 'direction','uuid' , 'number', 'recordUrl','answered','uploaded') VALUES ('%s', '%s', '%s','%s', '%s', '%s', '%s', '%s')",
                call.startDate, call.endDate, call.direction, call.uuid, call.number, call.recordUrl, call.getAnswered(), call.getUploaded()
        ))
        db.close()
    }

    fun updateCallUploaded(db: SQLiteDatabase, uuid: UUID, recordUrl: String, uploaded: Boolean) {
        if (uploaded) {
            db.execSQL(String.format("UPDATE Calls set uploaded='%s',recordUrl='%s' where uuid='%s'", 1, recordUrl, uuid))
        } else {
            db.execSQL(String.format("UPDATE Calls set uploaded='%s',recordUrl='%s' where uuid='%s'", 0, recordUrl, uuid))
        }
        db.close()
    }
}

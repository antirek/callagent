package ru.callagent.helpers

import android.database.Cursor
import java.text.SimpleDateFormat
import java.util.*

class Call {
    var id = 0
    var startDate: String? = null
    var endDate: String? = null
    var uuid: String? = null
    var direction: String? = null
    var number: String? = null
    var recordUrl: String? = null
    var answered: Boolean = true
    var uploaded: Boolean = false

    constructor(cursor: Cursor) {
        startDate = cursor.getString(cursor.getColumnIndex("startDate"))
        endDate = cursor.getString(cursor.getColumnIndex("endDate"))
        uuid = cursor.getString(cursor.getColumnIndex("uuid"))
        direction = cursor.getString(cursor.getColumnIndex("direction"))
        number = cursor.getString(cursor.getColumnIndex("number"))
        recordUrl = cursor.getString(cursor.getColumnIndex("recordUrl"))
        answered = cursor.getInt(cursor.getColumnIndex("answered")) == 1
        uploaded = cursor.getInt(cursor.getColumnIndex("uploaded")) == 1
        id = cursor.getInt(cursor.getColumnIndex("id"))
    }

    constructor(sd: Date, ed: Date, u: UUID, d: String, n: String?, r: String, a: Boolean) {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        startDate = formatter.format(sd)
        endDate = formatter.format(ed)
        uuid = u.toString()
        direction = d
        number = n.toString()
        recordUrl = r
        answered = a
    }

    fun getUploaded(): Int {
        if (uploaded) {
            return 1
        }
        return 0
    }

    fun getAnswered(): Int {
        if (answered) {
            return 1
        }
        return 0
    }

    fun toJsonString(): String {
        return "{" +
                "\"uploaded\":\"${uploaded}\"," +
                "\"direction\":\"${direction}\"," +
                "\"startDate\":\"${startDate}\"," +
                "\"endDate\":\"${endDate}\"," +
                "\"number\":\"${number.toString()}\"," +
                "\"uuid\":\"${uuid}\"," +
                "\"answered\":${answered}," +
                "\"id\":${id}," +
                "\"recordUrl\":\"${recordUrl}\"" +
                "}"
    }
}
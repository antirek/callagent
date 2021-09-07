package ru.callagent.helpers

import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

class CallEvent {
    var accountId: String? = null
    var direction: String? = null
    var startDate: String? = null
    var type: String? = null
    var number: String? = null
    var uuid: String? = null
    var mobile: String? = null

    constructor(sharedPref: SharedPreferences, date: Date, numberIn: String?, directionIn: String, typeIn: String, uuidIn: UUID) {
        accountId = sharedPref.getString("account_id", "")
        mobile = sharedPref.getString("number", "")
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        startDate = formatter.format(date)
        type = typeIn
        direction = directionIn
        uuid = uuidIn.toString()
        number = numberIn.toString()
    }

    fun toJsonString(): String {
        return "{" +
                "\"accountId\":\"${accountId}\"," +
                "\"direction\":\"${direction}\"," +
                "\"startDate\":\"${startDate}\"," +
                "\"type\":\"${type}\"," +
                "\"number\":\"${number.toString()}\"," +
                "\"uuid\":\"${uuid}\"," +
                "\"mobile\":\"${mobile}\"" +
                "}"
    }

    fun isValid(): Boolean {
        return (accountId != "") && (mobile != "")
    }
}
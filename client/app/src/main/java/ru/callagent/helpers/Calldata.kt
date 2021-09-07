package ru.callagent.helpers

import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

class Calldata {
    var accountId: String? = null
    var direction: String? = null
    var startDate: String? = null
    var endDate: String? = null
    var number: String? = null
    var uuid: String? = null
    var mobile: String? = null
    var answered: Boolean = true
    var recordPath: String? = ""

    constructor(sharedPref: SharedPreferences, call: Call) {
        accountId = sharedPref.getString("account_id", "")
        mobile = sharedPref.getString("number", "")
        direction = call.direction
        startDate = call.startDate
        endDate = call.endDate
        uuid = call.uuid
        number = call.number
        answered = call.answered
        recordPath= call.recordUrl
    }

    constructor(sharedPref: SharedPreferences, start: Date, end: Date, n: String?, d: String, u: UUID, a: Boolean, p: String = "") {
        accountId = sharedPref.getString("account_id", "")
        mobile = sharedPref.getString("number", "")
        direction = d
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        startDate = formatter.format(start)
        endDate = formatter.format(end)
        uuid = u.toString()
        number = n.toString()
        answered = a
        recordPath = p
    }

    fun toJsonString(): String {
        return "{" +
                "\"accountId\":\"${accountId}\"," +
                "\"direction\":\"${direction}\"," +
                "\"startDate\":\"${startDate}\"," +
                "\"endDate\":\"${endDate}\"," +
                "\"number\":\"${number.toString()}\"," +
                "\"uuid\":\"${uuid}\"," +
                "\"answered\":${answered}," +
                "\"mobile\":\"${mobile}\"," +
                "\"recordPath\":\"${recordPath}\"" +
                "}"
    }

    fun isValid(): Boolean {
        return (accountId != "") && (mobile != "")
    }
}
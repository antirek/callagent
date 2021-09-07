package ru.callagent.helpers

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FileDataPart
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.result.failure
import com.github.kittinunf.result.success
import java.io.File
import java.util.*

class NetworkHelper {
    companion object {
        fun sendCallDataInfo(sharedPref: SharedPreferences, callData: Calldata) {
            val uploadUrl = sharedPref.getString("uploadUrl","")
            if (callData.isValid()) {
                Fuel.post("${uploadUrl}/calldata/${callData.accountId}/${callData.mobile}/${callData.uuid}")
                        .header(Headers.CONTENT_TYPE, "application/json")
                        .body(callData.toJsonString())
                        .also { println(it) }
                        .response { result ->
                            Log.i(result.toString())
                        }
            }
        }

        fun sendEvent(sharedPref: SharedPreferences, callEvent: CallEvent) {
            val uploadUrl = sharedPref.getString("uploadUrl","")
            if (callEvent.isValid()) {
                Fuel.post("${uploadUrl}/event/${callEvent.accountId}/${callEvent.mobile}/${callEvent.uuid}")
                        .header(Headers.CONTENT_TYPE, "application/json")
                        .body(callEvent.toJsonString())
                        .also { println(it) }
                        .response { result ->
                            Log.i(result.toString())
                        }
            }
        }

        fun sendRecordFile(context: Context, sharedPref: SharedPreferences, audioFile: File?, uuid: UUID) {
            Log.i("NetworkHelper.start upload file for uuid", uuid.toString())
            if (audioFile != null) {
                val accountId = sharedPref.getString("account_id", "")
                val phoneNumber = sharedPref.getString("number", "")
                val uploadUrl = sharedPref.getString("uploadUrl","")
                if (accountId != "" && phoneNumber != "") {
                    Log.i("NetworkHelper.accountId number", "${accountId} ${phoneNumber}")
                    Fuel.upload("${uploadUrl}/record/${accountId}/${phoneNumber}/${uuid}")
                            .add {
                                FileDataPart(audioFile, name = "file", filename = audioFile.name)
                            }
                            .also { println(it) }
                            .response { result ->
                                val dbh = DBHelper(context, 2);
                                result.success {
                                    Log.i("NetworkHelper.sendRecordFile.success", it.toString())
                                    dbh.updateCallUploaded(dbh.writableDatabase, uuid, audioFile.path, true)
                                }
                                result.failure {
                                    Log.i("NetworkHelper.sendRecordFile.failure", it.toString())
                                    dbh.updateCallUploaded(dbh.writableDatabase, uuid, audioFile.path, false)
                                }
                            }
                } else {
                    Log.i("NetworkHelper. account and number not set in settings")
                }
            }
        }

        fun sendLogFile(context: Context, sharedPref: SharedPreferences, logFile: File?) {
            Log.i("NetworkHelper.start upload logfile")
            if (logFile != null) {
                val accountId = sharedPref.getString("account_id", "")
                val phoneNumber = sharedPref.getString("number", "")
                val uploadUrl = sharedPref.getString("uploadUrl","")
                if (accountId != "" && phoneNumber != "") {
                    Log.i("NetworkHelper.accountId number", "${accountId} ${phoneNumber}")
                    val copyFileName = "log_copy.txt"
                    val copyPath = Environment.getExternalStoragePublicDirectory("CallAgent")
                    val copyFile = File(copyPath, copyFileName)
                    logFile.copyTo(copyFile)
                    Fuel.upload("${uploadUrl}/log/${accountId}/${phoneNumber}")
                            .add {
                                FileDataPart(copyFile, name = "file", filename = logFile.name)
                            }
                            .also { println(it) }
                            .response { result ->
                                result.success {
                                    Log.i("NetworkHelper.sendLogFile.success", it.toString())
                                    copyFile.delete()
                                }
                                result.failure {
                                    Log.i("NetworkHelper.sendLogFile.failure", it.toString())
                                    copyFile.delete()
                                }
                            }
                } else {
                    Log.i("NetworkHelper. account and number not set in settings")
                }
            }
        }
    }
}
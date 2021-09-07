package ru.callagent.workers

import android.content.Context
import android.preference.PreferenceManager
import androidx.work.*
import ru.callagent.helpers.DBHelper
import ru.callagent.helpers.Log
import ru.callagent.helpers.Calldata
import ru.callagent.helpers.NetworkHelper
import java.io.File
import java.util.*


class UploadWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        uploadCalls()
        return Result.success()
    }

    private fun uploadCalls() {
        Log.i("UploadWorker", "UploadStarted ${Date()}")
        val dbh = DBHelper(applicationContext, 2)
        val db = dbh.writableDatabase
        val calls = dbh.getNotUploadedCalls(db)
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        Log.i("UploadWorker.calls to upload", calls.size.toString())
        for (call in calls) {
            Log.i("UploadWorker start upload call", call.toJsonString())
            if (call.recordUrl != "") {
                NetworkHelper.sendRecordFile(applicationContext, sharedPref, File(call.recordUrl), UUID.fromString(call.uuid))
                NetworkHelper.sendCallDataInfo(sharedPref,Calldata(sharedPref, call))
            } else {
                Log.i("UploadWorker.", "recordUrlNotSet")
                dbh.updateCallUploaded(dbh.writableDatabase, UUID.fromString(call.uuid), "", true)
            }
        }
    }

}
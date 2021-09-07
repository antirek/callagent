package ru.callagent

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ru.callagent.helpers.*
import ru.callagent.receivers.CallStateReceiver
import ru.callagent.services.CallAgentService
import ru.callagent.workers.UploadWorker
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CallAgentManager private constructor(private val context: Context) {
    private var callStateReceiver: CallStateReceiver? = null

    fun startCallStateReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(CallStateReceiver.ACTION_IN)
        intentFilter.addAction(CallStateReceiver.ACTION_OUT)

        if (callStateReceiver == null) {
            callStateReceiver = CallStateReceiver(this)
        }
        context.registerReceiver(callStateReceiver, intentFilter)
    }

    fun stopCallStateReceiver() {
        try {
            if (callStateReceiver != null) {
                context.unregisterReceiver(callStateReceiver)
            }
        } catch (e: Exception) {
            Log.e(e)
        }
    }

    fun startCallAgentService() {
        val intent = Intent()
        intent.setClass(context, CallAgentService::class.java)
        intent.action = "START"
        val uploadWorkRequest = PeriodicWorkRequestBuilder<UploadWorker>(
                15, TimeUnit.MINUTES)
                .build()
        WorkManager.getInstance(context).enqueue(uploadWorkRequest)
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val buildDateString = formatter.format(Date(BuildConfig.BUILD_TIME))
        Log.i("Starting the service build at $buildDateString")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i("Starting the service in >=26 Mode")
            context.startForegroundService(intent)
            return
        }
        Log.i("Starting the service in < 26 Mode")
        context.startService(intent)
    }

    fun stopCallAgentService() {
        val intent = Intent()
        intent.setClass(context, CallAgentService::class.java)
        intent.action = "STOP"
        context.stopService(intent)
        WorkManager.getInstance(context).cancelAllWork()
        Log.i(TAG, "stopService()")
    }

    class Builder(private val context: Context) {
        val logEnable: Boolean
            get() = PrefsHelper.readPrefBool(context, PREF_LOG_ENABLE)

        init {
            PrefsHelper.writePrefBool(context, PREF_LOG_ENABLE, false)
        }

        fun build(): CallAgentManager {
            val callRecord = CallAgentManager(context)

            if (logEnable) {
                Timber.plant(Timber.DebugTree())
            }

            return callRecord
        }

        fun setLogEnable(isEnable: Boolean = false): Builder {
            PrefsHelper.writePrefBool(context, PREF_LOG_ENABLE, isEnable)
            return this
        }
    }

    companion object {
        private val TAG = CallAgentManager::class.java.simpleName
        const val PREF_LOG_ENABLE = "PrefLogEnable"
    }
}

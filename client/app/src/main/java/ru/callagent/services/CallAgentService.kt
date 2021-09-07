package ru.callagent.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import ru.callagent.CallAgentManager
import ru.callagent.R
import ru.callagent.activities.MainActivity
import ru.callagent.helpers.Log

open class CallAgentService : Service() {

    private lateinit var callAgentManager: CallAgentManager

    private var isServiceStarted = false

    override fun onBind(intent: Intent): IBinder? {
        Log.i("Some component want to bind with the service")
        // We don't provide binding, so return null
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("onStartCommand executed with startId: $startId")
        if (intent != null) {
            val action = intent.action
            Log.i("using an intent with action $action")
            when (action) {
                "START" -> startService()
                "STOP" -> stopService()
                else -> Log.i("This should never happen. No action in the received intent")
            }
        } else {
            Log.i("with a null intent. It has been probably restarted by the system.")
        }

        // by returning this we make sure the service is restarted if the system kills the service
        return START_REDELIVER_INTENT
    }

    override fun onCreate() {
        super.onCreate()
        Log.i("The service has been created")
        val notification = createNotification()
        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        callAgentManager.stopCallStateReceiver()
        Log.i("The service has been destroyed")
        Toast.makeText(this, "Сервис остановлен", Toast.LENGTH_SHORT).show()
    }

    private fun startService() {
        callAgentManager = CallAgentManager.Builder(this).build()

        Log.i(TAG, "callAgentManager.startCallReceiver()")
        callAgentManager.startCallStateReceiver()
    }

    private fun stopService() {
        Log.i("Stopping the foreground service")
        Toast.makeText(this, "Сервис остановлен", Toast.LENGTH_SHORT).show()
        try {
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            Log.i("Service stopped without being started: ${e.message}")
        }
        isServiceStarted = false
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "ENDLESS SERVICE CHANNEL"

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                    notificationChannelId,
                    getString(R.string.app_notification_text),
                    NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = getString(R.string.app_name)
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }

        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
                this,
                notificationChannelId
        ) else Notification.Builder(this)

        return builder
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.app_notification_text))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(getString(R.string.app_name) + " - " + getString(R.string.app_notification_text))
                .setVisibility(Notification.VISIBILITY_SECRET)
                .build()
    }

    companion object {
        private val TAG = CallAgentService::class.java.simpleName
    }
}

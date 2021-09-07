package ru.callagent.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import ru.callagent.CallAgentManager

class OnBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val accountId = sharedPref.getString("account_id", "")
        val phoneNumber = sharedPref.getString("number", "")
        val startOnBoot = sharedPref.getBoolean("startOnBoot", false)
        if (accountId != "" && phoneNumber != "" && startOnBoot) {
            val callRecord = context?.let {
                CallAgentManager.Builder(it)
                        .setLogEnable(true)
                        .build()
            }
            callRecord?.startCallAgentService()
        }
    }
}
package ru.callagent.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.preference.PreferenceManager
import android.telephony.TelephonyManager
import ru.callagent.CallAgentManager
import ru.callagent.helpers.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.*

open class CallStateReceiver(private var callRecord: CallAgentManager) : BroadcastReceiver() {

    companion object {
        private val TAG = CallStateReceiver::class.java.simpleName

        const val ACTION_IN = "android.intent.action.PHONE_STATE"
        const val ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL"
        const val EXTRA_PHONE_NUMBER = "android.intent.extra.PHONE_NUMBER"
        private var lastState = TelephonyManager.CALL_STATE_IDLE
        private var callStartTime: Date = Date()
        private var uuid: UUID = UUID.randomUUID()
        private var isIncoming: Boolean = false
        private var savedNumber: String? = null  //because the passed incoming is only valid in ringing
    }

    override fun onReceive(context: Context, intent: Intent) {
        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
        Log.i("PhoneCallReceiver.onReceive uuid", uuid.toString())
        Log.i("PhoneCallReceiver.STATE_STR start", intent.extras!!.getString(TelephonyManager.EXTRA_STATE))
        Log.i("PhoneCallReceiver.action", intent.action)
        Log.i("PhoneCallReceiver.EXTRA_PHONE_NUMBER", intent.extras!!.getString(CallStateReceiver.EXTRA_PHONE_NUMBER))
        Log.i("PhoneCallReceiver.EXTRA_INCOMING_NUMBER", intent.extras!!.getString(TelephonyManager.EXTRA_INCOMING_NUMBER))
        Log.i("PhoneCallReceiver.SAVED_NUMBER", savedNumber)
        Log.i("PhoneCallReceiver.STATE_STR end", intent.extras!!.getString(TelephonyManager.EXTRA_STATE))

        if (intent.action == CallStateReceiver.ACTION_OUT) {
            Log.i("PhoneCallReciever", "ACTION_OUT")
            return
        }

        val stateStr = intent.extras!!.getString(TelephonyManager.EXTRA_STATE)
        val number = intent.extras!!.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
        if (number === null) {
            Log.i("PhoneCallReceiver.LOG", "event came without EXTRA_INCOMING_NUMBER");
            return
        }
        if (stateStr === TelephonyManager.EXTRA_STATE_IDLE) {
            savedNumber = null
        } else {
            savedNumber = number
        }

        var state = 0

        when (stateStr) {
            TelephonyManager.EXTRA_STATE_IDLE -> state = TelephonyManager.CALL_STATE_IDLE
            TelephonyManager.EXTRA_STATE_OFFHOOK -> state = TelephonyManager.CALL_STATE_OFFHOOK
            TelephonyManager.EXTRA_STATE_RINGING -> state = TelephonyManager.CALL_STATE_RINGING
        }
        Log.i("savedNumber.beforeCallStateChanged", savedNumber)
        onCallStateChanged(context, state, savedNumber)
    }

    fun onIncomingCallReceived(context: Context, number: String?, start: Date, uuid: UUID) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        NetworkHelper.sendEvent(sharedPref,CallEvent(sharedPref, start, number, "incoming", "START", uuid))
    }

    fun onIncomingCallAnswered(context: Context, number: String?, start: Date, uuid: UUID) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        NetworkHelper.sendEvent(sharedPref,CallEvent(sharedPref, Date(), number, "incoming", "ANSWER", uuid))
    }

    fun onIncomingCallEnded(context: Context, number: String?, start: Date, end: Date, uuid: UUID) {
        var recordPath = ""
        if (isRecordEnabled(context)) {
            recordPath = showLastUpdatedFile(context, uuid, start, end)
        }
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        NetworkHelper.sendEvent(sharedPref,CallEvent(sharedPref, Date(), number, "incoming", "END", uuid))
        NetworkHelper.sendCallDataInfo(sharedPref,Calldata(sharedPref, start, end, number, "incoming", uuid, true,recordPath))
        saveRecordToDb(context, Call(start, Date(), uuid, "incoming", number, recordPath, true))
    }

    fun onOutgoingCallStarted(context: Context, number: String?, start: Date, uuid: UUID) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        NetworkHelper.sendEvent(sharedPref,CallEvent(sharedPref, start, number, "outgoing", "START", uuid))
    }

    fun onOutgoingCallEnded(context: Context, number: String?, start: Date, end: Date, uuid: UUID) {
        var recordPath = ""
        if (isRecordEnabled(context)) {
            recordPath = showLastUpdatedFile(context, uuid, start, end)
        }
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        NetworkHelper.sendEvent(sharedPref,CallEvent(sharedPref, Date(), number, "outgoing", "END", uuid))
        NetworkHelper.sendCallDataInfo(sharedPref,Calldata(sharedPref, start, end, number, "outgoing", uuid, true,recordPath))
        saveRecordToDb(context, Call(start, Date(), uuid, "outgoing", number, recordPath, true))
    }

    fun onMissedCall(context: Context, number: String?, start: Date, uuid: UUID) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        NetworkHelper.sendEvent(sharedPref,CallEvent(sharedPref, Date(), number, "incoming", "END", uuid))
        NetworkHelper.sendCallDataInfo(sharedPref,Calldata(sharedPref, start, Date(), number, "incoming", uuid, false))
        saveRecordToDb(context, Call(start, Date(), uuid, "incoming", number, "", false))
    }

    private fun isRecordEnabled(context: Context): Boolean {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPref.getBoolean("isRecordEnabled", false)
    }

    private fun showLastUpdatedFile(context: Context, uuid: UUID, start: Date, end: Date): String {
        val pathToWatch = getPathToWatch(context)
        val dir = File(pathToWatch)
        val files = dir.listFiles()
        if (files == null || files.size == 0 || (files.size == 1 && files[0].endsWith("log.txt"))) {
            Log.i("FileWatcher", "no files in directory " + pathToWatch)
            return ""
        } else {
            var lastModifiedFile = files[0]
            for (i in 1 until files.size) {
                if (!files[i].endsWith("log.txt") && lastModifiedFile.lastModified() < files[i].lastModified()) {
                    lastModifiedFile = files[i]
                }
            }
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val durationFormatter = SimpleDateFormat("mm:ss")
            Log.i("FileWatcher", "------ start detect call record file, call uuid: ${uuid.toString()}")
            Log.i("FileWatcher", "last modified file is " + lastModifiedFile.absolutePath)
            Log.i("FileWatcher", "last_modified_time: " + formatter.format(lastModifiedFile.lastModified()))
            Log.i("FileWatcher", "current_time: " + formatter.format(System.currentTimeMillis()))
            Log.i("FileWatcher", "call start at " + formatter.format(start.time) + ", end at " + formatter.format(end.time))

            val callLengthInMsecs = end.time - start.time
            Log.i("FileWatcher", "call duration: " + durationFormatter.format(Date(callLengthInMsecs)))
            Log.i("FileWatcher", "call length in milliseconds ${callLengthInMsecs}")

            if (System.currentTimeMillis() - lastModifiedFile.lastModified() < callLengthInMsecs) {
                Log.i("FileWatcher", "last modified detect: ok")
                onRecordingFinished(context, callRecord, lastModifiedFile, uuid)
                return lastModifiedFile.path
            } else {
                Log.i("FileWatcher", "last modified detect: not ok (current_time - last_modified_time > call length)")
            }
            Log.i("FileWatcher", "------ end detect call record file, call uuid: ${uuid.toString()}")
            return ""
        }
    }

    private fun getPathToWatch(context: Context): String {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val pathToWatch = sharedPref.getString("pathToRecordDir", "/storage/emulated/0/CallRecorder/")
        val pathTemplate = sharedPref.getString("templateForModel", "default")
        when (pathTemplate) {
            // "xiaomi_redmi" -> return "/storage/emulated/0/MIUI/sound_recorder/call_rec/"
            "realme" -> return "/storage/emulated/0/Music/Recordings/Call Recordings/"
            "asus_zenfone" -> return "/storage/emulated/0/callrecordings/"
            // "meizu" -> return "/storage/emulated/0/Recorder/call/"
            else -> {
                return pathToWatch
            }
        }
    }

    // Derived classes could override these to respond to specific events of interest
    protected open fun onRecordingStarted(context: Context, callRecord: CallAgentManager, audioFile: File?, uuid: UUID) {}

    fun onRecordingFinished(context: Context, callRecord: CallAgentManager, audioFile: File?, uuid: UUID) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        NetworkHelper.sendRecordFile(context, sharedPref, audioFile, uuid)
    }

    fun saveRecordToDb(context: Context, call: Call) {
        val dbh = DBHelper(context, 2)
        val db: SQLiteDatabase = dbh.writableDatabase // Открываем базу для записи
        dbh.addCall(db, call)
    }
    fun onCallStateChanged(context: Context, state: Int, number: String?) {
        Log.i("PhoneCallReceiver.onCallStateChanged.state", state.toString())
        Log.i("PhoneCallReceiver.onCallStateChanged.uuid", uuid.toString())
        Log.i("PhoneCallReceiver.onCallStateChanged.number", number)
        if (lastState == state) {
            //No change, debounce extras
            return
        }

        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                isIncoming = true
                callStartTime = Date()
                savedNumber = number
                uuid = UUID.randomUUID()
                Log.i("BEFORE.onIncomingCallReceived", number)
                onIncomingCallReceived(context, number, callStartTime, uuid)
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false
                    callStartTime = Date()
                    uuid = UUID.randomUUID()
                    onOutgoingCallStarted(context, savedNumber, callStartTime, uuid)
                } else {
                    isIncoming = true
                    callStartTime = Date()

                    onIncomingCallAnswered(context, savedNumber, callStartTime, uuid)
                }
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    //Ring but no pickup-  a miss
                    onMissedCall(context, savedNumber, callStartTime, uuid)
                } else if (isIncoming) {
                    onIncomingCallEnded(context, savedNumber, callStartTime, Date(), uuid)
                } else {
                    onOutgoingCallEnded(context, savedNumber, callStartTime, Date(), uuid)
                }
                savedNumber = null
            }
        }
        lastState = state
    }
}

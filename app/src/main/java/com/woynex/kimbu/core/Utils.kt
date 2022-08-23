package com.woynex.kimbu.core

import android.annotation.SuppressLint
import java.text.SimpleDateFormat

@SuppressLint("SimpleDateFormat")
fun Long.millisToDate(format: String): String {
    val simpleDateFormat = SimpleDateFormat(format)
    return simpleDateFormat.format(this)
}

/*do {
val stringNumber = cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.NUMBER))
val stringName = cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.CACHED_NAME))
val stringId = cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls._ID))
val stringType = cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.TYPE))
val stringDate = cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.DATE))

stringOutput =
stringOutput + "Number: " + stringNumber + "\nName: " + stringName + "\nId: " + stringId + "\n Type: " + stringType + "\n\n" + "\n Date: " + stringDate.toLong()
    .millisToDate(dateFormat) + "\n\n"
} while (cursorCallLogs.moveToPrevious())
*/
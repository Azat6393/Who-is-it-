package com.woynex.kimbu.core.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.TELECOM_SERVICE
import android.content.pm.PackageManager
import android.telecom.TelecomManager
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.woynex.kimbu.R
import com.woynex.kimbu.feature_search.domain.model.CountryInfo
import java.text.SimpleDateFormat

@SuppressLint("SimpleDateFormat")
fun Long.millisToDate(format: String): String {
    val simpleDateFormat = SimpleDateFormat(format)
    return simpleDateFormat.format(this)
}


fun <T> convertToSet(list: List<T>): Set<T> {
    return HashSet(list)
}

fun getJsonFromAssets(context: Context, fileName: String): String? {
    var jsonString = ""
    try {
        val inputStream = context.assets.open(fileName)
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        jsonString = String(buffer)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
    return jsonString
}

fun String.fromJsonToCountyList(): List<CountryInfo> {
    val gson = Gson()
    return gson.fromJson(this, Array<CountryInfo>::class.java).asList()
}

fun Context.checkPermission(permission: String): Int {
    if (ContextCompat.checkSelfPermission(
            this, permission
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        return 0
    }
    return -1
}

fun requestPermission(
    context: Activity,
    view: View,
    permission: String,
    message: String,
    granted: (Boolean) -> Unit
) {
    when {
        ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED -> {
            // Permission is granted
            granted(true)
        }
        ActivityCompat.shouldShowRequestPermissionRationale(
            context,
            permission
        ) -> {
            // Additional rationale should be displayed
            Snackbar.make(
                view.findViewById(R.id.container),
                message,
                Snackbar.LENGTH_INDEFINITE
            ).setAction(context.getString(R.string.ok)) {
                granted(false)
            }.show()
        }
        else -> {
            // Permission has not been asked yet
            granted(false)
        }
    }
}

fun Context.showToastMessage(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.showAlertDialog(message: String, title: String, onPositive: () -> Unit) {
    val dialogBuilder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
    dialogBuilder.setMessage(message)
        .setPositiveButton(
            this.getString(R.string.ok)
        ) { _, _ ->
            onPositive()
        }
        .setNegativeButton(
            getString(R.string.cancel)
        ) { dialog, _ ->
            dialog.dismiss()
        }
    val alert = dialogBuilder.create()
    alert.setTitle(title)
    alert.show()
}


fun Context.isAppDefaultDialer(): Boolean {
    /*if (this.checkPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_DENIED) {
        return false
    }
    if (this.checkPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED) {
        return false
    }
    return this.checkPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_DENIED*/
      val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
      return packageName == telecomManager.defaultDialerPackage
}

fun MutableMap<String, Int>.getLastValue(): Int {
    var lastValue = 0
    var count = 1
    for (it in this.entries) {
        if (count == this.size) {
            lastValue = it.value
        }
        count++
    }
    return lastValue
}

fun String.deleteCountryCode(): String {
    val phoneInstance = PhoneNumberUtil.getInstance()
    try {
        return if (this.startsWith("+")) {
            val phoneNumber = phoneInstance.parse(this, null)
            phoneNumber?.nationalNumber?.toString() ?: this
        } else {
            val phoneNumber = phoneInstance.parse(this, "CN")
            phoneNumber?.nationalNumber?.toString() ?: this
        }
    } catch (e: Exception) {

    }
    return this
}
package com.woynex.kimbu.core.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.TELECOM_SERVICE
import android.content.pm.PackageManager
import android.os.Build
import android.telecom.TelecomManager
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
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

@RequiresApi(Build.VERSION_CODES.M)
fun Context.isAppDefaultDialer(): Boolean {
    val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
    return packageName == telecomManager.defaultDialerPackage
}
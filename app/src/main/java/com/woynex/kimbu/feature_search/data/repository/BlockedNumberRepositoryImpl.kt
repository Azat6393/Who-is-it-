package com.woynex.kimbu.feature_search.data.repository

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.BlockedNumberContract.BlockedNumbers
import android.provider.BlockedNumberContract.isBlocked
import androidx.annotation.RequiresApi
import com.woynex.kimbu.feature_search.domain.repository.BlockedNumberRepository
import javax.inject.Inject


class BlockedNumberRepositoryImpl @Inject constructor(
    private val context: Context
) : BlockedNumberRepository {

    @RequiresApi(Build.VERSION_CODES.N)
    override suspend fun blockNumber(number: String) {
        val values = ContentValues()
        values.put(
            BlockedNumbers.COLUMN_ORIGINAL_NUMBER,
            number
        )
        context.contentResolver.insert(
            BlockedNumbers.CONTENT_URI,
            values
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override suspend fun unblockNumber(number: String) {
        val values = ContentValues()
        values.put(
            BlockedNumbers.COLUMN_ORIGINAL_NUMBER,
            number
        )
        val uri = context.contentResolver.insert(
            BlockedNumbers.CONTENT_URI,
            values
        )
        if (uri != null) {
            context.contentResolver.delete(uri, null, null)
        }
    }

    @SuppressLint("Recycle", "Range")
    @RequiresApi(Build.VERSION_CODES.N)
    override suspend fun getBlockedNumbers(): List<String> {
        val c: Cursor = context.contentResolver.query(
            BlockedNumbers.CONTENT_URI, arrayOf(
                BlockedNumbers.COLUMN_ID,
                BlockedNumbers.COLUMN_ORIGINAL_NUMBER,
                BlockedNumbers.COLUMN_E164_NUMBER
            ), null, null, null
        ) ?: return emptyList()
        val blockedList = arrayListOf<String>()
        c.moveToFirst()
        while (c.moveToNext()) {
            val stringNumber =
                c.getString(c.getColumnIndex(BlockedNumbers.COLUMN_ORIGINAL_NUMBER))
            blockedList.add(stringNumber)
        }
        return blockedList
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override suspend fun isBlockedNumber(number: String): Boolean {
        return isBlocked(context, number)
    }
}
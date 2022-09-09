package com.woynex.kimbu.feature_search.data.repository

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.BaseColumns
import android.provider.CallLog
import android.provider.ContactsContract
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.woynex.kimbu.core.data.local.room.KimBuDatabase
import com.woynex.kimbu.feature_search.data.local.room.CallHistoryPagingSource
import com.woynex.kimbu.feature_search.domain.model.Contact
import com.woynex.kimbu.feature_search.domain.model.NumberInfo
import com.woynex.kimbu.feature_search.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class SearchRepositoryImpl @Inject constructor(
    private val database: KimBuDatabase,
    private val context: Context
) : SearchRepository {

    override suspend fun updateCallNumber(callNumber: NumberInfo) {
        return database.callHistoryDao.updateCall(callNumber)
    }

    override suspend fun getLastCallLogs(): Flow<List<NumberInfo>> {
        return database.callHistoryDao.getLastCallLogs()
    }

    override suspend fun getCallsFromDao(): List<NumberInfo> {
        return database.callHistoryDao.getAllCall()
    }

    override suspend fun insertCall(calls: List<NumberInfo>) {
        database.callHistoryDao.insertCalls(calls)
    }

    @SuppressLint("Range")
    override suspend fun updateLogsName() {
        val cursorContacts =
            context.contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null
            )
        cursorContacts?.let {
            cursorContacts.moveToFirst()
            cursorContacts.let {
                while (cursorContacts.moveToNext()) {
                    val id = cursorContacts.getString(
                        cursorContacts.getColumnIndex(ContactsContract.Contacts._ID)
                    )
                    val name = cursorContacts.getString(
                        cursorContacts.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    )
                    val phoneNumber = (cursorContacts.getString(
                        cursorContacts.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                    )).toInt()

                    if (phoneNumber > 0) {
                        val cursorPhone = context.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                            arrayOf(id),
                            null
                        )

                        if (cursorPhone?.count!! > 0) {
                            while (cursorPhone.moveToNext()) {
                                val phoneNumValue = cursorPhone.getString(
                                    cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                ).filter { !it.isWhitespace() }
                                database.callHistoryDao.updateLogs(name, phoneNumValue)
                            }
                        }
                        cursorPhone.close()
                    }
                }
                cursorContacts.close()
            }
        }
    }

    @SuppressLint("Range")
    override fun searchContactByNumber(number: String): String {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(number)
        )
        var name = ""

        val contentResolver: ContentResolver = context.contentResolver
        val contactLookup: Cursor? = contentResolver.query(
            uri, arrayOf(
                BaseColumns._ID,
                ContactsContract.PhoneLookup.DISPLAY_NAME
            ), null, null, null
        )

        contactLookup.use { contactLookup ->
            if (contactLookup != null && contactLookup.count > 0) {
                contactLookup.moveToNext()
                name =
                    contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME))
                //String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
            }
        }
        return name
    }

    override suspend fun getPagedCalls(): Pager<Int, NumberInfo> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20
            )
        ) {
            CallHistoryPagingSource(database.callHistoryDao)
        }
    }

    @SuppressLint("Range")
    override suspend fun getAllContacts(): List<Contact> {
        val cursorContacts =
            context.contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null
            )
        val contacts = mutableSetOf<Contact>()
        cursorContacts?.let {
            cursorContacts.moveToFirst()
            cursorContacts.let {
                while (cursorContacts.moveToNext()) {
                    val id = cursorContacts.getString(
                        cursorContacts.getColumnIndex(ContactsContract.Contacts._ID)
                    )
                    val name = cursorContacts.getString(
                        cursorContacts.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    )
                    val phoneNumber = (cursorContacts.getString(
                        cursorContacts.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                    )).toInt()

                    if (phoneNumber > 0) {
                        val cursorPhone = context.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                            arrayOf(id),
                            null
                        )

                        if (cursorPhone?.count!! > 0) {
                            while (cursorPhone.moveToNext()) {
                                val phoneNumValue = cursorPhone.getString(
                                    cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                ).filter { !it.isWhitespace() }
                                contacts.add(Contact(id = id, number = phoneNumValue, name = name))
                            }
                        }
                        cursorPhone.close()
                    }
                }
                cursorContacts.close()
            }
        }
        return contacts.toList()
    }


    @SuppressLint("Range")
    override suspend fun getCallLogs(): List<NumberInfo> {

        val uriCallLogs = Uri.parse("content://call_log/calls")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val cursorCallLogs =
                context.contentResolver.query(
                    uriCallLogs, null, null, null
                )
            val callLogList = arrayListOf<NumberInfo>()
            cursorCallLogs?.let {
                cursorCallLogs.moveToLast()

                var size = 0

                cursorCallLogs.let {
                    do {
                        val stringId =
                            cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls._ID))
                        val stringNumber =
                            cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.NUMBER))
                        val stringName =
                            cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.CACHED_NAME))
                        val stringDate =
                            cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.DATE))
                        val typeString =
                            cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.TYPE))
                        val countryCodeString =
                            cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.COUNTRY_ISO))

                        callLogList.add(
                            NumberInfo(
                                id = stringId.toInt(),
                                name = if (stringName.isNullOrBlank()) ""
                                else stringName,
                                number = stringNumber,
                                type = typeString,
                                countryCode = if (countryCodeString.isNullOrBlank()) ""
                                else countryCodeString,
                                date = stringDate.toLong(),
                                profilePhoto = "",
                                has_permission = false
                            )
                        )
                        size++
                    } while (cursorCallLogs.moveToPrevious() && size <= 1000)
                    cursorCallLogs.close()
                }
                return callLogList
            }
        }
        return emptyList()
    }
}
package com.woynex.kimbu.feature_search.presentation.contacts

import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.woynex.kimbu.core.data.local.datastore.KimBuPreferencesKey
import com.woynex.kimbu.core.utils.Constants
import com.woynex.kimbu.feature_search.domain.model.Contact
import com.woynex.kimbu.feature_search.domain.use_case.GetContactsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val getContactsUseCase: GetContactsUseCase
) : ViewModel() {

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts = _contacts.asStateFlow()

    fun getAllContacts() = viewModelScope.launch {
        if(contacts.value.isEmpty()){
            println("Empty")
            val contactList = getContactsUseCase().sortedBy { it.name }
            _contacts.value = contactList
        }
    }
}
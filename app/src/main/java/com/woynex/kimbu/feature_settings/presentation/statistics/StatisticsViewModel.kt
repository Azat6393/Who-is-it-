package com.woynex.kimbu.feature_settings.presentation.statistics

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.woynex.kimbu.core.data.local.datastore.KimBuPreferencesKey
import com.woynex.kimbu.core.utils.Constants
import com.woynex.kimbu.core.utils.Resource
import com.woynex.kimbu.feature_auth.domain.model.User
import com.woynex.kimbu.feature_search.domain.model.Statistics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val currentUserId = dataStore.data.map { preferences ->
        preferences[KimBuPreferencesKey.USER_ID_KEY]
    }

    private val _statisticsResponse = MutableStateFlow<Resource<Statistics>>(Resource.Empty())
    val statisticsResponse = _statisticsResponse.asStateFlow()

    init {
        getStatistics()
    }

    private fun getStatistics() = viewModelScope.launch {
        currentUserId.first()?.let { id ->
            _statisticsResponse.value = Resource.Loading<Statistics>()
            val db = Firebase.firestore
            db.collection(Constants.FIREBASE_STATISTICS_COLLECTION)
                .document(id)
                .get()
                .addOnSuccessListener {
                    val statisticsList = it.toObject(Statistics::class.java)
                    if (statisticsList == null) {
                        _statisticsResponse.value =
                            Resource.Error<Statistics>("There isn't statistics")
                    } else {
                        if (statisticsList.searched_id_list?.isEmpty() == true) {
                            _statisticsResponse.value =
                                Resource.Error<Statistics>("There isn't statistics")
                        } else {
                            _statisticsResponse.value = Resource.Success<Statistics>(statisticsList)
                        }
                    }
                }
                .addOnFailureListener {
                    _statisticsResponse.value =
                        Resource.Error<Statistics>(it.localizedMessage ?: "Something went wrong")
                }
        }
    }
}
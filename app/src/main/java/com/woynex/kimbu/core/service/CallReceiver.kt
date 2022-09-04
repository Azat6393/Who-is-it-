package com.woynex.kimbu.core.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.woynex.kimbu.core.utils.isAppDefaultDialer
import com.woynex.kimbu.core.utils.showToastMessage
import com.woynex.kimbu.feature_search.domain.use_case.UpdateCallLogsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CallReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob())
    @Inject
    lateinit var updateCallLogsUseCase: UpdateCallLogsUseCase

    override fun onReceive(p0: Context?, p1: Intent?) {
        p0?.let { context ->
            if (context.isAppDefaultDialer()) {
                p1?.let { intent ->
                    if (intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                            .equals(TelephonyManager.EXTRA_STATE_IDLE)
                    ) {
                        scope.launch {
                            updateCallLogsUseCase()
                        }
                    }
                }
            }
        }
    }
}
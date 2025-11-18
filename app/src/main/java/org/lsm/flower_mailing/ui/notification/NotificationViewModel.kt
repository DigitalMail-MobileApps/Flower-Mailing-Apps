package org.lsm.flower_mailing.ui.notification

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.lsm.flower_mailing.data.local.AppDatabase
import org.lsm.flower_mailing.data.local.NotificationEntity

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).notificationDao()
    val notifications: StateFlow<List<NotificationEntity>> = dao.getAllNotifications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun markAsRead(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.markAsRead(id)
        }
    }

    fun clearAll() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.clearAll()
        }
    }
}
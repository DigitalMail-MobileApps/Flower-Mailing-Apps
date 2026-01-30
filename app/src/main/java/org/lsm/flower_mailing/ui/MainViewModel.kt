package org.lsm.flower_mailing.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.lsm.flower_mailing.remote.RetrofitClient

class MainViewModel : ViewModel() {
    var message by mutableStateOf("Loading...")
        private set
}

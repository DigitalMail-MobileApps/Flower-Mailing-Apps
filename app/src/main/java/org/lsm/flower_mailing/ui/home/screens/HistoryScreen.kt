package org.lsm.flower_mailing.ui.home.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.lsm.flower_mailing.ui.home.HomeViewModel

@Composable
fun HistoryScreen(viewModel: HomeViewModel, onNavigateToLetterDetail: (Int) -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "History Page")
    }
}
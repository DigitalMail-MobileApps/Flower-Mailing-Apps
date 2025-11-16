package org.lsm.flower_mailing.ui.home.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.lsm.flower_mailing.ui.home.HomeViewModel

@Composable
fun DraftScreen(
    viewModel: HomeViewModel
) {
    val letters by viewModel.draftSuratList.collectAsState()
    val isLoading by viewModel.isLoadingSurat.collectAsState()
    val errorMessage by viewModel.errorMessageSurat.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchSurat()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (errorMessage != null) {
            Text(
                text = "Error: $errorMessage",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center).padding(20.dp)
            )
        } else if (letters.isEmpty()) {
            Text(
                text = "Tidak ada surat draft.",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(letters) { letter ->
                    LetterListItem(letter = letter, onClick = {
                        TODO()
                    })
                }
            }
        }
    }
}
package org.lsm.flower_mailing.ui.home.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.lsm.flower_mailing.ui.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    viewModel: HomeViewModel,
    onNavigateToLetterDetail: (Int) -> Unit
) {
    val letters by viewModel.historyList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredLetters = remember(searchQuery, letters) {
        if (searchQuery.isBlank()) letters
        else letters.filter {
            it.judulSurat.contains(searchQuery, ignoreCase = true) ||
                    it.pengirim.contains(searchQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchHistoryLetters()
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            placeholder = { Text("Cari riwayat...", style = TextStyle(fontSize = 12.sp), color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            textStyle = TextStyle(fontSize = 14.sp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.fetchHistoryLetters() },
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (!isLoading && filteredLetters.isEmpty()) {
                    if (errorMessage != null) {
                        CenteredMessage(Icons.Default.ErrorOutline, "Error: $errorMessage")
                    } else {
                        CenteredMessage(Icons.Default.History, "Riwayat kosong.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = filteredLetters, key = { it.id }) { letter ->
                            LetterListItem(
                                letter = letter,
                                onClick = { onNavigateToLetterDetail(letter.id) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
                if (isLoading && letters.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}
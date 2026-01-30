package org.lsm.flower_mailing.ui.home.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.lsm.flower_mailing.ui.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SuratMasukScreen(viewModel: HomeViewModel, onNavigateToLetterDetail: (Int) -> Unit) {
    val letters by viewModel.inboxList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredLetters =
            remember(searchQuery, letters) {
                if (searchQuery.isBlank()) letters
                else
                        letters.filter {
                            it.judulSurat.contains(searchQuery, ignoreCase = true) ||
                                    it.pengirim.contains(searchQuery, ignoreCase = true) ||
                                    it.nomorSurat.contains(searchQuery, ignoreCase = true)
                        }
            }

    LaunchedEffect(Unit) { viewModel.fetchInboxLetters() }

    Column(
            modifier =
                    Modifier.fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Column {
            val userRole by viewModel.userRole.collectAsState()
            val title =
                    if (userRole?.equals("staf_program", ignoreCase = true) == true) "Perlu Balasan"
                    else "Surat Masuk"

            Text(
                    text = title,
                    style =
                            MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                            ),
                    color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                    text = "Daftar surat yang perlu ditindaklanjuti",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                            text = "Cari judul, pengirim...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                    )
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                colors =
                        OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
        )

        Spacer(modifier = Modifier.height(24.dp))

        PullToRefreshBox(
                isRefreshing = isLoading && letters.isNotEmpty(),
                onRefresh = { viewModel.fetchInboxLetters() },
                modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (!isLoading && filteredLetters.isEmpty()) {
                    if (errorMessage != null) {
                        CenteredMessage(
                                icon = Icons.Default.ErrorOutline,
                                message = "Error: $errorMessage"
                        )
                    } else {
                        val msg =
                                if (searchQuery.isNotBlank()) "Tidak ada hasil pencarian."
                                else "Kotak masuk Anda kosong."
                        CenteredMessage(icon = Icons.Default.Inbox, message = msg)
                    }
                } else {
                    LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

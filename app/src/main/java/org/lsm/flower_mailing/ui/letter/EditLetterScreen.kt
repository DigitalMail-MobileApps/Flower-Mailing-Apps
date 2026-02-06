package org.lsm.flower_mailing.ui.letter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLetterScreen(viewModel: EditLetterViewModel = viewModel(), onNavigateBack: () -> Unit) {
        val scrollState = rememberScrollState()

        LaunchedEffect(Unit) { viewModel.navigateBack.collect { if (it) onNavigateBack() } }

        Scaffold(
                topBar = {
                        TopAppBar(
                                title = { Text("Edit Surat") },
                                navigationIcon = {
                                        IconButton(onClick = onNavigateBack) {
                                                Icon(
                                                        Icons.AutoMirrored.Filled.ArrowBack,
                                                        contentDescription = "Back"
                                                )
                                        }
                                }
                        )
                }
        ) { padding ->
                Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                        if (viewModel.isLoading) {
                                CircularProgressIndicator(
                                        modifier =
                                                Modifier.align(androidx.compose.ui.Alignment.Center)
                                )
                        } else {
                                Column(
                                        modifier =
                                                Modifier.fillMaxSize()
                                                        .verticalScroll(scrollState)
                                                        .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                        viewModel.errorMessage?.let {
                                                Text(
                                                        text = it,
                                                        color = MaterialTheme.colorScheme.error
                                                )
                                        }

                                        FormTextField(
                                                label = "Nomor Surat",
                                                value = viewModel.nomorSurat,
                                                onValueChange = { viewModel.nomorSurat = it }
                                        )

                                        FormTextField(
                                                label = "Nomor Agenda",
                                                value = viewModel.nomorAgenda,
                                                onValueChange = { viewModel.nomorAgenda = it },
                                                enabled = false
                                        )

                                        FormTextField(
                                                label = "Judul / Perihal",
                                                value = viewModel.judulSurat,
                                                onValueChange = { viewModel.judulSurat = it }
                                        )

                                        FormTextField(
                                                label = "Pengirim",
                                                value = viewModel.pengirim,
                                                onValueChange = { viewModel.pengirim = it }
                                        )

                                        FormTextField(
                                                label = "Tujuan",
                                                value = viewModel.tujuan,
                                                onValueChange = { viewModel.tujuan = it }
                                        )

                                        FormTextField(
                                                label = "Tanggal Surat (YYYY-MM-DD)",
                                                value = viewModel.tanggalSurat,
                                                onValueChange = { viewModel.tanggalSurat = it }
                                        )

                                        FormTextField(
                                                label = "Tanggal Diterima (YYYY-MM-DD)",
                                                value = viewModel.tanggalMasuk,
                                                onValueChange = { viewModel.tanggalMasuk = it }
                                        )

                                        OutlinedTextField(
                                                value = viewModel.isiSurat,
                                                onValueChange = { viewModel.isiSurat = it },
                                                label = { Text("Isi Ringkas") },
                                                modifier = Modifier.fillMaxWidth().height(120.dp)
                                        )

                                        FormTextField(
                                                label = "Kesimpulan",
                                                value = viewModel.kesimpulan,
                                                onValueChange = { viewModel.kesimpulan = it }
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Button(
                                                onClick = { viewModel.saveChanges() },
                                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                                enabled = !viewModel.isLoading,
                                                shape = RoundedCornerShape(8.dp)
                                        ) {
                                                if (viewModel.isLoading) {
                                                        CircularProgressIndicator(
                                                                modifier = Modifier.size(24.dp),
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onPrimary
                                                        )
                                                } else {
                                                        Text("Simpan Perubahan")
                                                }
                                        }
                                }
                        }
                }
        }
}

@Composable
private fun FormTextField(
        label: String,
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier = Modifier,
        maxLines: Int = 1,
        singleLine: Boolean = true,
        isError: Boolean = false,
        icon: ImageVector? = null,
        enabled: Boolean = true
) {
        OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                maxLines = maxLines,
                singleLine = singleLine,
                isError = isError,
                enabled = enabled,
                leadingIcon =
                        if (icon != null) {
                                { Icon(icon, null, tint = MaterialTheme.colorScheme.outline) }
                        } else null,
                textStyle = MaterialTheme.typography.bodyMedium,
                colors =
                        OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
        )
}

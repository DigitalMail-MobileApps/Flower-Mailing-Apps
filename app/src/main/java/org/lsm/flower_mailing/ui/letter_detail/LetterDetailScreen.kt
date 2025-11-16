package org.lsm.flower_mailing.ui.letter_detail

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// This screen uses the same font-scaling logic as your AddLetterScreen
// Max 14.sp for input, 12.sp for labels.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetterDetailScreen(
    // We get the ViewModel via Hilt/Compose-Navigation's default factory
    // which will correctly handle the SavedStateHandle
    viewModel: LetterDetailViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // --- Collect navigation event ---
    LaunchedEffect(Unit) {
        viewModel.navigateBack.collect {
            if (it) onNavigateBack()
        }
    }

    // --- Date/Time Picker Logic (same as AddLetterScreen) ---
    val context = LocalContext.current
    fun showDateTimePicker(
        initialDateString: String,
        onDateTimeSet: (Long) -> Unit
    ) {
        val calendar = Calendar.getInstance()
        // Try to parse the initial date, if not, use now
        try {
            val localSdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            localSdf.timeZone = TimeZone.getDefault()
            calendar.time = localSdf.parse(initialDateString) ?: Date()
        } catch (e: Exception) { /* Use current time */ }

        val datePickerDialog = DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, day: Int ->
                calendar.set(year, month, day)
                val timePickerDialog = TimePickerDialog(
                    context,
                    { _, hour: Int, minute: Int ->
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        onDateTimeSet(calendar.timeInMillis)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                )
                timePickerDialog.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    // --- Main UI Scaffold ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Surat", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.errorMessage != null) {
                Text(
                    text = "Error: ${uiState.errorMessage}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(20.dp)
                )
            } else {
                // --- Form Content ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // --- Section 1: Informasi Surat ---
                    Text(
                        "Informasi Surat",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    FormTextField(
                        label = "Judul Surat",
                        value = viewModel.judulSurat,
                        onValueChange = { viewModel.judulSurat = it },
                        enabled = uiState.isLetterInfoEditable
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        FormTextField(
                            label = "Pengirim",
                            value = viewModel.pengirim,
                            onValueChange = { viewModel.pengirim = it },
                            enabled = uiState.isLetterInfoEditable,
                            modifier = Modifier.weight(1f)
                        )
                        FormTextField(
                            label = "No. Agenda",
                            value = viewModel.nomorAgenda,
                            onValueChange = { viewModel.nomorAgenda = it },
                            enabled = uiState.isLetterInfoEditable,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    FormTextField(
                        label = "Nomor Surat",
                        value = viewModel.nomorSurat,
                        onValueChange = { viewModel.nomorSurat = it },
                        enabled = uiState.isLetterInfoEditable
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        FormDateField(
                            label = "Tanggal Surat",
                            value = viewModel.tanggalSurat,
                            onClick = {
                                showDateTimePicker(viewModel.tanggalSurat) { millis ->
                                    viewModel.tanggalSurat = viewModel.formatTimestampToDateTime(millis.toString())
                                }
                            },
                            enabled = uiState.isLetterInfoEditable,
                            modifier = Modifier.weight(1f)
                        )
                        FormDateField(
                            label = "Tanggal Masuk",
                            value = viewModel.tanggalMasuk,
                            onClick = {
                                showDateTimePicker(viewModel.tanggalMasuk) { millis ->
                                    viewModel.tanggalMasuk = viewModel.formatTimestampToDateTime(millis.toString())
                                }
                            },
                            enabled = uiState.isLetterInfoEditable,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    FormDropdown(
                        label = "Sifat Surat (Prioritas)",
                        options = viewModel.prioritasOptions,
                        selectedOption = viewModel.prioritas,
                        onOptionSelected = { viewModel.prioritas = it },
                        enabled = uiState.isLetterInfoEditable
                    )
                    FormTextField(
                        label = "Isi / Ringkasan Surat",
                        value = viewModel.isiSurat,
                        onValueChange = { viewModel.isiSurat = it },
                        enabled = uiState.isLetterInfoEditable,
                        maxLines = 3
                    )
                    FormTextField(
                        label = "Kesimpulan (Opsional)",
                        value = viewModel.kesimpulan,
                        onValueChange = { viewModel.kesimpulan = it },
                        enabled = uiState.isLetterInfoEditable,
                        maxLines = 2
                    )

                    // --- Section 2: Informasi Disposisi ---
                    if (uiState.isDispositionSectionVisible) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Informasi Disposisi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        FormTextField(
                            label = "Tujuan Disposisi (Bidang/Bagian)",
                            value = viewModel.bidangTujuan,
                            onValueChange = { viewModel.bidangTujuan = it },
                            enabled = uiState.isDispositionInfoEditable
                        )
                        FormTextField(
                            label = "Catatan Disposisi",
                            value = viewModel.disposisi,
                            onValueChange = { viewModel.disposisi = it },
                            enabled = uiState.isDispositionInfoEditable,
                            maxLines = 3
                        )
                        FormDateField(
                            label = "Tanggal Disposisi",
                            value = viewModel.tanggalDisposisi,
                            onClick = {
                                showDateTimePicker(viewModel.tanggalDisposisi) { millis ->
                                    viewModel.tanggalDisposisi = viewModel.formatTimestampToDateTime(millis.toString())
                                }
                            },
                            enabled = uiState.isDispositionInfoEditable
                        )
                    }

                    // --- Section 3: Buttons ---
                    Spacer(modifier = Modifier.height(16.dp))
                    uiState.buttons.forEach { buttonType ->
                        when (buttonType) {
                            LetterButtonType.SAVE_DRAFT -> {
                                OutlinedButton(
                                    onClick = { viewModel.onSaveDraft() },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
                                ) {
                                    Text("Simpan Draft", fontSize = 14.sp)
                                }
                            }
                            LetterButtonType.SUBMIT_TO_ADC -> {
                                Button(
                                    onClick = { viewModel.onSubmitToAdc() },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Text("Teruskan ke ADC", fontSize = 16.sp)
                                }
                            }
                            LetterButtonType.SUBMIT_DISPOSITION -> {
                                Button(
                                    onClick = { viewModel.onSubmitDisposition() },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Text("Submit Disposisi", fontSize = 16.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}


// --- Reusable Form Components (Copied from AddLetterScreen) ---

@Composable
private fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    isError: Boolean = false,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        maxLines = maxLines,
        isError = isError,
        enabled = enabled,
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormDateField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick) // Only clickable if enabled
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label, fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            trailingIcon = { Icon(Icons.Default.CalendarMonth, "Pilih tanggal") },
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
            readOnly = true,
            enabled = false, // Always false to prevent focus
            colors = OutlinedTextFieldDefaults.colors(
                // Manually set colors to appear enabled or disabled
                disabledTextColor = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                disabledLabelColor = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                disabledBorderColor = if (enabled) MaterialTheme.colorScheme.outline
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.38f),
                disabledTrailingIconColor = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = if (enabled) expanded else false,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOption.replaceFirstChar(Char::titlecase),
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontSize = 12.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
            enabled = false, // Always false to prevent focus
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                disabledLabelColor = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                disabledBorderColor = if (enabled) MaterialTheme.colorScheme.outline
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.38f),
                disabledTrailingIconColor = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            )
        )
        if (enabled) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.replaceFirstChar(Char::titlecase), fontSize = 14.sp) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
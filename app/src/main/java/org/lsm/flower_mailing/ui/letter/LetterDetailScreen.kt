package org.lsm.flower_mailing.ui.letter

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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetterDetailScreen(
    viewModel: LetterDetailViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.navigateBack.collect { if (it) onNavigateBack() }
    }

    // --- Date Picker Logic ---
    fun showDateTimePicker(
        initialDateString: String,
        onDateTimeSet: (Long) -> Unit
    ) {
        val calendar = Calendar.getInstance()
        try {
            val localSdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            localSdf.timeZone = TimeZone.getDefault()
            calendar.time = localSdf.parse(initialDateString) ?: Date()
        } catch (e: Exception) { /* Use current time */ }

        DatePickerDialog(
            context,
            { _, year, month, day ->
                calendar.set(year, month, day)
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        onDateTimeSet(calendar.timeInMillis)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true
                ).show()
            },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Detail Surat", style = MaterialTheme.typography.titleMedium)
                        // Subtitle showing status roughly (optional)
                        if (!uiState.isLoading) {
                            Text(
                                "Lihat & Proses",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
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
            .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.errorMessage != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Terjadi Kesalahan",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.errorMessage ?: "Unknown error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    SectionHeader(title = "Data Utama")
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                FormTextField(
                                    label = "No. Agenda",
                                    value = viewModel.nomorAgenda,
                                    onValueChange = { viewModel.nomorAgenda = it },
                                    enabled = uiState.isLetterInfoEditable,
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Tag
                                )
                                FormTextField(
                                    label = "Nomor Surat",
                                    value = viewModel.nomorSurat,
                                    onValueChange = { viewModel.nomorSurat = it },
                                    enabled = uiState.isLetterInfoEditable,
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Tag
                                )
                            }

                            FormDropdown(
                                label = "Sifat Surat",
                                options = viewModel.prioritasOptions,
                                selectedOption = viewModel.prioritas,
                                onOptionSelected = { viewModel.prioritas = it },
                                enabled = uiState.isLetterInfoEditable
                            )
                        }
                    }

                    SectionHeader(title = "Detil Pengiriman")
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            FormTextField(
                                label = "Pengirim / Tujuan",
                                value = viewModel.pengirim,
                                onValueChange = { viewModel.pengirim = it },
                                enabled = uiState.isLetterInfoEditable,
                                icon = Icons.Default.Person
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                FormDateField(
                                    label = "Tgl. Surat",
                                    value = viewModel.tanggalSurat,
                                    onClick = {
                                        showDateTimePicker(viewModel.tanggalSurat) { millis ->
                                            viewModel.tanggalSurat = viewModel.formatMillisToDateTimeString(millis)
                                        }
                                    },
                                    enabled = uiState.isLetterInfoEditable,
                                    modifier = Modifier.weight(1f)
                                )
                                FormDateField(
                                    label = "Tgl. Diterima",
                                    value = viewModel.tanggalMasuk,
                                    onClick = {
                                        showDateTimePicker(viewModel.tanggalMasuk) { millis ->
                                            viewModel.tanggalMasuk = viewModel.formatMillisToDateTimeString(millis)
                                        }
                                    },
                                    enabled = uiState.isLetterInfoEditable,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    SectionHeader(title = "Isi Surat")
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            FormTextField(
                                label = "Judul / Perihal",
                                value = viewModel.judulSurat,
                                onValueChange = { viewModel.judulSurat = it },
                                enabled = uiState.isLetterInfoEditable,
                                icon = Icons.Default.Description
                            )
                            FormTextField(
                                label = "Ringkasan Isi",
                                value = viewModel.isiSurat,
                                onValueChange = { viewModel.isiSurat = it },
                                enabled = uiState.isLetterInfoEditable,
                                maxLines = 4,
                                singleLine = false
                            )
                            FormTextField(
                                label = "Kesimpulan (Opsional)",
                                value = viewModel.kesimpulan,
                                onValueChange = { viewModel.kesimpulan = it },
                                enabled = uiState.isLetterInfoEditable,
                                maxLines = 2,
                                singleLine = false
                            )
                        }
                    }

                    if (!uiState.downloadUrl.isNullOrBlank()) {
                        SectionHeader(title = "Lampiran")
                        DownloadFileSection(url = uiState.downloadUrl!!)
                    }

                    if (uiState.isDispositionSectionVisible) {
                        SectionHeader(title = "Catatan / Disposisi")
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                FormTextField(
                                    label = "Tujuan Disposisi (Bidang)",
                                    value = viewModel.bidangTujuan,
                                    onValueChange = { viewModel.bidangTujuan = it },
                                    enabled = uiState.isDispositionInfoEditable
                                )
                                FormTextField(
                                    label = "Catatan / Instruksi",
                                    value = viewModel.disposisi,
                                    onValueChange = { viewModel.disposisi = it },
                                    enabled = uiState.isDispositionInfoEditable,
                                    maxLines = 3,
                                    singleLine = false
                                )
                                FormDateField(
                                    label = "Tanggal Proses",
                                    value = viewModel.tanggalDisposisi,
                                    onClick = {
                                        showDateTimePicker(viewModel.tanggalDisposisi) { millis ->
                                            viewModel.tanggalDisposisi = viewModel.formatMillisToDateTimeString(millis)
                                        }
                                    },
                                    enabled = uiState.isDispositionInfoEditable
                                )
                            }
                        }
                    }

                    if (uiState.buttons.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            uiState.buttons.forEach { buttonType ->
                                when (buttonType) {
                                    LetterButtonType.SAVE_DRAFT -> {
                                        OutlinedActionButton("Simpan Perubahan (Draft)") { viewModel.onSaveDraft() }
                                    }
                                    LetterButtonType.SUBMIT_TO_ADC -> {
                                        PrimaryActionButton("Simpan & Teruskan ke ADC") { viewModel.onSubmitToAdc() }
                                    }
                                    LetterButtonType.VERIFY_AND_FORWARD -> {
                                        PrimaryActionButton("Verifikasi & Teruskan ke Direktur") { viewModel.onVerifyAndForward() }
                                    }
                                    LetterButtonType.SUBMIT_DISPOSITION -> {
                                        PrimaryActionButton("Submit Disposisi (Selesai)") { viewModel.onSubmitDisposition() }
                                    }

                                    LetterButtonType.AJUKAN_PERSETUJUAN -> {
                                        PrimaryActionButton("Ajukan Persetujuan") { viewModel.onAjukanPersetujuan() }
                                    }
                                    LetterButtonType.APPROVE_LETTER -> {
                                        ActionButton(
                                            text = "Setujui Surat",
                                            color = Color(0xFF2E7D32), // Green
                                            onClick = { viewModel.onApproveLetter() }
                                        )
                                    }
                                    LetterButtonType.REJECT_REVISION -> {
                                        ActionButton(
                                            text = "Tolak & Minta Revisi",
                                            color = MaterialTheme.colorScheme.error,
                                            onClick = { viewModel.onRejectRevision() }
                                        )
                                    }
                                    LetterButtonType.FINALIZE_SEND -> {
                                        PrimaryActionButton("Kirim / Arsipkan (Selesai)") { viewModel.onFinalizeSend() }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun PrimaryActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun OutlinedActionButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ActionButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
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
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        disabledTextColor = MaterialTheme.colorScheme.onSurface,
        disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        maxLines = maxLines,
        singleLine = singleLine,
        enabled = enabled,
        leadingIcon = if (icon != null) {
            { Icon(icon, null) }
        } else null,
        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
        colors = colors
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val isInteractive = enabled

    ExposedDropdownMenuBox(
        expanded = if (isInteractive) expanded else false,
        onExpandedChange = { if (isInteractive) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption.replaceFirstChar(Char::titlecase),
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label, style = MaterialTheme.typography.bodySmall) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            leadingIcon = { Icon(Icons.Default.Info, null) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = if (enabled) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        )
        if (isInteractive) {
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.replaceFirstChar(Char::titlecase), fontSize = 14.sp) },
                        onClick = { onOptionSelected(option); expanded = false }
                    )
                }
            }
        }
    }
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
    Box(modifier = modifier.clickable(enabled = enabled) { onClick() }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label, style = MaterialTheme.typography.bodySmall) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            readOnly = true,
            enabled = false, // Disable focus
            leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            colors = OutlinedTextFieldDefaults.colors(
                // Custom colors to make it look "enabled" but read-only, or "disabled" visually if not editable
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = if (enabled) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        )
    }
}

@Composable
private fun DownloadFileSection(url: String) {
    val uriHandler = LocalUriHandler.current

    OutlinedCard(
        onClick = {
            try { uriHandler.openUri(url) } catch (e: Exception) { e.printStackTrace() }
        },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Dokumen Lampiran",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Ketuk untuk melihat file",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "Buka File",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
package org.lsm.flower_mailing.ui.letter

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
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
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLetterScreen(
    viewModel: AddLetterViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.onFileSelected(uri)
    }
    fun showDateTimePicker(
        onDateTimeSet: (Long) -> Unit
    ) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, day: Int ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)

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

    // Collect navigation event
    LaunchedEffect(Unit) {
        viewModel.navigateBack.collect {
            if (it) onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Surat Masuk", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ... (All FormTextField, FormDateField, and FormDropdown composables remain the same)

            Text(
                text = "Detail Surat",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            FormTextField(
                label = "Judul Surat",
                value = viewModel.judulSurat,
                onValueChange = { viewModel.judulSurat = it },
                isError = viewModel.judulSurat.isBlank() && viewModel.errorMessage != null
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FormTextField(
                    label = "Pengirim",
                    value = viewModel.pengirim,
                    onValueChange = { viewModel.pengirim = it },
                    isError = viewModel.pengirim.isBlank() && viewModel.errorMessage != null,
                    modifier = Modifier.weight(1f)
                )
                FormTextField(
                    label = "No. Agenda",
                    value = viewModel.nomorAgenda,
                    onValueChange = { viewModel.nomorAgenda = it },
                    modifier = Modifier.weight(1f)
                )
            }

            FormTextField(
                label = "Nomor Surat",
                value = viewModel.nomorSurat,
                onValueChange = { viewModel.nomorSurat = it },
                isError = viewModel.nomorSurat.isBlank() && viewModel.errorMessage != null
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FormDateField(
                    label = "Tanggal Surat",
                    value = viewModel.tanggalSurat.ifBlank { "Pilih Tanggal & Waktu" },
                    onClick = {
                        showDateTimePicker { millis ->
                            viewModel.tanggalSurat = viewModel.formatMillisToDateTimeString(millis)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                FormDateField(
                    label = "Tanggal Masuk",
                    value = viewModel.tanggalMasuk.ifBlank { "Pilih Tanggal & Waktu" },
                    onClick = {
                        showDateTimePicker { millis ->
                            viewModel.tanggalMasuk = viewModel.formatMillisToDateTimeString(millis)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            FormDropdown(
                label = "Sifat Surat (Prioritas)",
                options = viewModel.prioritasOptions,
                selectedOption = viewModel.prioritas,
                onOptionSelected = { viewModel.prioritas = it }
            )

            FormTextField(
                label = "Isi / Ringkasan Surat",
                value = viewModel.isiSurat,
                onValueChange = { viewModel.isiSurat = it },
                maxLines = 3
            )

            FormTextField(
                label = "Kesimpulan (Opsional)",
                value = viewModel.kesimpulan,
                onValueChange = { viewModel.kesimpulan = it },
                maxLines = 2
            )


            Text(
                text = "Lampiran",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            UploadFileSection(
                fileName = viewModel.fileName ?: "Belum ada file dipilih",
                onSelectFile = {
                    filePickerLauncher.launch("*/*")
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            viewModel.errorMessage?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.createLetter(isDraft = true) },
                    enabled = !viewModel.isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
                ) {
                    Text(
                        "Simpan Draft",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Button(
                    onClick = { viewModel.createLetter(isDraft = false) },
                    enabled = !viewModel.isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onSecondary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Buat Surat Masuk", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

// --- (Private composables: FormTextField, FormDateField, FormDropdown, UploadFileSection) ---
// ... (These are all unchanged from your last version) ...

@Composable
private fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        maxLines = maxLines,
        isError = isError,
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormDateField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label, fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Pilih tanggal & waktu"
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
            readOnly = true,
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledSupportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant
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
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOption.replaceFirstChar(Char::titlecase),
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontSize = 12.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp)
        )
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

@Composable
private fun UploadFileSection(
    fileName: String,
    onSelectFile: () -> Unit
) {
    OutlinedButton(
        onClick = onSelectFile,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Default.AttachFile,
                contentDescription = "Attach File",
                tint = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false) // Allow text to shrink
            )
            Text(
                "Pilih File",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
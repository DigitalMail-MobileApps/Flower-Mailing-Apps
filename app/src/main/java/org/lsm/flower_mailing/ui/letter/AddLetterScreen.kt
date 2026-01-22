package org.lsm.flower_mailing.ui.letter

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Calendar
import org.lsm.flower_mailing.data.model.response.VerifierDto
import org.lsm.flower_mailing.ui.components.FilePreviewCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLetterScreen(viewModel: AddLetterViewModel = viewModel(), onNavigateBack: () -> Unit) {
        val context = LocalContext.current
        val isSuratKeluar = viewModel.determinedJenisSurat == "keluar"
        val screenTitle = if (isSuratKeluar) "Registrasi Surat Keluar" else "Registrasi Surat Masuk"
        val partyLabel = if (isSuratKeluar) "Tujuan / Penerima" else "Pengirim Surat"
        val submitButtonText =
                if (isSuratKeluar) "Simpan & Ajukan Verifikasi" else "Kirim ke Direktur"
        val filePickerLauncher =
                rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                ) { uri -> viewModel.onFileSelected(uri) }
        fun showDateTimePicker(onDateTimeSet: (Long) -> Unit) {
                val cal = Calendar.getInstance()
                DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                        cal.set(year, month, day)
                                        TimePickerDialog(
                                                        context,
                                                        { _, hour, minute ->
                                                                cal.set(Calendar.HOUR_OF_DAY, hour)
                                                                cal.set(Calendar.MINUTE, minute)
                                                                onDateTimeSet(cal.timeInMillis)
                                                        },
                                                        cal.get(Calendar.HOUR_OF_DAY),
                                                        cal.get(Calendar.MINUTE),
                                                        true
                                                )
                                                .show()
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                        )
                        .show()
        }

        LaunchedEffect(Unit) { viewModel.navigateBack.collect { if (it) onNavigateBack() } }

        Scaffold(
                topBar = {
                        TopAppBar(
                                title = {
                                        Column {
                                                Text(
                                                        screenTitle,
                                                        style = MaterialTheme.typography.titleMedium
                                                )
                                                Text(
                                                        text =
                                                                viewModel
                                                                        .userRole
                                                                        ?.replace("_", " ")
                                                                        ?.uppercase()
                                                                        ?: "DASHBOARD",
                                                        style =
                                                                MaterialTheme.typography.labelSmall
                                                                        .copy(fontSize = 10.sp),
                                                        color =
                                                                MaterialTheme.colorScheme.onPrimary
                                                                        .copy(alpha = 0.7f)
                                                )
                                        }
                                },
                                navigationIcon = {
                                        IconButton(onClick = onNavigateBack) {
                                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                                        }
                                },
                                colors =
                                        TopAppBarDefaults.topAppBarColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                titleContentColor =
                                                        MaterialTheme.colorScheme.onPrimary,
                                                navigationIconContentColor =
                                                        MaterialTheme.colorScheme.onPrimary
                                        )
                        )
                }
        ) { padding ->
                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(padding)
                                        .verticalScroll(rememberScrollState())
                                        .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                        SectionHeader(title = "Data Utama")
                        Card(
                                colors =
                                        CardDefaults.cardColors(
                                                containerColor =
                                                        MaterialTheme.colorScheme
                                                                .surfaceContainerLow
                                        ),
                                shape = RoundedCornerShape(12.dp),
                        ) {
                                Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                        // Toggle for Staf Lembaga (Can do Masuk & Keluar Internal)
                                        if (viewModel.userRole?.equals(
                                                        "staf_lembaga",
                                                        ignoreCase = true
                                                ) == true
                                        ) {
                                                SingleChoiceSegmentedButtonRow(
                                                        modifier = Modifier.fillMaxWidth()
                                                ) {
                                                        SegmentedButton(
                                                                selected = !isSuratKeluar,
                                                                onClick = {
                                                                        viewModel
                                                                                .determinedJenisSurat =
                                                                                "masuk"
                                                                },
                                                                shape =
                                                                        RoundedCornerShape(
                                                                                topStart = 8.dp,
                                                                                bottomStart = 8.dp
                                                                        ),
                                                                label = { Text("Surat Masuk") }
                                                        )
                                                        SegmentedButton(
                                                                selected = isSuratKeluar,
                                                                shape =
                                                                        RoundedCornerShape(
                                                                                topEnd = 8.dp,
                                                                                bottomEnd = 8.dp
                                                                        ),
                                                                label = { Text("Surat Keluar") },
                                                                onClick = {
                                                                        viewModel
                                                                                .determinedJenisSurat =
                                                                                "keluar"
                                                                        // Staf Lembaga restricted
                                                                        // to Internal for Outgoing
                                                                        if (viewModel.userRole
                                                                                        ?.equals(
                                                                                                "staf_lembaga",
                                                                                                ignoreCase =
                                                                                                        true
                                                                                        ) == true
                                                                        ) {
                                                                                viewModel
                                                                                        .updateScope(
                                                                                                "Internal"
                                                                                        )
                                                                        }
                                                                }
                                                        )
                                                }
                                        }

                                        // Scope Selection REMOVED for Staf Lembaga ("remove it
                                        // completely") - OLD INCORRECT ASSUMPTION
                                        // CORRECT REQ: Start Masuk = Internal + Eksternal. Surat
                                        // Keluar = Internal Only.

                                        // Scope Selection for Staf Lembaga (Only for Surat Masuk)
                                        // Surat Keluar is forced to Internal for Staf Lembaga
                                        // (Hidden)
                                        if (viewModel.userRole?.equals(
                                                        "staf_lembaga",
                                                        ignoreCase = true
                                                ) == true && !isSuratKeluar
                                        ) {
                                                Text(
                                                        text = "Lingkup Surat",
                                                        style =
                                                                MaterialTheme.typography
                                                                        .labelMedium,
                                                        modifier = Modifier.padding(top = 8.dp)
                                                )
                                                SingleChoiceSegmentedButtonRow(
                                                        modifier = Modifier.fillMaxWidth()
                                                ) {
                                                        SegmentedButton(
                                                                selected =
                                                                        viewModel.scope ==
                                                                                "Internal",
                                                                onClick = {
                                                                        viewModel.updateScope(
                                                                                "Internal"
                                                                        )
                                                                },
                                                                shape =
                                                                        RoundedCornerShape(
                                                                                topStart = 8.dp,
                                                                                bottomStart = 8.dp
                                                                        ),
                                                                label = { Text("Internal") }
                                                        )
                                                        SegmentedButton(
                                                                selected =
                                                                        viewModel.scope ==
                                                                                "Eksternal",
                                                                onClick = {
                                                                        viewModel.updateScope(
                                                                                "Eksternal"
                                                                        )
                                                                },
                                                                shape =
                                                                        RoundedCornerShape(
                                                                                topEnd = 8.dp,
                                                                                bottomEnd = 8.dp
                                                                        ),
                                                                label = { Text("Eksternal") }
                                                        )
                                                }
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                FormTextField(
                                                        label = "No. Agenda",
                                                        value = viewModel.nomorAgenda,
                                                        onValueChange = {
                                                                viewModel.nomorAgenda = it
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        icon = Icons.Default.Tag
                                                )
                                                FormTextField(
                                                        label = "No. Surat",
                                                        value = viewModel.nomorSurat,
                                                        onValueChange = {
                                                                viewModel.nomorSurat = it
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        icon = Icons.Default.Tag,
                                                        isError =
                                                                viewModel.nomorSurat.isBlank() &&
                                                                        viewModel.errorMessage !=
                                                                                null
                                                )
                                        }

                                        FormDropdown(
                                                label = "Sifat Surat",
                                                options = viewModel.prioritasOptions,
                                                selectedOption = viewModel.prioritas,
                                                onOptionSelected = { viewModel.prioritas = it }
                                        )

                                        // Only show Verifier Dropdown for Eksternal scope
                                        // Staf Lembaga (Internal) gets auto-assigned manajer_pkl by
                                        // backend
                                        if (isSuratKeluar && viewModel.scope == "Eksternal") {
                                                VerifierDropdown(
                                                        verifiers = viewModel.verifiers,
                                                        selectedId = viewModel.assignedVerifierId,
                                                        onVerifierSelected = { id ->
                                                                android.util.Log.d(
                                                                        "AddLetterScreen",
                                                                        "Verifier selected: $id"
                                                                )
                                                                viewModel.assignedVerifierId = id
                                                                android.util.Log.d(
                                                                        "AddLetterScreen",
                                                                        "ViewModel assignedVerifierId now: ${viewModel.assignedVerifierId}"
                                                                )
                                                        }
                                                )
                                        }
                                }
                        }

                        SectionHeader(title = "Detil Pengiriman")
                        Card(
                                colors =
                                        CardDefaults.cardColors(
                                                containerColor =
                                                        MaterialTheme.colorScheme
                                                                .surfaceContainerLow
                                        ),
                                shape = RoundedCornerShape(12.dp),
                        ) {
                                Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                        FormTextField(
                                                label = partyLabel,
                                                value = viewModel.pengirim,
                                                onValueChange = { viewModel.pengirim = it },
                                                icon = Icons.Default.Person,
                                                isError =
                                                        viewModel.pengirim.isBlank() &&
                                                                viewModel.errorMessage != null
                                        )

                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                FormDateField(
                                                        label = "Tgl. Surat",
                                                        value =
                                                                viewModel.tanggalSurat.ifBlank {
                                                                        "Pilih..."
                                                                },
                                                        onClick = {
                                                                showDateTimePicker { ms ->
                                                                        viewModel.tanggalSurat =
                                                                                viewModel
                                                                                        .formatMillisToDateTimeString(
                                                                                                ms
                                                                                        )
                                                                }
                                                        },
                                                        modifier =
                                                                if (!isSuratKeluar)
                                                                        Modifier.weight(1f)
                                                                else Modifier.fillMaxWidth()
                                                )

                                                if (!isSuratKeluar) {
                                                        FormDateField(
                                                                label = "Tgl. Diterima",
                                                                value =
                                                                        viewModel.tanggalMasuk
                                                                                .ifBlank {
                                                                                        "Pilih..."
                                                                                },
                                                                onClick = {
                                                                        showDateTimePicker { ms ->
                                                                                viewModel
                                                                                        .tanggalMasuk =
                                                                                        viewModel
                                                                                                .formatMillisToDateTimeString(
                                                                                                        ms
                                                                                                )
                                                                        }
                                                                },
                                                                modifier = Modifier.weight(1f)
                                                        )
                                                }
                                        }
                                }
                        }

                        SectionHeader(title = "Isi Surat")
                        Card(
                                colors =
                                        CardDefaults.cardColors(
                                                containerColor =
                                                        MaterialTheme.colorScheme
                                                                .surfaceContainerLow
                                        ),
                                shape = RoundedCornerShape(12.dp),
                        ) {
                                Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                        FormTextField(
                                                label = "Judul / Perihal",
                                                value = viewModel.judulSurat,
                                                onValueChange = { viewModel.judulSurat = it },
                                                icon = Icons.Default.Description,
                                                isError =
                                                        viewModel.judulSurat.isBlank() &&
                                                                viewModel.errorMessage != null
                                        )
                                        FormTextField(
                                                label = "Ringkasan Isi",
                                                value = viewModel.isiSurat,
                                                onValueChange = { viewModel.isiSurat = it },
                                                maxLines = 4,
                                                singleLine = false
                                        )
                                        FormTextField(
                                                label = "Kesimpulan (Opsional)",
                                                value = viewModel.kesimpulan,
                                                onValueChange = { viewModel.kesimpulan = it },
                                                maxLines = 2,
                                                singleLine = false
                                        )
                                }
                        }

                        SectionHeader(title = "Lampiran")
                        if (viewModel.fileName != null) {
                                FilePreviewCard(
                                        fileName = viewModel.fileName!!,
                                        fileType = "File",
                                        fileUrl = viewModel.fileUri,
                                        onRemove = { viewModel.onClearFile() }
                                )
                        } else {
                                UploadFileSection(
                                        fileName = viewModel.fileName,
                                        onSelectFile = { filePickerLauncher.launch("*/*") }
                                )
                        }

                        viewModel.errorMessage?.let {
                                Card(
                                        colors =
                                                CardDefaults.cardColors(
                                                        containerColor =
                                                                MaterialTheme.colorScheme
                                                                        .errorContainer
                                                )
                                ) {
                                        Text(
                                                text = it,
                                                color = MaterialTheme.colorScheme.onErrorContainer,
                                                modifier = Modifier.padding(16.dp),
                                                style = MaterialTheme.typography.bodySmall
                                        )
                                }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(
                                        onClick = { viewModel.createLetter(isDraft = true) },
                                        enabled = !viewModel.isLoading,
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        shape = RoundedCornerShape(8.dp)
                                ) { Text("Simpan Draft") }

                                Button(
                                        onClick = { viewModel.createLetter(isDraft = false) },
                                        enabled = !viewModel.isLoading,
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        containerColor =
                                                                MaterialTheme.colorScheme.primary
                                                )
                                ) {
                                        if (viewModel.isLoading) {
                                                CircularProgressIndicator(
                                                        color = MaterialTheme.colorScheme.onPrimary,
                                                        modifier = Modifier.size(20.dp)
                                                )
                                        } else {
                                                Text(
                                                        submitButtonText,
                                                        textAlign =
                                                                androidx.compose.ui.text.style
                                                                        .TextAlign.Center,
                                                        fontSize = 13.sp,
                                                        lineHeight = 14.sp
                                                )
                                        }
                                }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                }
        }
}

@Composable
private fun SectionHeader(title: String) {
        Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
        )
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
        icon: ImageVector? = null
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormDropdown(
        label: String,
        options: List<String>,
        selectedOption: String,
        onOptionSelected: (String) -> Unit
) {
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                        value = selectedOption.replaceFirstChar(Char::titlecase),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                        trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        leadingIcon = {
                                Icon(
                                        Icons.Default.Info,
                                        null,
                                        tint = MaterialTheme.colorScheme.outline
                                )
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = MaterialTheme.typography.bodyMedium
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        options.forEach { option ->
                                DropdownMenuItem(
                                        text = { Text(option.replaceFirstChar(Char::titlecase)) },
                                        onClick = {
                                                onOptionSelected(option)
                                                expanded = false
                                        }
                                )
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
        modifier: Modifier = Modifier
) {
        Box(modifier = modifier.clickable { onClick() }) {
                OutlinedTextField(
                        value = value,
                        onValueChange = {},
                        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        readOnly = true,
                        enabled = false,
                        leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        colors =
                                OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor =
                                                MaterialTheme.colorScheme.outlineVariant,
                                        disabledLabelColor =
                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledLeadingIconColor = MaterialTheme.colorScheme.primary
                                )
                )
        }
}

@Composable
private fun UploadFileSection(fileName: String?, onSelectFile: () -> Unit) {
        Card(
                onClick = onSelectFile,
                colors =
                        CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
        ) {
                Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                        ) {
                                Icon(
                                        Icons.Default.AttachFile,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                        Text(
                                                "Upload Dokumen Surat",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                                text = fileName
                                                                ?: "Belum ada file dipilih (PDF/Img)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color =
                                                        if (fileName != null)
                                                                MaterialTheme.colorScheme.primary
                                                        else MaterialTheme.colorScheme.outline,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                        )
                                }
                        }
                        Text(
                                "PILIH",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                        )
                }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VerifierDropdown(
        verifiers: List<VerifierDto>,
        selectedId: Int?,
        onVerifierSelected: (Int) -> Unit
) {
        var expanded by remember { mutableStateOf(false) }
        val selectedVerifier = verifiers.find { it.id == selectedId }
        val selectedName = selectedVerifier?.username ?: "Pilih Verifikator..."

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                        value = selectedName,
                        onValueChange = {},
                        readOnly = true,
                        label = {
                                Text(
                                        "Verifikator (Atasan)",
                                        style = MaterialTheme.typography.bodySmall
                                )
                        },
                        trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        leadingIcon = {
                                Icon(
                                        Icons.Default.Person,
                                        null,
                                        tint = MaterialTheme.colorScheme.outline
                                )
                        },
                        modifier =
                                Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                        .fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        isError = selectedId == null
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        if (verifiers.isEmpty()) {
                                DropdownMenuItem(
                                        text = { Text("Tidak ada verifikator tersedia") },
                                        onClick = { expanded = false },
                                        enabled = false
                                )
                        } else {
                                verifiers.forEach { verifier ->
                                        DropdownMenuItem(
                                                text = {
                                                        Column {
                                                                Text(
                                                                        verifier.username,
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .bodyMedium
                                                                )
                                                                if (!verifier.role.isNullOrBlank()
                                                                ) {
                                                                        Text(
                                                                                verifier.role
                                                                                        .replace(
                                                                                                '_',
                                                                                                ' '
                                                                                        )
                                                                                        .uppercase(),
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .labelSmall,
                                                                                color =
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .outline
                                                                        )
                                                                }
                                                        }
                                                },
                                                onClick = {
                                                        onVerifierSelected(verifier.id)
                                                        expanded = false
                                                }
                                        )
                                }
                        }
                }
        }
}

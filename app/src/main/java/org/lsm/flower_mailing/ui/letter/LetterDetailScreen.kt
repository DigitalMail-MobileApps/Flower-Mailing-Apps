package org.lsm.flower_mailing.ui.letter

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import org.lsm.flower_mailing.ui.components.FilePreviewCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetterDetailScreen(
        viewModel: LetterDetailViewModel = viewModel(),
        onNavigateBack: () -> Unit,
        onNavigateToEdit: (Int, String) -> Unit = { _, _ -> },
        onNavigateToReply: (Int, String, String) -> Unit = { _, _, _ -> }
) {
        val uiState by viewModel.uiState.collectAsState()
        val context = LocalContext.current

        LaunchedEffect(Unit) { viewModel.navigateBack.collect { if (it) onNavigateBack() } }
        LaunchedEffect(Unit) {
                viewModel.navigateToReply.collect { (id, title, sender) ->
                        onNavigateToReply(id, title, sender)
                }
        }

        // --- Date Picker Logic ---
        fun showDateTimePicker(initialDateString: String, onDateTimeSet: (Long) -> Unit) {
                val calendar = Calendar.getInstance()
                try {
                        val localSdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                        localSdf.timeZone = TimeZone.getDefault()
                        calendar.time = localSdf.parse(initialDateString) ?: Date()
                } catch (e: Exception) {
                        /* Use current time */
                }

                DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                        calendar.set(year, month, day)
                                        TimePickerDialog(
                                                        context,
                                                        { _, hour, minute ->
                                                                calendar.set(
                                                                        Calendar.HOUR_OF_DAY,
                                                                        hour
                                                                )
                                                                calendar.set(
                                                                        Calendar.MINUTE,
                                                                        minute
                                                                )
                                                                onDateTimeSet(calendar.timeInMillis)
                                                        },
                                                        calendar.get(Calendar.HOUR_OF_DAY),
                                                        calendar.get(Calendar.MINUTE),
                                                        true
                                                )
                                                .show()
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                        )
                        .show()
        }

        Scaffold(
                topBar = {
                        TopAppBar(
                                title = {
                                        Column {
                                                Text(
                                                        "Detail Surat",
                                                        style = MaterialTheme.typography.titleMedium
                                                )
                                                // Subtitle showing status roughly (optional)
                                                if (!uiState.isLoading) {
                                                        Text(
                                                                "Lihat & Proses",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .labelSmall.copy(
                                                                                fontSize = 10.sp
                                                                        ),
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onPrimary.copy(
                                                                                alpha = 0.7f
                                                                        )
                                                        )
                                                }
                                        }
                                },
                                navigationIcon = {
                                        IconButton(onClick = onNavigateBack) {
                                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
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
        ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                        if (uiState.isLoading) {
                                CircularProgressIndicator(
                                        modifier = Modifier.align(Alignment.Center)
                                )
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
                                        modifier =
                                                Modifier.fillMaxSize()
                                                        .verticalScroll(rememberScrollState())
                                                        .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                        // --- STATUS & TAGS ---
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(8.dp)
                                                ) {
                                                        // Status Chip
                                                        StatusChip(status = uiState.status)

                                                        // Scope Chip
                                                        if (viewModel.scope.isNotBlank()) {
                                                                LetterTag(
                                                                        text = viewModel.scope,
                                                                        color =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .tertiaryContainer,
                                                                        onColor =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onTertiaryContainer
                                                                )
                                                        }

                                                        // Priority Chip (Sifat)
                                                        if (viewModel.prioritas.isNotBlank()) {
                                                                val (containerColor, contentColor) =
                                                                        when (viewModel.prioritas
                                                                                        .lowercase()
                                                                        ) {
                                                                                "penting" ->
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .errorContainer to
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .onErrorContainer
                                                                                "segera" ->
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .errorContainer to
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .onErrorContainer
                                                                                else ->
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .secondaryContainer to
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .onSecondaryContainer
                                                                        }
                                                                LetterTag(
                                                                        text =
                                                                                viewModel.prioritas
                                                                                        .replaceFirstChar {
                                                                                                it.uppercase()
                                                                                        },
                                                                        color = containerColor,
                                                                        onColor = contentColor
                                                                )
                                                        }
                                                }
                                        }

                                        SectionHeader(title = "Data Utama")
                                        Card(
                                                colors =
                                                        CardDefaults.cardColors(
                                                                containerColor =
                                                                        MaterialTheme.colorScheme
                                                                                .surfaceContainerLow
                                                        ),
                                                shape = RoundedCornerShape(12.dp),
                                                elevation =
                                                        CardDefaults.cardElevation(
                                                                defaultElevation = 1.dp
                                                        )
                                        ) {
                                                Column(
                                                        modifier = Modifier.padding(16.dp),
                                                        verticalArrangement =
                                                                Arrangement.spacedBy(16.dp)
                                                ) {
                                                        Row(
                                                                horizontalArrangement =
                                                                        Arrangement.spacedBy(12.dp)
                                                        ) {
                                                                FormTextField(
                                                                        label = "No. Agenda",
                                                                        value =
                                                                                viewModel
                                                                                        .nomorAgenda,
                                                                        onValueChange = {
                                                                                viewModel
                                                                                        .nomorAgenda =
                                                                                        it
                                                                        },
                                                                        enabled =
                                                                                uiState.isLetterInfoEditable,
                                                                        modifier =
                                                                                Modifier.weight(1f),
                                                                        icon = Icons.Default.Tag
                                                                )
                                                                FormTextField(
                                                                        label = "Nomor Surat",
                                                                        value =
                                                                                viewModel
                                                                                        .nomorSurat,
                                                                        onValueChange = {
                                                                                viewModel
                                                                                        .nomorSurat =
                                                                                        it
                                                                        },
                                                                        enabled =
                                                                                uiState.isLetterInfoEditable,
                                                                        modifier =
                                                                                Modifier.weight(1f),
                                                                        icon = Icons.Default.Tag
                                                                )
                                                        }

                                                        FormDropdown(
                                                                label = "Sifat Surat",
                                                                options =
                                                                        viewModel.prioritasOptions,
                                                                selectedOption =
                                                                        viewModel.prioritas,
                                                                onOptionSelected = {
                                                                        viewModel.prioritas = it
                                                                },
                                                                enabled =
                                                                        uiState.isLetterInfoEditable
                                                        )
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
                                                elevation =
                                                        CardDefaults.cardElevation(
                                                                defaultElevation = 1.dp
                                                        )
                                        ) {
                                                Column(
                                                        modifier = Modifier.padding(16.dp),
                                                        verticalArrangement =
                                                                Arrangement.spacedBy(16.dp)
                                                ) {
                                                        FormTextField(
                                                                label = "Pengirim / Tujuan",
                                                                value = viewModel.pengirim,
                                                                onValueChange = {
                                                                        viewModel.pengirim = it
                                                                },
                                                                enabled =
                                                                        uiState.isLetterInfoEditable,
                                                                icon = Icons.Default.Person
                                                        )

                                                        Row(
                                                                horizontalArrangement =
                                                                        Arrangement.spacedBy(12.dp)
                                                        ) {
                                                                FormDateField(
                                                                        label = "Tgl. Surat",
                                                                        value =
                                                                                viewModel
                                                                                        .tanggalSurat,
                                                                        onClick = {
                                                                                showDateTimePicker(
                                                                                        viewModel
                                                                                                .tanggalSurat
                                                                                ) { millis ->
                                                                                        viewModel
                                                                                                .tanggalSurat =
                                                                                                viewModel
                                                                                                        .formatMillisToDateTimeString(
                                                                                                                millis
                                                                                                        )
                                                                                }
                                                                        },
                                                                        enabled =
                                                                                uiState.isLetterInfoEditable,
                                                                        modifier =
                                                                                Modifier.weight(1f)
                                                                )
                                                                FormDateField(
                                                                        label = "Tgl. Diterima",
                                                                        value =
                                                                                viewModel
                                                                                        .tanggalMasuk,
                                                                        onClick = {
                                                                                showDateTimePicker(
                                                                                        viewModel
                                                                                                .tanggalMasuk
                                                                                ) { millis ->
                                                                                        viewModel
                                                                                                .tanggalMasuk =
                                                                                                viewModel
                                                                                                        .formatMillisToDateTimeString(
                                                                                                                millis
                                                                                                        )
                                                                                }
                                                                        },
                                                                        enabled =
                                                                                uiState.isLetterInfoEditable,
                                                                        modifier =
                                                                                Modifier.weight(1f)
                                                                )
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
                                                elevation =
                                                        CardDefaults.cardElevation(
                                                                defaultElevation = 1.dp
                                                        )
                                        ) {
                                                Column(
                                                        modifier = Modifier.padding(16.dp),
                                                        verticalArrangement =
                                                                Arrangement.spacedBy(16.dp)
                                                ) {
                                                        FormTextField(
                                                                label = "Judul / Perihal",
                                                                value = viewModel.judulSurat,
                                                                onValueChange = {
                                                                        viewModel.judulSurat = it
                                                                },
                                                                enabled =
                                                                        uiState.isLetterInfoEditable,
                                                                icon = Icons.Default.Description
                                                        )
                                                        FormTextField(
                                                                label = "Ringkasan Isi",
                                                                value = viewModel.isiSurat,
                                                                onValueChange = {
                                                                        viewModel.isiSurat = it
                                                                },
                                                                enabled =
                                                                        uiState.isLetterInfoEditable,
                                                                maxLines = 4,
                                                                singleLine = false
                                                        )
                                                        FormTextField(
                                                                label = "Kesimpulan (Opsional)",
                                                                value = viewModel.kesimpulan,
                                                                onValueChange = {
                                                                        viewModel.kesimpulan = it
                                                                },
                                                                enabled =
                                                                        uiState.isLetterInfoEditable,
                                                                maxLines = 2,
                                                                singleLine = false
                                                        )
                                                }
                                        }

                                        if (!uiState.downloadUrl.isNullOrBlank()) {
                                                SectionHeader(title = "Lampiran")
                                                val uriHandler = LocalUriHandler.current
                                                FilePreviewCard(
                                                        fileName = "Dokumen Lampiran",
                                                        fileType = "Tampilkan",
                                                        fileUrl = uiState.downloadUrl,
                                                        onClick = {
                                                                try {
                                                                        uriHandler.openUri(
                                                                                uiState.downloadUrl!!
                                                                        )
                                                                } catch (e: Exception) {
                                                                        e.printStackTrace()
                                                                }
                                                        }
                                                )
                                        }

                                        if (uiState.isDispositionSectionVisible) {
                                                SectionHeader(title = "Catatan / Disposisi")
                                                Card(
                                                        colors =
                                                                CardDefaults.cardColors(
                                                                        containerColor =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .secondaryContainer
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.1f
                                                                                        )
                                                                ),
                                                        shape = RoundedCornerShape(12.dp),
                                                        border =
                                                                BorderStroke(
                                                                        1.dp,
                                                                        MaterialTheme.colorScheme
                                                                                .secondary.copy(
                                                                                alpha = 0.2f
                                                                        )
                                                                )
                                                ) {
                                                        Column(
                                                                modifier = Modifier.padding(16.dp),
                                                                verticalArrangement =
                                                                        Arrangement.spacedBy(16.dp)
                                                        ) {
                                                                FormTextField(
                                                                        label =
                                                                                "Tujuan Disposisi (Bidang)",
                                                                        value =
                                                                                viewModel
                                                                                        .bidangTujuan,
                                                                        onValueChange = {
                                                                                viewModel
                                                                                        .bidangTujuan =
                                                                                        it
                                                                        },
                                                                        enabled =
                                                                                uiState.isDispositionInfoEditable
                                                                )
                                                                FormTextField(
                                                                        label =
                                                                                "Catatan / Instruksi",
                                                                        value = viewModel.disposisi,
                                                                        onValueChange = {
                                                                                viewModel
                                                                                        .disposisi =
                                                                                        it
                                                                        },
                                                                        enabled =
                                                                                uiState.isDispositionInfoEditable,
                                                                        maxLines = 3,
                                                                        singleLine = false
                                                                )
                                                                FormDateField(
                                                                        label = "Tanggal Proses",
                                                                        value =
                                                                                viewModel
                                                                                        .tanggalDisposisi,
                                                                        onClick = {
                                                                                showDateTimePicker(
                                                                                        viewModel
                                                                                                .tanggalDisposisi
                                                                                ) { millis ->
                                                                                        viewModel
                                                                                                .tanggalDisposisi =
                                                                                                viewModel
                                                                                                        .formatMillisToDateTimeString(
                                                                                                                millis
                                                                                                        )
                                                                                }
                                                                        },
                                                                        enabled =
                                                                                uiState.isDispositionInfoEditable
                                                                )

                                                                // Perlu Balasan Checkbox
                                                                if (uiState.isDispositionInfoEditable
                                                                ) {
                                                                        Row(
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth(),
                                                                                verticalAlignment =
                                                                                        Alignment
                                                                                                .CenterVertically
                                                                        ) {
                                                                                Checkbox(
                                                                                        checked =
                                                                                                viewModel
                                                                                                        .needsReply,
                                                                                        onCheckedChange = {
                                                                                                viewModel
                                                                                                        .needsReply =
                                                                                                        it
                                                                                        }
                                                                                )
                                                                                Text(
                                                                                        text =
                                                                                                "Surat ini perlu balasan",
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .bodyMedium,
                                                                                        modifier =
                                                                                                Modifier
                                                                                                        .clickable {
                                                                                                                viewModel
                                                                                                                        .needsReply =
                                                                                                                        !viewModel
                                                                                                                                .needsReply
                                                                                                        }
                                                                                )
                                                                        }
                                                                }
                                                        }
                                                }
                                        }

                                        if (uiState.buttons.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(8.dp))

                                                var showDeleteDialog by remember {
                                                        mutableStateOf(false)
                                                }

                                                if (showDeleteDialog) {
                                                        AlertDialog(
                                                                onDismissRequest = {
                                                                        showDeleteDialog = false
                                                                },
                                                                title = { Text("Hapus Surat") },
                                                                text = {
                                                                        Text(
                                                                                "Apakah Anda yakin ingin menghapus surat ini? Tindakan ini tidak dapat dibatalkan."
                                                                        )
                                                                },
                                                                confirmButton = {
                                                                        TextButton(
                                                                                onClick = {
                                                                                        showDeleteDialog =
                                                                                                false
                                                                                        viewModel
                                                                                                .deleteLetter()
                                                                                }
                                                                        ) {
                                                                                Text(
                                                                                        "Hapus",
                                                                                        color =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .error
                                                                                )
                                                                        }
                                                                },
                                                                dismissButton = {
                                                                        TextButton(
                                                                                onClick = {
                                                                                        showDeleteDialog =
                                                                                                false
                                                                                }
                                                                        ) { Text("Batal") }
                                                                }
                                                        )
                                                }

                                                Column(
                                                        verticalArrangement =
                                                                Arrangement.spacedBy(12.dp)
                                                ) {
                                                        uiState.buttons.forEach { buttonType ->
                                                                when (buttonType) {
                                                                        LetterButtonType
                                                                                .SAVE_DRAFT -> {
                                                                                OutlinedActionButton(
                                                                                        "Simpan Perubahan (Draft)"
                                                                                ) {
                                                                                        viewModel
                                                                                                .onSaveDraft()
                                                                                }
                                                                        }
                                                                        LetterButtonType
                                                                                .SUBMIT_LETTER -> {
                                                                                PrimaryActionButton(
                                                                                        "Kirim ke Direktur"
                                                                                ) {
                                                                                        viewModel
                                                                                                .onSubmitLetter()
                                                                                }
                                                                        }
                                                                        LetterButtonType.EDIT -> {
                                                                                OutlinedActionButton(
                                                                                        "Edit Detail"
                                                                                ) {
                                                                                        val id =
                                                                                                viewModel
                                                                                                        .letterId
                                                                                                        .toIntOrNull()
                                                                                                        ?: 0
                                                                                        val type =
                                                                                                if (uiState.isDispositionSectionVisible
                                                                                                )
                                                                                                        "masuk"
                                                                                                else
                                                                                                        "keluar"
                                                                                        onNavigateToEdit(
                                                                                                id,
                                                                                                type
                                                                                        )
                                                                                }
                                                                        }
                                                                        LetterButtonType.DELETE -> {
                                                                                ActionButton(
                                                                                        text =
                                                                                                "Hapus Surat",
                                                                                        color =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .error,
                                                                                        onClick = {
                                                                                                showDeleteDialog =
                                                                                                        true
                                                                                        }
                                                                                )
                                                                        }
                                                                        LetterButtonType
                                                                                .SUBMIT_TO_STAF_PROGRAM -> {
                                                                                PrimaryActionButton(
                                                                                        "Simpan & Teruskan ke Staf Program"
                                                                                ) {
                                                                                        viewModel
                                                                                                .onSubmitToStafProgram()
                                                                                }
                                                                        }
                                                                        LetterButtonType
                                                                                .VERIFY_AND_FORWARD -> {
                                                                                PrimaryActionButton(
                                                                                        "Verifikasi & Teruskan ke Direktur"
                                                                                ) {
                                                                                        viewModel
                                                                                                .onVerifyAndForward()
                                                                                }
                                                                        }
                                                                        LetterButtonType
                                                                                .SUBMIT_DISPOSITION -> {
                                                                                PrimaryActionButton(
                                                                                        "Submit Disposisi (Selesai)"
                                                                                ) {
                                                                                        viewModel
                                                                                                .onSubmitDisposition()
                                                                                }
                                                                        }
                                                                        LetterButtonType
                                                                                .AJUKAN_PERSETUJUAN -> {
                                                                                PrimaryActionButton(
                                                                                        "Ajukan Persetujuan"
                                                                                ) {
                                                                                        viewModel
                                                                                                .onAjukanPersetujuan()
                                                                                }
                                                                        }
                                                                        LetterButtonType
                                                                                .APPROVE_LETTER -> {
                                                                                ActionButton(
                                                                                        text =
                                                                                                "Setujui Surat",
                                                                                        color =
                                                                                                Color(
                                                                                                        0xFF2E7D32
                                                                                                ), // Green
                                                                                        onClick = {
                                                                                                viewModel
                                                                                                        .verifyLetterApprove()
                                                                                        }
                                                                                )
                                                                        }
                                                                        LetterButtonType
                                                                                .REJECT_REVISION -> {
                                                                                ActionButton(
                                                                                        text =
                                                                                                "Tolak & Minta Revisi",
                                                                                        color =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .error,
                                                                                        onClick = {
                                                                                                viewModel
                                                                                                        .onRejectRevision()
                                                                                        }
                                                                                )
                                                                        }
                                                                        LetterButtonType
                                                                                .FINALIZE_SEND -> {
                                                                                PrimaryActionButton(
                                                                                        "Kirim / Arsipkan (Selesai)"
                                                                                ) {
                                                                                        viewModel
                                                                                                .onFinalizeSend()
                                                                                }
                                                                        }
                                                                        LetterButtonType.REPLY -> {
                                                                                PrimaryActionButton(
                                                                                        "Balas Surat"
                                                                                ) {
                                                                                        viewModel
                                                                                                .onReply()
                                                                                }
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
                colors =
                        ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                        )
        ) { Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun OutlinedActionButton(text: String, onClick: () -> Unit) {
        OutlinedButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) { Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun ActionButton(text: String, color: Color, onClick: () -> Unit) {
        Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = color)
        ) { Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White) }
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
        val colors =
                OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        disabledLeadingIconColor =
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
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
                leadingIcon =
                        if (icon != null) {
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
                        trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        leadingIcon = { Icon(Icons.Default.Info, null) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        colors =
                                OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor =
                                                if (enabled)
                                                        MaterialTheme.colorScheme.outlineVariant
                                                else
                                                        MaterialTheme.colorScheme.outline.copy(
                                                                alpha = 0.2f
                                                        ),
                                        disabledLabelColor =
                                                MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.6f
                                                ),
                                        disabledLeadingIconColor =
                                                MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.4f
                                                ),
                                        disabledTrailingIconColor =
                                                MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.4f
                                                )
                                )
                )
                if (isInteractive) {
                        ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                        ) {
                                options.forEach { option ->
                                        DropdownMenuItem(
                                                text = {
                                                        Text(
                                                                option.replaceFirstChar(
                                                                        Char::titlecase
                                                                ),
                                                                fontSize = 14.sp
                                                        )
                                                },
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
                        colors =
                                OutlinedTextFieldDefaults.colors(
                                        // Custom colors to make it look "enabled" but read-only, or
                                        // "disabled" visually if not editable
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor =
                                                if (enabled)
                                                        MaterialTheme.colorScheme.outlineVariant
                                                else
                                                        MaterialTheme.colorScheme.outline.copy(
                                                                alpha = 0.2f
                                                        ),
                                        disabledLabelColor =
                                                MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.6f
                                                ),
                                        disabledLeadingIconColor =
                                                MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.4f
                                                )
                                )
                )
        }
}

@Composable
private fun StatusChip(status: String) {
        val (color, text) =
                when (status.lowercase()) {
                        "draft" -> Color(0xFFE0E0E0) to "Draft" // Grey
                        "perlu_verifikasi" -> Color(0xFFFFF9C4) to "Perlu Verifikasi" // Yellow
                        "perlu_persetujuan" -> Color(0xFFFFCC80) to "Perlu Persetujuan" // Orange
                        "perlu_revisi" -> Color(0xFFFFCDD2) to "Perlu Revisi" // Red
                        "disetujui" -> Color(0xFFC8E6C9) to "Disetujui" // Green
                        "belum_disposisi" -> Color(0xFFB3E5FC) to "Belum Disposisi" // Blue
                        "sudah_disposisi" -> Color(0xFFC8E6C9) to "Sudah Disposisi" // Green
                        "diarsipkan" -> Color(0xFFCFD8DC) to "Diarsipkan" // Blue Grey
                        else -> MaterialTheme.colorScheme.surfaceVariant to status
                }

        Surface(
                color = color,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(28.dp)
        ) {
                Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                        Text(
                                text = text,
                                style =
                                        MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold
                                        ),
                                color = Color.Black.copy(alpha = 0.8f) // Ensure readability
                        )
                }
        }
}

@Composable
private fun LetterTag(text: String, color: Color, onColor: Color) {
        Surface(
                color = color,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(28.dp)
        ) {
                Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                        Text(
                                text = text,
                                style =
                                        MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold
                                        ),
                                color = onColor
                        )
                }
        }
}

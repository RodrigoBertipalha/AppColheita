package com.colheitadecampo.ui.screens.importexport

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.colheitadecampo.R
import com.colheitadecampo.domain.ImportarArquivoUseCase
import com.colheitadecampo.ui.components.AppScaffold
import com.colheitadecampo.ui.components.ConfirmationDialog
import com.colheitadecampo.ui.components.EmptyState
import com.colheitadecampo.ui.components.ErrorDialog
import com.colheitadecampo.ui.components.InfoCard
import com.colheitadecampo.ui.components.LoadingIndicator
import com.colheitadecampo.ui.components.SectionTitle
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@Composable
fun ImportExportScreen(
    navigateUp: () -> Unit,
    viewModel: ImportExportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedTabIndex by remember { mutableStateOf(0) }
    
    val fileSelectorLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.onFileSelected(it) }
    }
    
    LaunchedEffect(state.isImportSuccess) {
        if (state.isImportSuccess) {
            snackbarHostState.showSnackbar(message = "Arquivo importado com sucesso")
            viewModel.resetImportSuccess()
        }
    }

    LaunchedEffect(state.isExportSuccess) {
        if (state.isExportSuccess) {
            snackbarHostState.showSnackbar(message = "Arquivo exportado com sucesso")
            viewModel.resetExportSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        AppScaffold(
            title = stringResource(R.string.import_export_title),
            navigationIcon = {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Tabs
                TabRow(selectedTabIndex = selectedTabIndex) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text(text = "Importação") }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text(text = "Exportação") }
                    )
                }
                
                when {
                    state.isLoading -> LoadingIndicator()
                    state.error != null -> {
                        ErrorDialog(
                            title = "Erro",
                            message = state.error ?: "Erro desconhecido",
                            onDismiss = { viewModel.resetError() }
                        )
                        EmptyState(message = "Ocorreu um erro. Tente novamente.")
                    }
                    else -> {
                        when (selectedTabIndex) {
                            0 -> ImportTab(
                                state = state,
                                onFileSelect = { fileSelectorLauncher.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) },
                                onImportStrategySelected = viewModel::onImportStrategySelected,
                                onImportConfirm = viewModel::importFile
                            )
                            1 -> ExportTab(
                                state = state,
                                onExportConfirm = viewModel::exportFile,
                                onShareExportedFile = viewModel::shareExportedFile
                            )
                        }
                    }
                }
            }
        }
    }

    // Show error dialog if there's an error
    if (state.error != null) {
        ErrorDialog(
            title = "Erro",
            message = state.error ?: "Erro desconhecido",
            onDismiss = { viewModel.resetError() }
        )
    }
}

@Composable
fun ImportTab(
    state: ImportExportState,
    onFileSelect: () -> Unit,
    onImportStrategySelected: (ImportarArquivoUseCase.ImportStrategy) -> Unit,
    onImportConfirm: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // File Selection
            InfoCard(title = stringResource(R.string.select_file)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onFileSelect,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.select_file))
                    }

                    if (state.selectedFileUri != null) {
                        Text(
                            text = "Arquivo: ${state.selectedFileUri.lastPathSegment}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        // Preview of the file (if selected)
        if (state.filePreview.isNotEmpty()) {
            item {
                InfoCard(title = stringResource(R.string.file_preview)) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Headers
                        val headers = state.filePreview.firstOrNull()?.keys ?: emptyList()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            headers.take(3).forEach { header ->
                                Text(
                                    text = header,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Data rows (limited preview)
                        state.filePreview.take(5).forEach { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                headers.take(3).forEach { header ->
                                    Text(
                                        text = row[header] ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            Divider()
                        }

                        if (state.filePreview.size > 5) {
                            Text(
                                text = "... e mais ${state.filePreview.size - 5} linhas",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.align(Alignment.End).padding(8.dp)
                            )
                        }
                    }
                }
            }
            
            // Import Strategy
            item {
                InfoCard(title = stringResource(R.string.import_strategy)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectableGroup()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = state.selectedImportStrategy is ImportarArquivoUseCase.ImportStrategy.Replace,
                                    onClick = { onImportStrategySelected(ImportarArquivoUseCase.ImportStrategy.Replace) }
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.selectedImportStrategy is ImportarArquivoUseCase.ImportStrategy.Replace,
                                onClick = null
                            )
                            Text(
                                text = stringResource(R.string.replace),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = state.selectedImportStrategy is ImportarArquivoUseCase.ImportStrategy.Merge,
                                    onClick = { onImportStrategySelected(ImportarArquivoUseCase.ImportStrategy.Merge) }
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.selectedImportStrategy is ImportarArquivoUseCase.ImportStrategy.Merge,
                                onClick = null
                            )
                            Text(
                                text = stringResource(R.string.merge),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
            
            // Import Button
            item {
                Button(
                    onClick = onImportConfirm,
                    enabled = state.selectedFileUri != null && state.selectedImportStrategy != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.import_btn))
                }
            }
        }
        
        // Recent Imports
        if (state.recentImportSessions.isNotEmpty()) {
            item {
                SectionTitle(title = stringResource(R.string.import_history))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Últimas importações",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        state.recentImportSessions.forEach { session ->
                            val fileName = Uri.parse(session.path).lastPathSegment ?: "Desconhecido"
                            val dateTime = session.dataHora.format(
                                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                            )
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = fileName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "$dateTime - ${session.linhasProcessadas} registros",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExportTab(
    state: ImportExportState,
    onExportConfirm: () -> Unit,
    onShareExportedFile: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (state.currentField == null) {
            EmptyState(message = stringResource(R.string.error_no_field_imported))
        } else {
            InfoCard(title = stringResource(R.string.export_title)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Campo: ${state.currentField.nomeCampo}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.export_btn))
                    }
                    
                    if (state.exportedFileUri != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Arquivo exportado com sucesso!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = onShareExportedFile,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.send_email))
                        }
                    }
                }
            }
        }
    }
    
    if (showConfirmDialog) {
        ConfirmationDialog(
            title = "Confirmar exportação",
            message = "Deseja exportar os dados do campo ${state.currentField?.nomeCampo} em um novo arquivo Excel?",
            onConfirm = {
                onExportConfirm()
                showConfirmDialog = false
            },
            onDismiss = { showConfirmDialog = false }
        )
    }
}

package com.colheitadecampo.ui.screens.harvest

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.colheitadecampo.R
import com.colheitadecampo.data.model.Plot
import com.colheitadecampo.ui.components.AppScaffold
import com.colheitadecampo.ui.components.LoadingIndicator
import timber.log.Timber

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HarvestScreen(
    fieldId: Long,
    navigateUp: () -> Unit,
    viewModel: HarvestViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    
    // Show snackbar for success messages
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(message = it)
            viewModel.resetSuccessMessage()
        }
    }

    // Show snackbar for errors
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(message = it)
            viewModel.resetErrorMessage()
        }
    }
    
    // Auto-focus on RECID input field - com segurança adicional
    LaunchedEffect(state.field) {
        try {
            // Atrasa um pouco para garantir que o componente esteja pronto
            kotlinx.coroutines.delay(300)
            focusRequester.requestFocus()
        } catch (e: Exception) {
            // Ignora erros de foco, não são críticos para a funcionalidade
            Timber.e(e, "Erro ao solicitar foco")
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { outerPadding ->
        AppScaffold(
            title = stringResource(R.string.harvest_title),
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
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 64.dp) // Aumentamos significativamente o padding inferior
                    .verticalScroll(rememberScrollState()) // Adiciona scroll vertical
            ) {
                when {
                    state.isLoading -> LoadingIndicator()
                    state.field == null -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.error_no_field_imported),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    else -> {
                        // Nome do campo e data
                        state.field?.let { field ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = field.nomeCampo,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Text(
                                        text = "Importado: ${field.dataArquivo.toLocalDate()}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Harvest Progress
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Calcular porcentagem de colheita
                                val porcentagemColheita = if (state.totalPlots > 0) {
                                    (state.harvestedPlots.toFloat() / state.totalPlots.toFloat() * 100).toInt()
                                } else {
                                    0
                                }
                                
                                Text(
                                    text = "$porcentagemColheita% Colhido",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = stringResource(
                                        R.string.harvested_count,
                                        state.harvestedPlots,
                                        state.totalPlots
                                    ),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = stringResource(R.string.remaining, state.remainingPlots),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // RECID Input with Scanner Icon
                        OutlinedTextField(
                            value = state.recidInput,
                            onValueChange = viewModel::updateRecidInput,
                            label = { Text(stringResource(R.string.recid_input)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            leadingIcon = { 
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null
                                ) 
                            },
                            trailingIcon = {
                                if (state.recidInput.isNotBlank()) {
                                    IconButton(onClick = { viewModel.updateRecidInput("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear"
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done,
                                keyboardType = KeyboardType.Text
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    if (state.recidInput.isNotBlank()) {
                                        viewModel.marcarColhidoPorRecid()
                                    }
                                }
                            )
                        )
                        
                        // Scanner waiting indicator (animation)
                        AnimatedVisibility(
                            visible = state.recidInput.isBlank(),
                            enter = fadeIn(animationSpec = tween(500)),
                            exit = fadeOut(animationSpec = tween(500))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = stringResource(R.string.awaiting_scanner),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Action Buttons
                        Button(
                            onClick = { viewModel.marcarColhidoPorRecid() },
                            enabled = state.recidInput.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.harvest_by_plot))
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { 
                                if (state.availableGrupos.isNotEmpty()) {
                                    // Se tiver RECID, tenta determinar o grupo a partir dele
                                    if (state.recidInput.isNotBlank()) {
                                        viewModel.determineGroupFromRecid(state.recidInput)
                                    } else {
                                        // Se não tiver RECID, mostra diálogo para escolher grupo
                                        viewModel.prepareGroupSelection()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.harvest_by_group))
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedButton(
                            onClick = { viewModel.desfazerColheita() },
                            enabled = state.harvestedPlots > 0,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.undo_harvest))
                        }
                        
                        // Exibir último plot colhido
                        state.lastColhidoPlot?.let { lastPlot ->
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 32.dp), // Adiciona padding inferior para evitar corte
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Último Plot Colhido:",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Row {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Plot: ${lastPlot.plot}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "RECID: ${lastPlot.recid}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                        
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "Grupo: ${lastPlot.grupoId}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = "Range: ${lastPlot.range}, Row: ${lastPlot.row}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // Spacer adicional no final da coluna para garantir que tudo fique visível
                Spacer(modifier = Modifier.height(40.dp))
            }
            
            // Group Selector Dialog
            if (state.showGroupSelector) {
                GroupSelectorDialog(
                    grupos = state.availableGruposForSelection,
                    onGroupSelected = viewModel::selectGroupFromList,
                    onDismiss = viewModel::hideGroupSelector
                )
            }
            
            // Group Harvest Dialog
            if (state.showGroupDialog && state.selectedGrupoId != null) {
                GroupHarvestDialog(
                    grupoId = state.selectedGrupoId!!,
                    plots = state.groupPlots,
                    selectedPlots = state.selectedPlots,
                    onTogglePlotSelection = viewModel::togglePlotSelection,
                    onConfirm = viewModel::marcarColhidoPorGrupo,
                    onSelectAll = viewModel::marcarColhidoPorGrupo,
                    onDismiss = viewModel::hideGroupDialog
                )
            }
        }
    }
}

@Composable
fun GroupHarvestDialog(
    grupoId: String,
    plots: List<Plot>,
    selectedPlots: Set<String>,
    onTogglePlotSelection: (String) -> Unit,
    onConfirm: () -> Unit,
    onSelectAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val nonHarvestedPlots = plots.filter { !it.colhido }
    val harvestedPlots = plots.filter { it.colhido }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Grupo: $grupoId",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Colhidos: ${harvestedPlots.size} / ${plots.size}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (nonHarvestedPlots.isEmpty()) {
                    Text(
                        text = "Todos os plots deste grupo já foram colhidos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = stringResource(R.string.select_plots_to_harvest),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // List of non-harvested plots
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(nonHarvestedPlots) { plot ->
                            PlotSelectionItem(
                                plot = plot,
                                isSelected = selectedPlots.contains(plot.recid),
                                onToggleSelection = { onTogglePlotSelection(plot.recid) }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(R.string.cancel))
                    }
                    
                    if (nonHarvestedPlots.isNotEmpty()) {
                        TextButton(onClick = onSelectAll) {
                            Text(text = "Marcar Todos")
                        }
                        
                        Button(
                            onClick = onConfirm,
                            enabled = selectedPlots.isNotEmpty()
                        ) {
                            Text(text = stringResource(R.string.confirm))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlotSelectionItem(
    plot: Plot,
    isSelected: Boolean,
    onToggleSelection: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleSelection)
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggleSelection() }
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Layout melhorado conforme solicitado
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Plot: ${plot.plot}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "RECID: ${plot.recid}",
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        // Grupo e localização à direita
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "Grupo: ${plot.grupoId}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Range: ${plot.range}, Row: ${plot.row}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

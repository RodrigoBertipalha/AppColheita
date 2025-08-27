package com.colheitadecampo.ui.screens.dashboard

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.colheitadecampo.R
import com.colheitadecampo.data.model.Plot
import com.colheitadecampo.data.model.GroupStats
import com.colheitadecampo.ui.components.AppScaffold
import com.colheitadecampo.ui.utils.pagingItems
import com.colheitadecampo.ui.components.InfoCard
import com.colheitadecampo.ui.components.LoadingIndicator
import com.colheitadecampo.ui.components.SectionTitle

@Composable
fun DashboardScreen(
    fieldId: Long,
    navigateUp: () -> Unit,
    navigateToHarvest: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedGrupoId by viewModel.selectedGrupoId.collectAsState()
    val pagedPlots = viewModel.pagedPlots.collectAsLazyPagingItems()

    AppScaffold(
        title = stringResource(R.string.dashboard_title),
        navigationIcon = {
            IconButton(onClick = navigateUp) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        actions = {
            IconButton(onClick = navigateToHarvest) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Ir para colheita",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) {
        when {
            state.isLoading -> LoadingIndicator()
            state.field == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Campo não encontrado",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Field Summary
                    InfoCard(title = state.field?.nomeCampo ?: "") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Total de plots
                            Text(
                                text = stringResource(R.string.total_plots, state.totalPlots),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Total elegíveis (total - descartados)
                            val eligiblePlots = state.totalPlots - state.discardedPlots
                            Text(
                                text = "Plots elegíveis: $eligiblePlots",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Total descartados
                            Text(
                                text = "Plots descartados: ${state.discardedPlots}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colorResource(id = R.color.discarded)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Percentual de colheita
                            Text(
                                text = stringResource(R.string.harvested_percentage, state.percentageHarvested),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Barra de progresso
                            LinearProgressIndicator(
                                progress = state.percentageHarvested / 100f,
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = viewModel::updateSearchQuery,
                        placeholder = { Text(text = stringResource(R.string.search_by_recid)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // Removida a seção de filtro por grupo para simplificar a UI
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Group Distribution
                    if (state.groupStats.isNotEmpty()) {
                        SectionTitle(title = stringResource(R.string.group_distribution))
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.groupStats) { groupStat ->
                                GroupStatCard(groupStat = groupStat)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Legend for plot colors
                    SectionTitle(title = "Legenda")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LegendItem(color = colorResource(id = R.color.harvested), text = "Colhido")
                        LegendItem(color = colorResource(id = R.color.not_harvested), text = "Não Colhido")
                        LegendItem(color = colorResource(id = R.color.discarded), text = "Descartado")
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Plots List
                    SectionTitle(title = "Plots")
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(2f),  // Aumentado o peso para dar mais espaço à lista
                        verticalArrangement = Arrangement.spacedBy(4.dp) // Reduzido o espaçamento
                    ) {
                        if (pagedPlots != null) {
                            pagingItems(pagedPlots) { plot ->
                                if (plot != null) {
                                    PlotItem(plot = plot)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupFilters(
    groups: List<String>,
    selectedGroup: String?,
    onSelectGroup: (String?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            FilterChip(
                selected = selectedGroup == null,
                onClick = { onSelectGroup(null) },
                label = { Text("Todos") }
            )
        }
        
        items(groups) { group ->
            FilterChip(
                selected = selectedGroup == group,
                onClick = { onSelectGroup(group) },
                label = { Text(group) }
            )
        }
    }
}

@Composable
fun GroupStatCard(groupStat: GroupStats) {
    Card(
        modifier = Modifier
            .width(90.dp)
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = groupStat.grupoId,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "${groupStat.colhidos}/${groupStat.total}",
                style = MaterialTheme.typography.bodySmall
            )
            
            LinearProgressIndicator(
                progress = groupStat.percentageColhido / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.background
            )
        }
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun PlotItem(plot: Plot) {
    val backgroundColor = when {
        plot.descartado -> colorResource(id = R.color.discarded)
        plot.colhido -> colorResource(id = R.color.harvested)
        else -> colorResource(id = R.color.not_harvested)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),  // Reduzido o padding para caber mais plots
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator
            Box(
                modifier = Modifier
                    .size(12.dp)  // Reduzido o tamanho do indicador
                    .background(backgroundColor, CircleShape)
            )
            
            Spacer(modifier = Modifier.width(8.dp))  // Reduzido o espaçamento
            
            // Plot details - Layout melhorado conforme solicitado
            Column(modifier = Modifier.weight(1f)) {
                // Plot em destaque
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Plot: ${plot.plot}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Adicionar um indicador visual se o plot for descartado
                    if (plot.descartado) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "[DESCARTADO]",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorResource(id = R.color.discarded),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // RECID logo abaixo
                Text(
                    text = "RECID: ${plot.recid}",
                    style = MaterialTheme.typography.bodySmall
                )
                
                // Mostrar a decisão se for diferente de "undecided"
                if (plot.decision.isNotEmpty() && plot.decision != "undecided") {
                    Text(
                        text = "Decisão: ${plot.decision}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (plot.descartado) colorResource(id = R.color.discarded) else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            // Grupo e localização à direita
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "Grupo: ${plot.grupoId}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Range: ${plot.range}, Row: ${plot.row}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

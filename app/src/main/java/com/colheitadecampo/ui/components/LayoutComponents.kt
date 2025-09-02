package com.colheitadecampo.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String,
    onNavigateUp: (() -> Unit)? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    if (navigationIcon != null) {
                        navigationIcon()
                    } else if (onNavigateUp != null) {
                        IconButton(onClick = onNavigateUp) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Voltar"
                            )
                        }
                    }
                },
                actions = {
                    if (actions != null) {
                        actions()
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                // Aplicar padding para evitar sobreposição com a status bar
                modifier = Modifier.statusBarsPadding()
            )
        },
        floatingActionButton = floatingActionButton,
        // Aplicamos padding para a navigation bar e garantimos o preenchimento completo da tela
        // Usamos contentWindowInsets para evitar problemas com a barra de status e navegação
        contentWindowInsets = WindowInsets(0),
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) { paddingValues ->
        // Aplicamos padding da Scaffold para o conteúdo, garantindo espaçamento adequado
        content(
            // Usamos valores de padding adequados para garantir visibilidade de todo conteúdo
            PaddingValues(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding() + 24.dp, // Aumentamos o padding inferior para evitar cortes
                start = 16.dp,
                end = 16.dp
            )
        )
    }
}

@Composable
fun SectionTitle(
    text: String? = null,
    title: String? = null, // Para compatibilidade com código existente
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color? = null
) {
    val titleText = text ?: title ?: ""
    Text(
        text = titleText,
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold
        ),
        color = color ?: MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(
            horizontal = 16.dp,
            vertical = 8.dp
        )
    )
}

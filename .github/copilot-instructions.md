# Colheita de Campo - App Android

Este é um aplicativo Android nativo em Kotlin + Jetpack Compose, offline-first, para operações de colheita em campo com suporte a importação e exportação de arquivos Excel.

## Características principais
- Importação/exportação de arquivos Excel (.xlsx)
- Operação offline completa com banco de dados Room
- Interface com Jetpack Compose
- Suporte a scanner externo para captura de RECIDs
- Colheita por plot individual ou por grupo

## Stack tecnológica
- Kotlin
- Jetpack Compose
- Room Database
- ViewModel + Coroutines/Flow
- Navigation Compose
- Apache POI (Excel handling)
- Hilt (Dependency Injection)

## Arquitetura
O aplicativo segue uma arquitetura MVVM com Clean Architecture:

- **Data layer**: Repositórios, DAOs e modelos
- **Domain layer**: Use cases para operações de negócio
- **UI layer**: ViewModels e componentes de UI em Compose

## Fluxo principal
1. Importação de arquivo Excel
2. Operação de colheita (individual ou por grupo)
3. Exportação do arquivo atualizado

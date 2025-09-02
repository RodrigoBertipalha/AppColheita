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

# Copilot Instructions for App Colheita

Welcome to the App Colheita codebase! This document provides essential guidelines for AI coding agents to be productive in this project. It covers the architecture, workflows, conventions, and integration points specific to this repository.

## Project Overview

App Colheita is an Android application designed for field harvesting operations. It is built with Kotlin and Jetpack Compose, following an MVVM architecture with Clean Architecture principles. The app supports offline-first functionality using Room Database and integrates with external tools like Excel for data import/export.

### Key Features
- **Excel Integration**: Import/export `.xlsx` files using Apache POI.
- **Offline Functionality**: Room Database ensures data persistence.
- **Field Operations**: Supports harvesting by individual plots or groups.
- **Dependency Injection**: Hilt is used for managing dependencies.
- **Navigation**: Navigation Compose handles screen transitions.

## Architecture

The project is structured into three main layers:

1. **UI Layer**:
   - Located in `app/src/main/java/com/colheitadecampo/ui/`
   - Built with Jetpack Compose.
   - Uses `ViewModel` to manage state.
   - Example: `HarvestScreen.kt` for the harvesting interface.

2. **Domain Layer**:
   - Contains business logic.
   - Use cases are defined here, e.g., `MarcarColhidoPorRecidUseCase.kt`.

3. **Data Layer**:
   - Handles data persistence and retrieval.
   - Includes DAOs, repositories, and models.
   - Example: `PlotDao.kt` for database operations.

## Developer Workflows

### Building the Project
- Use Gradle to build the project:
  ```bash
  ./gradlew assembleDebug
  ```
- APKs are generated in `app/build/outputs/apk/`.

### Testing
- Run unit tests with:
  ```bash
  ./gradlew test
  ```
- Instrumentation tests:
  ```bash
  ./gradlew connectedAndroidTest
  ```

### Debugging
- Use Android Studio's debugger for runtime inspection.
- Logs are managed with Timber.

### Generating APK for Testing
- Use the provided script `gerar_apk_teste.bat` to generate a debug APK:
  ```
  gerar_apk_teste.bat
  ```
- The APK will be copied to the desktop for easy access.

## Project-Specific Conventions

1. **UI Patterns**:
   - Use `AppScaffold` for consistent screen layouts.
   - Ensure proper handling of system bars with `statusBarsPadding` and `navigationBarsPadding`.

2. **Data Handling**:
   - Numeric fields are treated as strings to avoid displaying `.0` in the UI.
   - Use `getCellValue` in `ExcelService.kt` for consistent Excel data parsing.

3. **Search Optimization**:
   - Prefer database queries over in-memory filtering for performance.
   - Example: `findPlotByExactRecid` in `PlotDao.kt`.

## Integration Points

- **Excel Handling**:
  - Apache POI is used for reading/writing Excel files.
  - Key file: `ExcelService.kt`.

- **Dependency Injection**:
  - Hilt modules are defined in `di/`.

- **Navigation**:
  - Centralized in `Navigation.kt`.

## Key Files and Directories

- `app/src/main/java/com/colheitadecampo/ui/`: UI components and screens.
- `app/src/main/java/com/colheitadecampo/data/`: Data models, DAOs, and repositories.
- `app/src/main/java/com/colheitadecampo/domain/`: Business logic and use cases.
- `app/src/main/java/com/colheitadecampo/util/ExcelService.kt`: Excel file handling.
- `gerar_apk_teste.bat`: Script for generating debug APKs.

## Notes for AI Agents

- Always ensure UI components are scrollable and adapt to different screen sizes.
- Follow the MVVM pattern strictly to maintain separation of concerns.
- Validate Excel data formats during import to prevent runtime errors.

Feel free to iterate on this document as the project evolves!

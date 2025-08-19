# Colheita de Campo - Aplicativo Android

## Descrição
App Android nativo em Kotlin + Jetpack Compose, offline-first, para importar arquivos Excel (.xlsx), operar colheita em campo e exportar o arquivo atualizado com os dados da colheita.

## Requisitos
- Android 8.0 (API 26) ou superior
- Espaço em disco: ~15MB
- Permissões:
  - Armazenamento (leitura/escrita)

## Funcionalidades Principais
- Importação de arquivos .xlsx com opção de substituir ou mesclar dados
- Dashboard com visualização da colheita por grupos
- Colheita por plot individual ou por grupo
- Exportação do arquivo atualizado com dados da colheita
- Compartilhamento do arquivo via e-mail

## Instalação
1. Copie o arquivo APK para o dispositivo
2. Abra o arquivo APK no dispositivo e siga as instruções de instalação
3. Conceda as permissões solicitadas (armazenamento)

## Como Utilizar

### Fluxo básico
1. Na tela inicial, clique em "Buscar Arquivo" para importar um arquivo Excel
2. Escolha entre substituir ou mesclar dados
3. Visualize o dashboard para acompanhar o progresso da colheita
4. Na tela de colheita, escaneie ou digite os RECIDs para marcar plots como colhidos
5. Quando concluído, volte à tela de importação/exportação e exporte o arquivo atualizado

### Importação de Arquivos
- O arquivo Excel deve conter as seguintes colunas:
  - Loc Seq, entry book name, range, row, recid, tier, plot, GrupoId
- "recid" é utilizado como identificador único
- "GrupoId" indica o grupo ao qual o plot pertence (ex: f1, m3)

### Colheita
- Individual: Digite ou escaneie um RECID e clique em "Colher por Plot"
- Por grupo: Clique em "Colher por Grupo", selecione os plots desejados e confirme
- Para desfazer a colheita: Digite o RECID e clique em "Desfazer Colheita"

### Exportação
- Gera um novo arquivo Excel com todas as colunas originais + coluna "colhido"
- O arquivo é salvo no diretório Documents do aplicativo
- Utilize o botão "Enviar por E-mail" para compartilhar o arquivo

## Suporte para Scanner
O aplicativo suporta scanners externos (conectados via Bluetooth ou USB). O scanner deve ser configurado para funcionar como um teclado comum.

## Geração e Assinatura do APK

### Gerar APK usando Gradle
```
./gradlew assembleRelease
```

### Assinatura manual do APK
1. Gere uma keystore (caso ainda não tenha)
```
keytool -genkey -v -keystore colheita.keystore -alias colheita -keyalg RSA -keysize 2048 -validity 10000
```

2. Configure a keystore no arquivo `gradle.properties` (não inclua no controle de versão)
```
RELEASE_STORE_FILE=../colheita.keystore
RELEASE_STORE_PASSWORD=sua_senha
RELEASE_KEY_ALIAS=colheita
RELEASE_KEY_PASSWORD=sua_senha
```

3. Configure o arquivo `app/build.gradle.kts` para usar a keystore
```kotlin
android {
    ...
    signingConfigs {
        create("release") {
            storeFile = file(project.property("RELEASE_STORE_FILE") as String)
            storePassword = project.property("RELEASE_STORE_PASSWORD") as String
            keyAlias = project.property("RELEASE_KEY_ALIAS") as String
            keyPassword = project.property("RELEASE_KEY_PASSWORD") as String
        }
    }
    buildTypes {
        getByName("release") {
            ...
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

4. Gere o APK assinado
```
./gradlew assembleRelease
```

O APK final estará disponível em `app/build/outputs/apk/release/app-release.apk`

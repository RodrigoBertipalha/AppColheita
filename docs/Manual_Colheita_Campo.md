# Manual do Aplicativo Colheita de Campo

## Sumário
1. [Introdução](#introdução)
2. [Requisitos da Planilha Excel](#requisitos-da-planilha-excel)
3. [Funcionalidades do Aplicativo](#funcionalidades-do-aplicativo)
4. [Telas do Aplicativo](#telas-do-aplicativo)
5. [Fluxo de Trabalho Completo](#fluxo-de-trabalho-completo)
6. [Perguntas Frequentes](#perguntas-frequentes)

## Introdução

O aplicativo **Colheita de Campo** foi desenvolvido para facilitar o processo de colheita em campos agrícolas, com suporte para operações offline e integração com planilhas Excel. Este manual apresenta as informações necessárias para a utilização correta e eficiente do aplicativo.

**Características principais:**
- Importação e exportação de arquivos Excel (.xlsx)
- Operação offline completa com banco de dados local
- Suporte a scanner externo para captura de RECIDs
- Colheita por plot individual ou por grupo
- Tratamento de plots descartados

## Requisitos da Planilha Excel

### Formato da Planilha

O aplicativo trabalha com planilhas Excel (.xlsx) que devem seguir um formato específico. Abaixo estão as colunas obrigatórias e opcionais:

| Coluna | Tipo | Obrigatório | Descrição |
|--------|------|-------------|-----------|
| RECID | Texto | Sim | Identificador único do plot |
| LOC_SEQ | Texto | Sim | Sequência de localização |
| ENTRYBOOKNAME | Texto | Sim | Nome do livro de entrada |
| RANGE | Texto | Sim | Número da faixa (range) |
| ROW | Texto | Sim | Número da linha (row) |
| TIER | Texto | Sim | Nível (tier) |
| PLOT | Texto | Sim | Número do plot |
| GROUP | Texto | Sim | Identificador do grupo ao qual o plot pertence |
| COLHIDO | Texto/Booleano | Não | Status de colheita (padrão: não colhido) |
| DECISION | Texto | Não | Decisão sobre o plot ("discard" indica que o plot deve ser descartado) |

### Exemplo de Planilha

| RECID | LOC_SEQ | ENTRYBOOKNAME | RANGE | ROW | TIER | PLOT | GROUP | COLHIDO | DECISION |
|-------|---------|---------------|-------|-----|------|------|-------|---------|----------|
| 12345 | LOC1 | BOOK1 | 1 | 2 | A | 101 | G1 | | |
| 12346 | LOC1 | BOOK1 | 1 | 3 | A | 102 | G1 | | |
| 12347 | LOC1 | BOOK1 | 2 | 1 | A | 201 | G2 | | discard |
| 12348 | LOC1 | BOOK1 | 2 | 2 | A | 202 | G2 | | |

### Regras para a Coluna DECISION

A coluna DECISION é usada para identificar plots que devem ser descartados:
- Se contiver o valor "discard", o plot será marcado como descartado no aplicativo
- Plots descartados não podem ser colhidos
- Plots descartados são exibidos em azul na interface
- Plots descartados não são contabilizados para o cálculo de percentual de colheita

## Funcionalidades do Aplicativo

### Importação e Exportação de Dados

- **Importação**: O aplicativo lê arquivos Excel (.xlsx) e importa os dados para o banco de dados local.
- **Exportação**: Os dados atualizados (incluindo status de colheita) podem ser exportados de volta para um arquivo Excel.

### Operações de Colheita

- **Colheita Individual**: Marcar plots individuais como colhidos usando seu RECID.
- **Colheita por Grupo**: Marcar múltiplos plots de um mesmo grupo como colhidos simultaneamente.
- **Desfazer Colheita**: Permite desfazer a última operação de colheita.
- **Tratamento de Plots Descartados**: Plots marcados como descartados não podem ser colhidos.

### Visualização de Dados

- **Dashboard**: Visão geral do campo com estatísticas de colheita.
- **Listagem de Plots**: Lista todos os plots do campo com indicação visual do status (colhido, não colhido, descartado).
- **Busca por RECID**: Permite localizar rapidamente um plot específico pelo seu RECID.

## Telas do Aplicativo

### Tela Inicial

A tela inicial permite importar uma nova planilha ou selecionar um campo já importado para iniciar a colheita ou visualizar os dados.

**Funcionalidades:**
- Botão para importar nova planilha Excel
- Lista de campos importados anteriormente
- Opção para exportar dados atualizados

### Tela de Dashboard

O dashboard apresenta uma visão geral do campo selecionado, com estatísticas de colheita e listagem de plots.

**Elementos da tela:**
- Nome do campo
- Total de plots
- Plots elegíveis (excluindo descartados)
- Plots descartados
- Porcentagem de colheita
- Barra de progresso de colheita
- Campo de busca por RECID
- Estatísticas por grupo
- Legenda de cores (verde = colhido, vermelho = não colhido, azul = descartado)
- Lista de plots com indicação visual de status

**Código de cores:**
- **Verde**: Plot colhido
- **Vermelho**: Plot não colhido
- **Azul**: Plot descartado

### Tela de Colheita

A tela de colheita permite marcar plots como colhidos, individualmente ou em grupo.

**Elementos da tela:**
- Campo para inserção do RECID (manual ou via scanner)
- Botão para marcar como colhido
- Estatísticas de colheita atualizadas
- Seleção de grupo para colheita em massa
- Opção para desfazer última colheita
- Informações sobre o último plot colhido

**Mensagens de status:**
- Confirmação de colheita bem-sucedida
- Aviso quando um plot já foi colhido
- Aviso quando um plot foi descartado
- Mensagens de erro em caso de problemas

## Fluxo de Trabalho Completo

### Importação de Dados
1. Na tela inicial, selecione "Importar Planilha"
2. Navegue até o arquivo Excel (.xlsx) e selecione-o
3. Nomeie o campo (ou aceite o nome sugerido)
4. Aguarde a conclusão da importação

### Colheita de Plots
1. Selecione um campo na tela inicial
2. Navegue até a tela de colheita clicando no ícone de play
3. Para colheita individual:
   - Escaneie o código de barras do RECID ou digite-o manualmente
   - Pressione o botão para confirmar a colheita
4. Para colheita por grupo:
   - Selecione o grupo desejado
   - Marque os plots a serem colhidos
   - Confirme a colheita em massa

### Visualização de Progresso
1. Acesse o Dashboard para ver o progresso geral
2. Use o campo de busca para localizar plots específicos
3. Verifique as estatísticas por grupo

### Exportação de Dados
1. Na tela inicial, selecione o campo desejado
2. Clique em "Exportar Dados"
3. Escolha o local para salvar o arquivo Excel atualizado
4. O arquivo exportado conterá todos os dados originais mais o status de colheita atualizado

## Perguntas Frequentes

**P: Como o aplicativo funciona sem conexão à internet?**  
R: O aplicativo armazena todos os dados localmente utilizando um banco de dados Room. Isso permite operação completa sem necessidade de conexão à internet.

**P: Como identifico plots que foram descartados?**  
R: Plots descartados são exibidos em azul e marcados com o texto "[DESCARTADO]" na listagem. Eles também são contabilizados separadamente nas estatísticas.

**P: É possível usar um scanner externo para captura de RECIDs?**  
R: Sim, o aplicativo suporta scanners externos que funcionem como dispositivos de entrada de teclado. Basta focar no campo de entrada de RECID e usar o scanner.

**P: Como desfazer uma colheita errada?**  
R: Na tela de colheita, use o botão "Desfazer Colheita" para reverter a última operação realizada.

**P: Os plots descartados contam para o percentual de colheita?**  
R: Não, o percentual de colheita é calculado apenas sobre os plots elegíveis (total menos descartados).

**P: Como marcar um plot como descartado?**  
R: Os plots descartados são definidos na planilha Excel de origem através da coluna "DECISION". Não é possível marcar plots como descartados diretamente no aplicativo.

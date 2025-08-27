# Instruções para Conversão do Manual

Este diretório contém o manual de usuário para o aplicativo Colheita de Campo em formato Markdown. Abaixo estão as instruções para converter o documento para PDF ou Word.

## Convertendo para PDF

### Usando Pandoc (Recomendado)

1. Instale o [Pandoc](https://pandoc.org/installing.html) e um sistema LaTeX como [MiKTeX](https://miktex.org/download) (Windows) ou [MacTeX](https://www.tug.org/mactex/) (macOS).

2. Abra o terminal na pasta docs e execute:
   ```
   pandoc Manual_Colheita_Campo.md -o Manual_Colheita_Campo.pdf --pdf-engine=xelatex -V geometry:margin=2.5cm
   ```

### Usando Editores Markdown

Alternativas online ou aplicativos que podem converter Markdown para PDF:
- [MarkdownToPDF.com](https://www.markdowntopdf.com/)
- [Typora](https://typora.io/) (aplicativo desktop)
- [Visual Studio Code](https://code.visualstudio.com/) com extensão Markdown PDF

## Convertendo para Word

### Usando Pandoc

1. Instale o [Pandoc](https://pandoc.org/installing.html).

2. Abra o terminal na pasta docs e execute:
   ```
   pandoc Manual_Colheita_Campo.md -o Manual_Colheita_Campo.docx -V geometry:margin=2.5cm
   ```

### Usando Editores Online

Alternativas online para converter Markdown para Word:
- [Convertio](https://convertio.co/markdown-docx/)
- [CloudConvert](https://cloudconvert.com/md-to-docx)

## Adicionando Imagens

Quando adicionar imagens ao manual, siga estas etapas:

1. Coloque as imagens na pasta `docs/imagens/`
2. Referencie-as no documento Markdown desta forma:
   ```markdown
   ![Descrição da Imagem](imagens/nome_da_imagem.png)
   ```
3. Ao converter o documento, as imagens serão incluídas automaticamente.

## Observações

- As imagens descritas em `Imagens_Manual.md` devem ser criadas e adicionadas antes da conversão final do documento.
- Ao adicionar imagens, verifique se elas têm resolução suficiente e se os detalhes importantes estão visíveis.
- Ao converter para PDF ou Word, verifique se o formato da tabela está correto e ajuste se necessário.

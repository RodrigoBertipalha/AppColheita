package com.colheitadecampo.util

import com.colheitadecampo.data.model.Field
import com.colheitadecampo.data.model.Plot
import io.mockk.*
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ExcelServiceTest {
    
    private lateinit var excelService: ExcelService
    private lateinit var workbook: XSSFWorkbook
    private lateinit var sheet: Sheet
    private lateinit var row: Row
    private lateinit var mockCell: Cell
    
    @Before
    fun setup() {
        workbook = mockk(relaxed = true)
        sheet = mockk(relaxed = true)
        row = mockk(relaxed = true)
        mockCell = mockk(relaxed = true)
        
        excelService = ExcelService()
        
        mockkConstructor(XSSFWorkbook::class)
        every { anyConstructed<XSSFWorkbook>().getSheetAt(0) } returns sheet
        every { sheet.getRow(0) } returns row
        every { row.getCell(0) } returns mockCell
    }
    
    @Test
    fun `extractFieldName should return correct field name`() {
        // Given
        val inputStream = mockk<InputStream>()
        every { mockCell.stringCellValue } returns "Test Field"
        every { anyConstructed<XSSFWorkbook>().getSheetAt(0) } returns sheet
        
        // When
        val fieldName = excelService.extractFieldName(inputStream)
        
        // Then
        assertEquals("Test Field", fieldName)
    }
    
    @Test
    fun `extractPlots should correctly parse plots from Excel`() {
        // Given
        val inputStream = mockk<InputStream>()
        val fieldId = 1L
        
        // Mock the header row
        val headerRow = mockk<Row>()
        every { sheet.getRow(1) } returns headerRow
        
        // Define the column indices
        val headerCells = mockk<Iterator<Cell>>()
        every { headerRow.cellIterator() } returns headerCells
        
        // Mock header cells
        val locSeqCell = mockk<Cell>()
        val entryBookNameCell = mockk<Cell>()
        val rangeCell = mockk<Cell>()
        val rowCell = mockk<Cell>()
        val recidCell = mockk<Cell>()
        val tierCell = mockk<Cell>()
        val plotCell = mockk<Cell>()
        val grupoIdCell = mockk<Cell>()
        
        every { headerCells.hasNext() } returnsMany listOf(true, true, true, true, true, true, true, true, false)
        every { headerCells.next() } returnsMany listOf(
            locSeqCell, entryBookNameCell, rangeCell, rowCell, recidCell, tierCell, plotCell, grupoIdCell
        )
        
        every { locSeqCell.stringCellValue } returns "Loc Seq"
        every { entryBookNameCell.stringCellValue } returns "entry book name"
        every { rangeCell.stringCellValue } returns "range"
        every { rowCell.stringCellValue } returns "row"
        every { recidCell.stringCellValue } returns "recid"
        every { tierCell.stringCellValue } returns "tier"
        every { plotCell.stringCellValue } returns "plot"
        every { grupoIdCell.stringCellValue } returns "GrupoId"
        
        every { locSeqCell.columnIndex } returns 0
        every { entryBookNameCell.columnIndex } returns 1
        every { rangeCell.columnIndex } returns 2
        every { rowCell.columnIndex } returns 3
        every { recidCell.columnIndex } returns 4
        every { tierCell.columnIndex } returns 5
        every { plotCell.columnIndex } returns 6
        every { grupoIdCell.columnIndex } returns 7
        
        // Mock data rows
        val dataRow1 = mockk<Row>()
        val dataRow2 = mockk<Row>()
        
        every { sheet.lastRowNum } returns 3
        every { sheet.getRow(2) } returns dataRow1
        every { sheet.getRow(3) } returns dataRow2
        
        // Mock cells for first data row
        val dataRow1Cells = Array<Cell>(8) { mockk() }
        every { dataRow1.getCell(0) } returns dataRow1Cells[0]
        every { dataRow1.getCell(1) } returns dataRow1Cells[1]
        every { dataRow1.getCell(2) } returns dataRow1Cells[2]
        every { dataRow1.getCell(3) } returns dataRow1Cells[3]
        every { dataRow1.getCell(4) } returns dataRow1Cells[4]
        every { dataRow1.getCell(5) } returns dataRow1Cells[5]
        every { dataRow1.getCell(6) } returns dataRow1Cells[6]
        every { dataRow1.getCell(7) } returns dataRow1Cells[7]
        
        every { dataRow1Cells[0].stringCellValue } returns "A1"
        every { dataRow1Cells[1].stringCellValue } returns "Book1"
        every { dataRow1Cells[2].stringCellValue } returns "1"
        every { dataRow1Cells[3].stringCellValue } returns "2"
        every { dataRow1Cells[4].stringCellValue } returns "REC001"
        every { dataRow1Cells[5].stringCellValue } returns "T1"
        every { dataRow1Cells[6].stringCellValue } returns "P1"
        every { dataRow1Cells[7].stringCellValue } returns "G1"
        
        // Mock cells for second data row
        val dataRow2Cells = Array<Cell>(8) { mockk() }
        every { dataRow2.getCell(0) } returns dataRow2Cells[0]
        every { dataRow2.getCell(1) } returns dataRow2Cells[1]
        every { dataRow2.getCell(2) } returns dataRow2Cells[2]
        every { dataRow2.getCell(3) } returns dataRow2Cells[3]
        every { dataRow2.getCell(4) } returns dataRow2Cells[4]
        every { dataRow2.getCell(5) } returns dataRow2Cells[5]
        every { dataRow2.getCell(6) } returns dataRow2Cells[6]
        every { dataRow2.getCell(7) } returns dataRow2Cells[7]
        
        every { dataRow2Cells[0].stringCellValue } returns "A2"
        every { dataRow2Cells[1].stringCellValue } returns "Book1"
        every { dataRow2Cells[2].stringCellValue } returns "2"
        every { dataRow2Cells[3].stringCellValue } returns "3"
        every { dataRow2Cells[4].stringCellValue } returns "REC002"
        every { dataRow2Cells[5].stringCellValue } returns "T2"
        every { dataRow2Cells[6].stringCellValue } returns "P2"
        every { dataRow2Cells[7].stringCellValue } returns "G2"
        
        // When
        val plots = excelService.extractPlots(inputStream, fieldId)
        
        // Then
        assertEquals(2, plots.size)
        
        assertEquals("A1", plots[0].locSeq)
        assertEquals("Book1", plots[0].entryBookName)
        assertEquals("1", plots[0].range)
        assertEquals("2", plots[0].row)
        assertEquals("REC001", plots[0].recid)
        assertEquals("T1", plots[0].tier)
        assertEquals("P1", plots[0].plot)
        assertEquals("G1", plots[0].grupoId)
        assertEquals(fieldId, plots[0].fieldId)
        assertEquals(false, plots[0].colhido)
        
        assertEquals("A2", plots[1].locSeq)
        assertEquals("Book1", plots[1].entryBookName)
        assertEquals("2", plots[1].range)
        assertEquals("3", plots[1].row)
        assertEquals("REC002", plots[1].recid)
        assertEquals("T2", plots[1].tier)
        assertEquals("P2", plots[1].plot)
        assertEquals("G2", plots[1].grupoId)
        assertEquals(fieldId, plots[1].fieldId)
        assertEquals(false, plots[1].colhido)
    }
    
    @Test
    fun `createExportFile should generate Excel file with plot data`() {
        // Given
        val field = Field(id = 1, name = "Test Field")
        val plots = listOf(
            Plot(
                recid = "REC001",
                fieldId = 1,
                locSeq = "A1",
                entryBookName = "Book1",
                range = "1",
                row = "2",
                tier = "T1",
                plot = "P1",
                grupoId = "G1",
                colhido = true
            ),
            Plot(
                recid = "REC002",
                fieldId = 1,
                locSeq = "A2",
                entryBookName = "Book1",
                range = "2",
                row = "3",
                tier = "T2",
                plot = "P2",
                grupoId = "G2",
                colhido = false
            )
        )
        
        // Mock file operations
        mockkStatic(File::class)
        val mockFile = mockk<File>()
        every { File(any()) } returns mockFile
        every { mockFile.exists() } returns true
        
        // Mock workbook writing
        val outputStream = ByteArrayOutputStream()
        every { mockFile.outputStream() } returns outputStream
        
        // Create a real workbook for testing
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Harvest Data")
        
        mockkConstructor(XSSFWorkbook::class)
        every { anyConstructed<XSSFWorkbook>().createSheet(any()) } returns sheet
        every { anyConstructed<XSSFWorkbook>().write(any()) } just runs
        
        // When
        val exportFile = excelService.createExportFile(field, plots)
        
        // Then
        assertNotNull(exportFile)
        verify { anyConstructed<XSSFWorkbook>().write(any()) }
    }
}

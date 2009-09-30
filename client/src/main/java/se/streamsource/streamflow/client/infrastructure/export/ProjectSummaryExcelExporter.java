package se.streamsource.streamflow.client.infrastructure.export;

import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import se.streamsource.streamflow.resource.overview.ProjectSummaryDTO;
import se.streamsource.streamflow.resource.overview.ProjectSummaryListDTO;

public class ProjectSummaryExcelExporter extends AbstractExcelExporter
{
	List<ProjectSummaryDTO> summaryListDTO;

	@Override
	protected void doConversion()
	{
		summaryListDTO = (List<ProjectSummaryDTO>)getExportable();
		Sheet sheet = createSheet("Project Summary");

		// Create header cells
		Row headerRow = sheet.createRow((short) 0);
		headerRow.setHeightInPoints(30);

		createHeaderCell("Project", getWorkbook(), headerRow, (short) 0,
				HSSFCellStyle.ALIGN_CENTER, HSSFCellStyle.VERTICAL_CENTER);
		createHeaderCell("Inbox", getWorkbook(), headerRow, (short) 1,
				HSSFCellStyle.ALIGN_CENTER, HSSFCellStyle.VERTICAL_CENTER);
		createHeaderCell("Assigned", getWorkbook(), headerRow, (short) 2,
				HSSFCellStyle.ALIGN_CENTER, HSSFCellStyle.VERTICAL_CENTER);
		createHeaderCell("Total", getWorkbook(), headerRow, (short) 3,
				HSSFCellStyle.ALIGN_CENTER, HSSFCellStyle.VERTICAL_CENTER);
		short rowCounter = 0;
		for (ProjectSummaryDTO summaryDTO : summaryListDTO)
		{
			Row contentRow = sheet.createRow(++rowCounter);
//			contentRow.setHeightInPoints(30);

			// Project
			createCell(summaryDTO.project().get(), getWorkbook(), contentRow, (short) 0,
					HSSFCellStyle.ALIGN_LEFT, HSSFCellStyle.VERTICAL_TOP);
			// Inbox
			createCell(String.valueOf(summaryDTO.inboxCount().get()), getWorkbook(),
					contentRow, (short) 1, HSSFCellStyle.ALIGN_RIGHT,
					HSSFCellStyle.VERTICAL_TOP);
			// Assigned
			createCell(String.valueOf(summaryDTO.assignedCount().get()),
					getWorkbook(), contentRow, (short) 2, HSSFCellStyle.ALIGN_RIGHT,
					HSSFCellStyle.VERTICAL_TOP);
			// Total
			createCell(String.valueOf(summaryDTO.inboxCount().get()
					+ summaryDTO.assignedCount().get()), getWorkbook(), contentRow,
					(short) 3, HSSFCellStyle.ALIGN_RIGHT,
					HSSFCellStyle.VERTICAL_TOP);
		}
	}

	/**
	 * Creates a header cell and aligns it a certain way.
	 * 
	 * @param wb
	 *            the workbook
	 * @param row
	 *            the row to create the cell in
	 * @param column
	 *            the column number to create the cell in
	 * @param halign
	 *            the horizontal alignment for the cell.
	 * @param valign
	 *            the vertical alignment for the cell.
	 */
	private static void createHeaderCell(String cellValue, Workbook wb,
			Row row, short column, short halign, short valign)
	{
		Cell cell = row.createCell(column);
		cell.setCellValue(new HSSFRichTextString(cellValue));
		CellStyle cellStyle = wb.createCellStyle();
		cellStyle.setAlignment(halign);
		cellStyle.setVerticalAlignment(valign);
		cell.setCellStyle(cellStyle);
	}

	/**
	 * Creates a cell and aligns it a certain way.
	 * 
	 * @param wb
	 *            the workbook
	 * @param row
	 *            the row to create the cell in
	 * @param column
	 *            the column number to create the cell in
	 * @param halign
	 *            the horizontal alignment for the cell.
	 * @param valign
	 *            the vertical alignment for the cell.
	 */
	private static void createCell(String cellValue, Workbook wb, Row row,
			short column, short halign, short valign)
	{
		Cell cell = row.createCell(column);
		cell.setCellValue(new HSSFRichTextString(cellValue));
		CellStyle cellStyle = wb.createCellStyle();
		cellStyle.setAlignment(halign);
		cellStyle.setVerticalAlignment(valign);
		cell.setCellStyle(cellStyle);
	}
}

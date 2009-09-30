package se.streamsource.streamflow.client.infrastructure.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public abstract class AbstractExcelExporter implements ExcelExporter
{
	private Object exportable;
	private File fileName;
	private Workbook workbook;
//	private int sheetCounter = 0;
	
	/**
	 * Template method for dealing with the actual exporting flow.
	 * @param exportable
	 * @param fileName
	 * @throws IOException 
	 */
	public void export(Object exportable, File fileName) {
		this.exportable = exportable;
		this.fileName = fileName;
		createWorkbook();
		doConversion();
		try
		{
			saveFile();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected abstract void doConversion();

//	protected void createSheet() 
//	{
//		StringBuilder builder = new StringBuilder("Sheet");
//		workbook.createSheet(builder.append(++sheetCounter).toString());
//	}

	private void createWorkbook()
	{
		workbook = new HSSFWorkbook();
	}

	protected Sheet createSheet(String name) 
	{
		return workbook.createSheet(name);
	}

	private void saveFile() throws IOException
	{
		FileOutputStream fileOut = new FileOutputStream(fileName);
		workbook.write(fileOut);
		fileOut.close();
	}
	
	public Object getExportable()
	{
		return exportable;
	}

	public File getFileName()
	{
		return fileName;
	}

	public Workbook getWorkbook()
	{
		return workbook;
	}

}

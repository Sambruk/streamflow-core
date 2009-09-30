package se.streamsource.streamflow.client.infrastructure.export;

public interface AbstractExporterFactory
{
	public ExcelExporter createExcelExporter();
	public ExcelExporter createPdfExporter();
}

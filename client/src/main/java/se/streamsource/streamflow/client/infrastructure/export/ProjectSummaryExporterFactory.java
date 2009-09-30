package se.streamsource.streamflow.client.infrastructure.export;

public class ProjectSummaryExporterFactory implements AbstractExporterFactory
{
	public ExcelExporter createExcelExporter()
	{
		return new ProjectSummaryExcelExporter();
	}

	public ExcelExporter createPdfExporter()
	{
		// TODO Auto-generated method stub
		return null;
	}

}

package se.streamsource.streamflow.client.export.overview;

import se.streamsource.streamflow.client.infrastructure.export.AbstractExporterFactory;
import se.streamsource.streamflow.client.infrastructure.export.ExcelExporter;

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

package se.streamsource.streamflow.client.infrastructure.export;

import java.io.File;

public interface ExcelExporter
{
	public void export(Object exportable, File fileName);
}

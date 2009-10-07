/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.resource.users.overview;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.resource.overview.ProjectSummaryListDTO;
import se.streamsource.streamflow.web.domain.group.OverviewQueries;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Mapped to /user/{userid}/overview
 */
public class OverviewServerResource extends CommandQueryServerResource
{
	@Structure
	protected ObjectBuilderFactory obf;

    public ProjectSummaryListDTO projectSummary() throws ResourceException
    {
		UnitOfWork uow = uowf.currentUnitOfWork();
		String id = (String) getRequest().getAttributes().get("user");
		OverviewQueries queries = uow.get(OverviewQueries.class, id);

        return queries.getProjectsSummary();
	}

    public OutputRepresentation generateExcelProjectSummary() throws IOException 
    {
    	List<Language> languages = new ArrayList();
    	languages.add(new Language("en-gov"));
    	languages.add(new Language("sv-gov"));
    	languages.add(Language.ENGLISH);
    	languages.add(new Language("sv"));
    	Language language = getRequest().getClientInfo().getPreferredLanguage(languages);
    	if (language == null) 
    	{
    		language = new Language("sv-gov");
    	}
    	Locale locale = new Locale(language.getName());
    	
    	final Workbook workbook = new HSSFWorkbook();

        UnitOfWork uow = uowf.currentUnitOfWork();
        String id = (String) getRequest().getAttributes().get("user");
        OverviewQueries queries = uow.get(OverviewQueries.class, id);

        queries.generateExcelProjectSummary(locale, workbook);
        
        OutputRepresentation representation = new OutputRepresentation(MediaType.APPLICATION_EXCEL)
		{
			@Override
			public void write(OutputStream outputStream) throws IOException
			{
		        workbook.write(outputStream);
			}
		};
		
		representation.write(new ByteArrayOutputStream()); 
		return representation;
    }
}
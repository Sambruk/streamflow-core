/**
 *
 * Copyright 2009-2012 Jayway Products AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.rest.resource.surface.endusers;

import static se.streamsource.dci.value.table.TableValue.DATETIME;
import static se.streamsource.dci.value.table.TableValue.STRING;

import org.qi4j.api.util.Function;
import org.restlet.resource.ResourceException;

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.dci.value.table.TableBuilder;
import se.streamsource.dci.value.table.TableBuilderFactory;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.dci.value.table.TableValue;
import se.streamsource.streamflow.web.context.surface.endusers.OpenCasesContext;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLogEntryValue;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.rest.resource.workspace.cases.form.CaseSubmittedFormsResource;

/**
 * TODO
 */
public class OpenCasesResource
        extends CommandQueryResource
        implements SubResources
{
   public OpenCasesResource()
   {
      super(OpenCasesContext.class);
   }

   public TableValue cases(TableQuery tq) throws Throwable
   {
      Iterable<Case> openCases = context(OpenCasesContext.class).cases(tq);

      TableBuilderFactory tableBuilderFactory = new TableBuilderFactory(module.valueBuilderFactory());

      tableBuilderFactory.
              column("description", "Description", STRING, new Function<CaseEntity, Object>()
              {
                 public Object map(CaseEntity openCase)
                 {
                    return openCase.description().get();
                 }
              }, null).
              column("created", "Created", DATETIME, new Function<CaseEntity, Object>()
              {
                 public Object map(CaseEntity openCase)
                 {
                    return openCase.createdOn().get();
                 }
              }, null).
              column("caseid", "Case id", STRING, new Function<CaseEntity, Object>()
              {
                 public Object map(CaseEntity openCase)
                 {
                    return openCase.caseId().get();
                 }
              }, null).
              column("status", "Status", STRING, new Function<CaseEntity, Object>()
              {
                 public Object map(CaseEntity openCase)
                 {
                    return openCase.status().get().name();
                 }
              }, null).
              column("project", "Project", STRING, new Function<CaseEntity, Object>()
              {
                 public Object map(CaseEntity openCase)
                 {
                    return ((Describable) openCase.owner().get()).getDescription();
                 }
              }, null).
              column("lastupdated", "Last updated", DATETIME, new Function<CaseEntity, Object>()
              {
                 public Object map(CaseEntity openCase)
                 {
                    return ((CaseLogEntryValue) openCase.caselog().get().getLastMessage()).createdOn().get();
                 }
              }, null).
              column("href", "Location", STRING, new Function<CaseEntity, Object>()
              {
                 public Object map(CaseEntity openCase)
                 {
                    return openCase.toString() + "/";
                 }
              }, null);


      TableBuilder builder = tableBuilderFactory.newInstance(tq);

      TableValue table = builder.rows(openCases).orderBy().paging().newTable();
      return table;
   }

   public void resource(String segment) throws ResourceException
   {
      setResourceValidity(setRole(CaseEntity.class, segment));
      subResource(OpenCaseResource.class);
   }
   
}

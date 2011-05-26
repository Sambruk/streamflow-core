/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.web.resource.surface.endusers;

import org.qi4j.api.util.Function;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.dci.value.table.TableBuilder;
import se.streamsource.dci.value.table.TableBuilderFactory;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.dci.value.table.TableValue;
import se.streamsource.streamflow.web.context.surface.endusers.ClosedCasesContext;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;

import static se.streamsource.dci.value.table.TableValue.DATETIME;
import static se.streamsource.dci.value.table.TableValue.STRING;

/**
 * TODO
 */
public class ClosedCasesResource
   extends CommandQueryResource
   implements SubResources
{
   public ClosedCasesResource()
   {
      super(ClosedCasesContext.class);
   }

   public TableValue cases() throws Throwable
   {
      Iterable<CaseEntity> closedCases = (Iterable<CaseEntity>) invoke();

      TableQuery query = (TableQuery) getArguments()[0];

      TableBuilderFactory tableBuilderFactory = new TableBuilderFactory(module.valueBuilderFactory());

      tableBuilderFactory.
              column("description", "Description", STRING, new Function<CaseEntity, Object>()
              {
                 public Object map(CaseEntity closedCase)
                 {
                    return closedCase.description().get();
                 }
              }, null).
              column("created", "Created", DATETIME, new Function<CaseEntity, Object>()
              {
                 public Object map(CaseEntity closedCase)
                 {
                    return closedCase.createdOn().get();
                 }
              }, null).
              column("closed", "Closed", DATETIME, new Function<CaseEntity, Object>()
              {
                 public Object map(CaseEntity closedCase)
                 {
                    return closedCase.getHistoryMessage("closed").createdOn().get();
                 }
              }, null).
              column("caseid", "Case id", STRING, new Function<CaseEntity, Object>()
              {
                 public Object map(CaseEntity closedCase)
                 {
                    return closedCase.caseId().get();
                 }
              }, null).
              column("status", "Status", STRING, new Function<CaseEntity, Object>()
              {
                 public Object map(CaseEntity closedCase)
                 {
                    return closedCase.status().get().name();
                 }
              }, null).
              column("project", "Project", STRING, new Function<CaseEntity, Object>()
              {
                 public Object map(CaseEntity closedCase)
                 {
                    return ((Describable) closedCase.owner().get()).getDescription();
                 }
              }, null).
              column("resolution", "Resolution", STRING, new Function<CaseEntity, Object>()
              {
                 public Object map(CaseEntity closedCase)
                 {
                    String resolution = null;
                    if (closedCase.resolution().get() != null)
                       resolution = closedCase.resolution().get().getDescription();
                    return resolution;
                 }
              }, null).
              column("href", "Location", STRING, new Function<CaseEntity, Object>()
              {
                 public Object map(CaseEntity closedCase)
                 {
                    return closedCase.toString() + "/";
                 }
              }, null);


      TableBuilder builder = tableBuilderFactory.newInstance(query);

      return builder.rows(closedCases).orderBy().paging().newTable();
   }

   public void resource(String segment) throws ResourceException
   {
      setResourceValidity( setRole( CaseEntity.class, segment ) );
      subResource(ClosedCaseResource.class);
   }
}

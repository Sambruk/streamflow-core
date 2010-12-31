/*
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.web.rest;

import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.DateFunctions;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.resource.ResourceException;
import org.slf4j.LoggerFactory;
import se.streamsource.dci.restlet.server.ResultConverter;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.dci.value.table.TableBuilder;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.dci.value.table.TableValue;
import se.streamsource.streamflow.domain.interaction.gtd.CaseStates;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.resource.caze.CaseDTO;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.CaseId;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.created.Creator;
import se.streamsource.streamflow.web.domain.structure.label.Label;

import java.util.Collections;

/**
 * JAVADOC
 */
public class StreamflowResultConverter
      implements ResultConverter
{
   @Structure
   Module module;

   public Object convert( Object result, Request request, Object[] arguments )
   {
      if (result instanceof String)
      {
         ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder( StringValue.class );
         builder.prototype().string().set( (String) result );
         return builder.newInstance();
      } else if (result instanceof Form)
      {
         return result;
      } else if (result instanceof Case)
      {
         if (arguments.length > 0 && arguments[0] instanceof TableQuery)
            return caseTable( Collections.singleton( (CaseEntity) result ), module, request, arguments );
         else
            return caseDTO( (CaseEntity) result, module, request.getResourceRef().getBaseRef().getPath() );
      } else if (result instanceof Query)
      {
         Query query = (Query) result;
         if (query.resultType().equals( Case.class ))
         {
            if (arguments.length > 0 && arguments[0] instanceof TableQuery)
               return caseTable( query, module, request, arguments );
            else
               return buildCaseList( query, module, request.getResourceRef().getBaseRef().getPath() );
         }
      }

      if (result instanceof Iterable)
      {
         Iterable iterable = (Iterable) result;
         return new LinksBuilder( module.valueBuilderFactory() ).rel( "resource" ).addDescribables( iterable ).newLinks();
      }

      return result;
   }

   private LinksValue buildCaseList( Iterable<Case> query, Module module, String basePath )
   {
      LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() ).path( basePath );
      for (Case aCase : query)
      {
         try
         {
            linksBuilder.addLink( caseDTO( (CaseEntity) aCase, module, basePath ) );
         } catch (Exception e)
         {
            LoggerFactory.getLogger( getClass() ).error( "Could not create link for case:" + ((Identity) aCase).identity().get(), e );
         }
      }
      return linksBuilder.newLinks();
   }

   private CaseDTO caseDTO( CaseEntity aCase, Module module, String basePath )
   {
      ValueBuilder<CaseDTO> builder = module.valueBuilderFactory().newValueBuilder( CaseDTO.class );

      CaseDTO prototype = builder.prototype();

      prototype.id().set( aCase.identity().get() );
      prototype.creationDate().set( aCase.createdOn().get() );
      if (aCase.createdBy().get() != null)
         prototype.createdBy().set( ((Describable) aCase.createdBy().get()).getDescription() );
      if (aCase.caseId().get() != null)
         prototype.caseId().set( aCase.caseId().get() );
      prototype.href().set( basePath + "/workspace/cases/" + aCase.identity().get() + "/" );
      prototype.rel().set( "case" );
      if (aCase.owner().get() != null)
         prototype.owner().set( ((Describable) aCase.owner().get()).getDescription() );
      prototype.status().set( aCase.status().get() );
      prototype.text().set( aCase.description().get() );

      if (aCase.caseType().get() != null)
         prototype.caseType().set( aCase.caseType().get().getDescription() );

      if (aCase.isAssigned())
         prototype.assignedTo().set( ((Describable) aCase.assignedTo().get()).getDescription() );

      if (aCase.isStatus( CaseStates.CLOSED ) && aCase.resolution().get() != null)
         prototype.resolution().set( aCase.resolution().get().getDescription() );

      prototype.hasContacts().set( aCase.hasContacts() );
      prototype.hasConversations().set( aCase.hasConversations() );
      prototype.hasSubmittedForms().set( aCase.hasSubmittedForms() );
      prototype.hasAttachments().set( aCase.hasAttachments() );

      // Labels
      LinksBuilder labelsBuilder = new LinksBuilder( module.valueBuilderFactory() ).command( "delete" );
      for (Label label : aCase.labels())
      {
         labelsBuilder.addDescribable( label );
      }
      prototype.labels().set( labelsBuilder.newLinks() );

      // Subcases
      LinksBuilder subcasesBuilder = new LinksBuilder( module.valueBuilderFactory() );
      subcasesBuilder.path( ".." );
      try
      {
         for (Case subCase : aCase.subCases())
         {
            subcasesBuilder.classes( ((Status.Data) subCase).status().get().name() );
            subcasesBuilder.addDescribable( subCase );
         }
      } catch (Exception e)
      {
         e.printStackTrace();
      }
      prototype.subcases().set( subcasesBuilder.newLinks() );

      Case parentCase = aCase.parent().get();
      if (parentCase != null)
      {
         ValueBuilder<LinkValue> linkBuilder = module.valueBuilderFactory().newValueBuilder( LinkValue.class );
         linkBuilder.prototype().id().set( parentCase.toString() );
         linkBuilder.prototype().rel().set( "parent" );
         linkBuilder.prototype().href().set( "../" + parentCase.toString() + "/" );
         linkBuilder.prototype().text().set( ((CaseId.Data) parentCase).caseId().get() );
         prototype.parentCase().set( linkBuilder.newInstance() );
      }

      return builder.newInstance();
   }

   private TableValue caseTable( Iterable<CaseEntity> cases, Module module, Request request, Object[] arguments )
   {
      TableQuery query = (TableQuery) arguments[0];
      TableBuilder table = new TableBuilder( module.valueBuilderFactory() );

      String select = query.select().equals( "*" ) ? "description,created,creator,caseid,href,owner,status,casetype,assigned,hascontacts,hasconversations,hasattachments,hassubmittedforms,labels,subcases,parent" : query.select();
      String[] columns = select.split( "[, ]" );

      // Columns
      for (String column : columns)
      {
         // TODO Make type setting smarter
         if ("created".equals( column ))
            table.column( column, Strings.humanReadable( column ), "date" );
         else if ("hascontacts".equals( column ))
            table.column( column, Strings.humanReadable( column ), "boolean" );
         else if ("hasconversations".equals( column ))
            table.column( column, Strings.humanReadable( column ), "boolean" );
         else if ("hasattachments".equals( column ))
            table.column( column, Strings.humanReadable( column ), "boolean" );
         else if ("hassubmittedforms".equals( column ))
            table.column( column, Strings.humanReadable( column ), "boolean" );
         else
            table.column( column, Strings.humanReadable( column ), "string" );
      }

      // Data
      for (CaseEntity caseEntity : cases)
      {
         try
         {
            table.row();
// "description,created,creator,caseid,href,owner,status,casetype,assigned,hascontacts,hasconversations,hasattachments,hassubmittedforms,labels,subcases,parent"
            for (String column : columns)
            {
               if (column.equals( "description" ))
                  table.cell( caseEntity.identity().get(), caseEntity.description().get() );
               else if (column.equals( "created" ))
                  table.cell( caseEntity.createdOn().get(), DateFunctions.toUtcString( caseEntity.createdOn().get() ) );
               else if (column.equals( "creator" ))
               {
                  Creator v = caseEntity.createdBy().get();
                  table.cell( v, v == null ? "" : ((Describable) v).getDescription() );
               } else if (column.equals( "caseid" ))
               {
                  table.cell( caseEntity.caseId().get(), caseEntity.caseId().get() );
               } else if (column.equals( "href" ))
               {
                  String href = request.getResourceRef().getBaseRef().getPath() + "/workspace/cases/" + caseEntity.identity().get() + "/";
                  table.cell( href, href );
               } else if (column.equals( "owner" ))
               {
                  Owner owner = caseEntity.owner().get();
                  table.cell( owner, owner == null ? null : ((Describable) owner).getDescription() );
               } else if (column.equals( "status" ))
               {
                  table.cell( caseEntity.status().get().name(), Strings.humanReadable( caseEntity.status().get().name() ) );
               } else if (column.equals( "casetype" ))
               {
                  CaseType caseType = caseEntity.caseType().get();
                  table.cell( caseType, caseType == null ? null : caseType.getDescription() );
               } else if (column.equals( "assigned" ))
               {
                  Assignee assignee = caseEntity.assignedTo().get();
                  table.cell( assignee, assignee == null ? null : ((Describable) assignee).getDescription() );
               } else if (column.equals( "hascontacts" ))
               {
                  table.cell( caseEntity.hasContacts(), Boolean.toString( caseEntity.hasContacts() ) );
               } else if (column.equals( "hasconversations" ))
               {
                  table.cell( caseEntity.hasConversations(), Boolean.toString( caseEntity.hasConversations() ) );
               } else if (column.equals( "hassubmittedforms" ))
               {
                  table.cell( caseEntity.hasSubmittedForms(), Boolean.toString( caseEntity.hasSubmittedForms() ) );
               } else if (column.equals( "hasattachments" ))
               {
                  table.cell( caseEntity.hasAttachments(), Boolean.toString( caseEntity.hasAttachments() ) );
               } else if (column.equals( "labels" ))
               {
                  LinksBuilder labelsBuilder = new LinksBuilder( module.valueBuilderFactory() ).command( "delete" );
                  for (Label label : caseEntity.labels())
                  {
                     labelsBuilder.addDescribable( label );
                  }
                  LinksValue linksValue = labelsBuilder.newLinks();
                  table.cell( linksValue, null );
               } else if (column.equals( "subcases" ))
               {
                  LinksBuilder subcasesBuilder = new LinksBuilder( module.valueBuilderFactory() );
                  subcasesBuilder.path( ".." );
                  try
                  {
                     for (Case subCase : caseEntity.subCases())
                     {
                        subcasesBuilder.classes( ((Status.Data) subCase).status().get().name() );
                        subcasesBuilder.addDescribable( subCase );
                     }
                  } catch (Exception e)
                  {
                     e.printStackTrace();
                  }
                  LinksValue linksValue = subcasesBuilder.newLinks();
                  table.cell( linksValue, null );
               } else if (column.equals( "parent" ))
               {
                  Case parentCase = caseEntity.parent().get();
                  if (parentCase != null)
                  {
                     ValueBuilder<LinkValue> linkBuilder = module.valueBuilderFactory().newValueBuilder( LinkValue.class );
                     linkBuilder.prototype().id().set( parentCase.toString() );
                     linkBuilder.prototype().rel().set( "parent" );
                     linkBuilder.prototype().href().set( "../" + parentCase.toString() + "/" );
                     linkBuilder.prototype().text().set( ((CaseId.Data) parentCase).caseId().get() );
                     table.cell( linkBuilder.newInstance(), null );
                  } else
                  {
                     table.cell( null, null );
                  }
               } else
               {
                  throw new ResourceException( org.restlet.data.Status.SERVER_ERROR_INTERNAL, "Unhandled column name:" + column );
               }
            }
         } catch(Exception ex)
         {
            // Could not create row for this case, for some reason
            LoggerFactory.getLogger( getClass() ).error( "Could not create row for case:" + caseEntity.identity().get(), ex );
            table.abortRow();
         }
      }

      return table.newTable();
   }
}

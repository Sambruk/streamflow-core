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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.Value;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.Request;
import se.streamsource.dci.restlet.server.ResultConverter;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.domain.interaction.gtd.CaseStates;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.resource.caze.CaseValue;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.label.Label;

/**
 * JAVADOC
 */
public class StreamflowResultConverter
      implements ResultConverter
{
   @Structure
   Module module;

   public Value convert( Object result, Request request )
   {
      if (result instanceof Case)
      {
         return caseDTO( (CaseEntity) result, module,request.getResourceRef().getBaseRef().getPath() );
      } else if (result instanceof Query)
      {
         Query query = (Query) result;
         if (query.resultType().equals( Case.class ))
         {
            return buildCaseList( query, module, request.getResourceRef().getBaseRef().getPath() );
         }
      }

      if (result instanceof Iterable)
      {
         Iterable iterable = (Iterable) result;
         return new LinksBuilder( module.valueBuilderFactory() ).rel( "resource" ).addDescribables( iterable ).newLinks();
      }

      return (Value) result;
   }

   private LinksValue buildCaseList( Iterable<Case> query, Module module, String basePath )
   {
      LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() ).path( basePath );
      try
      {
         for (Case aCase : query)
         {
            linksBuilder.addLink( caseDTO( (CaseEntity) aCase, module, basePath ) );
         }
      } catch (Exception e)
      {
         return linksBuilder.newLinks();
      }
      return linksBuilder.newLinks();
   }

   private CaseValue caseDTO( CaseEntity aCase, Module module, String basePath )
   {
      ValueBuilder<CaseValue> builder = module.valueBuilderFactory().newValueBuilder( CaseValue.class );

      CaseValue prototype = builder.prototype();

      prototype.id().set( aCase.identity().get() );
      prototype.creationDate().set( aCase.createdOn().get() );
      if (aCase.createdBy().get() != null)
         prototype.createdBy().set( ((Describable) aCase.createdBy().get()).getDescription() );
      if (aCase.caseId().get() != null)
         prototype.caseId().set( aCase.caseId().get() );
      prototype.href().set( basePath + "/cases/" + aCase.identity().get() + "/" );
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

      return builder.newInstance();
   }

}

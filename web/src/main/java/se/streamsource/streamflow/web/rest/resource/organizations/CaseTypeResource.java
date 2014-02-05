/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.web.rest.resource.organizations;

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResource;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.context.administration.ArchivalSettingsContext;
import se.streamsource.streamflow.web.context.administration.CaseAccessDefaultsContext;
import se.streamsource.streamflow.web.context.administration.CaseDefaultDaysToCompleteContext;
import se.streamsource.streamflow.web.context.administration.CaseTypeContext;
import se.streamsource.streamflow.web.context.administration.FormOnCloseContext;
import se.streamsource.streamflow.web.context.administration.PriorityOnCaseContext;
import se.streamsource.streamflow.web.context.structure.DescribableContext;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseTypes;
import se.streamsource.streamflow.web.rest.resource.organizations.forms.FormsResource;
import se.streamsource.streamflow.web.rest.resource.organizations.forms.SelectedFormsResource;

/**
 * JAVADOC
 */
public class CaseTypeResource
   extends CommandQueryResource
{
   public CaseTypeResource()
   {
      super( CaseTypeContext.class, DescribableContext.class );
   }

   public LinksValue possiblemoveto() throws Throwable
   {
      Iterable<CaseTypes> caseTypesList = context(CaseTypeContext.class).possiblemoveto();
      LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory());
      builder.command( "move" );

      for( CaseTypes caseTypes : caseTypesList )
      {
         if(caseTypes instanceof OrganizationEntity )
         {
            builder.addDescribable( ((Describable)caseTypes), "" );
         } else
         {
            builder.addDescribable( ((Describable)caseTypes), ((Describable)((Ownable.Data)caseTypes).owner().get()).getDescription() );
         }
      }
      return builder.newLinks();
   }

   @SubResource
   public void forms( )
   {
      subResource( FormsResource.class );
   }

   @SubResource
   public void labels()
   {
      subResource( LabelsResource.class );
   }

   @SubResource
   public void selectedforms()
   {
      subResource( SelectedFormsResource.class );
   }

   @SubResource
   public void selectedlabels()
   {
      subResource( SelectedLabelsResource.class );
   }

   @SubResource
   public void resolutions()
   {
      subResource( ResolutionsResource.class );
   }

   @SubResource
   public void selectedresolutions()
   {
      subResource( SelectedResolutionsResource.class );
   }

   @SubResource
   public void caseaccessdefaults()
   {
      subResourceContexts(CaseAccessDefaultsContext.class);
   }

   @SubResource
   public void defaultdaystocomplete()
   {
      subResourceContexts( CaseDefaultDaysToCompleteContext.class );
   }

   @SubResource
   public void archival()
   {
      subResourceContexts(ArchivalSettingsContext.class);
   }
   
   @SubResource
   public void formonclose()
   {
      subResourceContexts( FormOnCloseContext.class );
   }
   
   @SubResource
   public void priorityoncase()
   {
      subResourceContexts( PriorityOnCaseContext.class );
   }
}

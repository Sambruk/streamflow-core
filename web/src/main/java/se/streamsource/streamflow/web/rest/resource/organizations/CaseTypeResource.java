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
import se.streamsource.streamflow.web.domain.entity.RequiresRemoved;
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

   @RequiresRemoved(false)
   public LinksValue possiblemoveto() throws Throwable
   {
      Iterable<CaseTypes> caseTypesList = context(CaseTypeContext.class).possiblemoveto();
      LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory());
      builder.command( "move" );

      for( CaseTypes caseTypes : caseTypesList )
      {
         if(caseTypes instanceof OrganizationEntity )
         {
            builder.addDescribable( ((Describable)caseTypes), "--" );
         } else
         {
            builder.addDescribable( ((Describable)caseTypes), ((Describable)((Ownable.Data)caseTypes).owner().get()).getDescription() );
         }
      }
      return builder.newLinks();
   }

   @RequiresRemoved(false)
   @SubResource
   public void forms( )
   {
      subResource( FormsResource.class );
   }

   @RequiresRemoved(false)
   @SubResource
   public void labels()
   {
      subResource( LabelsResource.class );
   }

   @RequiresRemoved(false)
   @SubResource
   public void selectedforms()
   {
      subResource( SelectedFormsResource.class );
   }

   @RequiresRemoved(false)
   @SubResource
   public void selectedlabels()
   {
      subResource( SelectedLabelsResource.class );
   }

   @RequiresRemoved(false)
   @SubResource
   public void resolutions()
   {
      subResource( ResolutionsResource.class );
   }

   @RequiresRemoved(false)
   @SubResource
   public void selectedresolutions()
   {
      subResource( SelectedResolutionsResource.class );
   }

   @RequiresRemoved(false)
   @SubResource
   public void caseaccessdefaults()
   {
      subResourceContexts(CaseAccessDefaultsContext.class);
   }

    @RequiresRemoved(false)
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

    @RequiresRemoved(false)
   @SubResource
   public void formonclose()
   {
      subResourceContexts( FormOnCloseContext.class );
   }

    @RequiresRemoved(false)
   @SubResource
   public void priorityoncase()
   {
      subResourceContexts( PriorityOnCaseContext.class );
   }

    @RequiresRemoved(true)
    @SubResource
   public void casetypedetail()
   {
        subResourceContexts( CaseTypeContext.class );
   }
}

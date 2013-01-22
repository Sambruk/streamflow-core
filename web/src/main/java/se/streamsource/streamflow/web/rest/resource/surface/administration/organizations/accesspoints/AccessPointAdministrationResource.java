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
package se.streamsource.streamflow.web.rest.resource.surface.administration.organizations.accesspoints;

import java.util.List;

import org.qi4j.api.constraint.Name;

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResource;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.context.administration.surface.accesspoints.AccessPointAdministrationContext;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.project.Project;

/**
 * JAVADOC
 */
public class AccessPointAdministrationResource
      extends CommandQueryResource
{
   public AccessPointAdministrationResource()
   {
      super( AccessPointAdministrationContext.class );
   }

   public LinksValue possibleprojects() throws Throwable
   {
      LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory()).
            command( "changeproject" );
      for( Project project : context(AccessPointAdministrationContext.class).possibleprojects() )
      {
         builder.addDescribable( project, ((Describable)((Ownable.Data)project).owner().get()).getDescription() );
      }
      return builder.newLinks();
   }

   public LinksValue possiblecasetypes() throws Throwable
   {
      LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory()).
            command( "changecasetype" );
      for(CaseType caseType : context(AccessPointAdministrationContext.class).possiblecasetypes() )
      {
         builder.addDescribable( caseType, ((Describable)((Ownable.Data)caseType).owner().get()).getDescription() );
      }
      return builder.newLinks();
   }

   public LinksValue possibleforms() throws Throwable
   {
      LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory()).
            command( "setform" );
      for(Form form : context(AccessPointAdministrationContext.class).possibleforms() )
      {
         builder.addDescribable( form, ((Describable)((Ownable.Data)form).owner().get()).getDescription() );
      }
      return builder.newLinks();
   }

   public LinksValue possiblesecondforms() throws Throwable
   {
      LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory()).
            command( "setsecondform" );
      for(Form form : context(AccessPointAdministrationContext.class).possiblesecondforms() )
      {
         builder.addDescribable( form, ((Describable)((Ownable.Data)form).owner().get()).getDescription() );
      }
      return builder.newLinks();
   }

   public LinksValue possibleformtemplates( @Name("filteron") String filteron) throws Throwable
   {
      LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() ).command( "setformtemplate" );

      List<Attachment> attachments = context(AccessPointAdministrationContext.class).possibleformtemplates( filteron );

      for (Attachment attachment : attachments)
      {
         linksBuilder.addLink(((AttachedFile.Data) attachment).name().get(), attachment.toString() );
      }

      return linksBuilder.newLinks();
   }

   @SubResource
   public void labels()
   {
      subResource( AccessPointLabelableResource.class );
   }
}
/**
 *
 * Copyright 2009-2012 Streamsource AB
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

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResource;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.context.administration.surface.accesspoints.AccessPointAdministrationContext;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;

import java.util.List;

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
      return new LinksBuilder(module.valueBuilderFactory()).
            command( "changeproject" ).
            addDescribables( context(AccessPointAdministrationContext.class).possibleprojects() ).
            newLinks();
   }

   public LinksValue possiblecasetypes() throws Throwable
   {
      return new LinksBuilder(module.valueBuilderFactory()).
            command( "changecasetype" ).
            addDescribables( context(AccessPointAdministrationContext.class).possiblecasetypes() ).
            newLinks();
   }

   public LinksValue possibleforms() throws Throwable
   {
      return new LinksBuilder(module.valueBuilderFactory()).
            command( "setform" ).
            addDescribables( context(AccessPointAdministrationContext.class).possibleforms() ).
            newLinks();
   }

   public LinksValue possibleformtemplates(StringValue extensionFilter) throws Throwable
   {
      LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() ).command( "setformtemplate" );

      List<Attachment> attachments = context(AccessPointAdministrationContext.class).possibleformtemplates(extensionFilter);

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
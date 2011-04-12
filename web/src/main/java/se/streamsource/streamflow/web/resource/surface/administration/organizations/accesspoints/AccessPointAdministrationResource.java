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

package se.streamsource.streamflow.web.resource.surface.administration.organizations.accesspoints;

import se.streamsource.dci.restlet.server.*;
import se.streamsource.dci.restlet.server.api.*;
import se.streamsource.streamflow.domain.structure.*;
import se.streamsource.streamflow.infrastructure.application.*;
import se.streamsource.streamflow.web.context.administration.surface.accesspoints.*;
import se.streamsource.streamflow.web.domain.structure.attachment.*;

import java.util.*;

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

   public void possibleprojects() throws Throwable
   {
      result(new LinksBuilder(module.valueBuilderFactory()).
            command( "changeproject" ).
            addDescribables( (Iterable<? extends Describable>) invoke() ).
            newLinks());
   }

   public void possiblecasetypes() throws Throwable
   {
      result(new LinksBuilder(module.valueBuilderFactory()).
            command( "changecasetype" ).
            addDescribables( (Iterable<? extends Describable>) invoke() ).
            newLinks());
   }

   public void possibleforms() throws Throwable
   {
      result(new LinksBuilder(module.valueBuilderFactory()).
            command( "setform" ).
            addDescribables( (Iterable<? extends Describable>) invoke() ).
            newLinks());
   }

   public void possibleformtemplates() throws Throwable
   {
      LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() ).command( "setformtemplate" );

      List<Attachment> attachments = (List<Attachment>) invoke();

      for (Attachment attachment : attachments)
      {
         linksBuilder.addLink(((AttachedFile.Data) attachment).name().get(), attachment.toString() );
      }

      result( linksBuilder.newLinks() );
   }

   @SubResource
   public void labels()
   {
      subResource( AccessPointLabelableResource.class );
   }
}
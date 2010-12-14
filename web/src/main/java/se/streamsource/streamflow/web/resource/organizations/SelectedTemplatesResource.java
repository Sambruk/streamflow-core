/**
 *
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

package se.streamsource.streamflow.web.resource.organizations;

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.value.link.LinksBuilder;
import se.streamsource.streamflow.web.context.administration.surface.SelectedTemplatesContext;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;

import java.util.List;

public class SelectedTemplatesResource extends CommandQueryResource
{
   public SelectedTemplatesResource()
   {
      super( SelectedTemplatesContext.class);
   }

   public void possibledefaulttemplates() throws Throwable
   {
      LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() ).command( "setdefaulttemplate" );

      List<Attachment> attachments = (List<Attachment>) invoke();

      for (Attachment attachment : attachments)
      {
         linksBuilder.addLink(((AttachedFile.Data) attachment).name().get(), attachment.toString() );
      }

      result( linksBuilder.newLinks() );
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

   public void possiblecasetemplates() throws Throwable
   {
      LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() ).command( "setcasetemplate" );

      List<Attachment> attachments = (List<Attachment>) invoke();

      for (Attachment attachment : attachments)
      {
         linksBuilder.addLink(((AttachedFile.Data) attachment).name().get(), attachment.toString() );
      }

      result( linksBuilder.newLinks() );
   }
}

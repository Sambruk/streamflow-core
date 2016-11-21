/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.rest.resource.organizations;

import java.util.List;

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.LinksBuilder;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.administration.surface.SelectedTemplatesContext;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;

public class SelectedTemplatesResource extends CommandQueryResource
{
   public SelectedTemplatesResource()
   {
      super( SelectedTemplatesContext.class);
   }

   public LinksValue possibledefaulttemplates(StringValue extensionFilter) throws Throwable
   {
      LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() ).command( "setdefaulttemplate" );

      List<Attachment> attachments = (List<Attachment>) context(SelectedTemplatesContext.class).possibledefaulttemplates(extensionFilter);

      for (Attachment attachment : attachments)
      {
         linksBuilder.addLink(((AttachedFile.Data) attachment).name().get(), attachment.toString() );
      }

      return linksBuilder.newLinks();
   }

   public LinksValue possibleformtemplates(StringValue extensionFilter) throws Throwable
   {
      LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() ).command( "setformtemplate" );

      List<Attachment> attachments = (List<Attachment>) context(SelectedTemplatesContext.class).possibleformtemplates(extensionFilter);

      for (Attachment attachment : attachments)
      {
         linksBuilder.addLink(((AttachedFile.Data) attachment).name().get(), attachment.toString() );
      }

      return linksBuilder.newLinks();
   }

   public LinksValue possiblecasetemplates(StringValue extensionFilter) throws Throwable
   {
      LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() ).command( "setcasetemplate" );

      List<Attachment> attachments = context(SelectedTemplatesContext.class).possiblecasetemplates(extensionFilter);

      for (Attachment attachment : attachments)
      {
         linksBuilder.addLink(((AttachedFile.Data) attachment).name().get(), attachment.toString() );
      }

      return linksBuilder.newLinks();
   }
}

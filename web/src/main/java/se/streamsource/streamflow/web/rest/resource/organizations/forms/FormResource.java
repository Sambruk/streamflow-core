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
package se.streamsource.streamflow.web.rest.resource.organizations.forms;

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResource;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.context.administration.forms.FormContext;
import se.streamsource.streamflow.web.context.administration.forms.definition.FormInfoContext;
import se.streamsource.streamflow.web.context.structure.DescribableContext;
import se.streamsource.streamflow.web.context.structure.NotableContext;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.structure.form.Forms;

/**
 * JAVADOC
 */
public class FormResource
      extends CommandQueryResource
{
   public FormResource()
   {
      super( FormContext.class);
   }

   public LinksValue possiblemoveto() throws Throwable
   {
      LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory());
      builder.command( "move" );

      for( Forms forms : context(FormContext.class).possiblemoveto() )
      {
         if(forms instanceof OrganizationEntity)
         {
            builder.addDescribable( ((Describable)forms), "" );
         } else
         {
            builder.addDescribable( (Describable) forms, ((Describable)((Ownable.Data)forms).owner().get()).getDescription() );
         }
      }

      return builder.newLinks();
   }

   @SubResource
   public void forminfo( )
   {
      subResourceContexts( FormInfoContext.class, DescribableContext.class, NotableContext.class );
   }

   @SubResource
   public void pages()
   {
      subResource(FormPagesResource.class );
   }

   @SubResource
   public void signatures()
   {
      subResource(FormSignaturesResource.class );
   }
}

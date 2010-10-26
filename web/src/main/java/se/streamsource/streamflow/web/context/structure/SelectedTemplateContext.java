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

package se.streamsource.streamflow.web.context.structure;

import static se.streamsource.dci.api.RoleMap.role;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.resource.organization.SelectedTemplateValue;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachments;
import se.streamsource.streamflow.web.domain.structure.attachment.SelectedTemplate;


/**
 * The context that handles selection/deselection of an attachment as some form of template.
 */
public class SelectedTemplateContext
{
   @Structure
   Module module;

   public SelectedTemplateValue selectedtemplate()
   {
      SelectedTemplate.Data template = role( SelectedTemplate.Data.class );
      ValueBuilder<SelectedTemplateValue> builder = module.valueBuilderFactory().newValueBuilder( SelectedTemplateValue.class );
      if (template.selectedTemplate().get() != null)
      {
         Attachment attachment = template.selectedTemplate().get();
         ValueBuilder<LinkValue> linkBuilder = module.valueBuilderFactory().newValueBuilder( LinkValue.class );
         EntityReference ref = EntityReference.getEntityReference( attachment );
         linkBuilder.prototype().text().set( ((AttachedFile.Data) attachment).name().get() );
         linkBuilder.prototype().id().set( ref.identity() );
         linkBuilder.prototype().href().set( ref.identity() );
         builder.prototype().selectedTemplate().set( linkBuilder.newInstance() );
      }

      return builder.newInstance();
   }

   public void settemplate( EntityValue dto )
   {
      SelectedTemplate template = role( SelectedTemplate.class );

      String entityReference = dto.entity().get();
      if (entityReference != null)
      {
         template.addSelectedTemplate( module.unitOfWorkFactory().currentUnitOfWork().get( Attachment.class, entityReference ) );
      } else
      {
         template.removeSelectedTemplate( ((SelectedTemplate.Data) template).selectedTemplate().get() );
      }
   }

   public LinksValue possibletemplates( StringValue extensionFilter )
   {
      LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() ).command( "settemplate" );

      Attachments.Data attachments = (Attachments.Data) role( Attachments.Data.class );
      SelectedTemplate.Data template = role( SelectedTemplate.Data.class );

      for (Attachment attachment : attachments.attachments())
      {
         if (!attachment.equals( template.selectedTemplate().get() )
               && ((AttachedFile.Data) attachment).mimeType().get().endsWith( extensionFilter.string().get() ))
         {
            linksBuilder.addLink(((AttachedFile.Data) attachment).name().get(), attachment.toString() );
         }
      }
      return linksBuilder.newLinks();
   }

}

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

package se.streamsource.streamflow.web.context.administration.surface;

import org.qi4j.api.entity.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.structure.*;
import org.qi4j.api.value.*;
import se.streamsource.dci.value.*;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.api.administration.surface.SelectedTemplatesDTO;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachments;
import se.streamsource.streamflow.web.domain.structure.attachment.CasePdfTemplate;
import se.streamsource.streamflow.web.domain.structure.attachment.DefaultPdfTemplate;
import se.streamsource.streamflow.web.domain.structure.attachment.FormPdfTemplate;

import java.util.*;

import static se.streamsource.dci.api.RoleMap.*;


/**
 * The context that handles selection/deselection of attachment as templates.
 */
public class SelectedTemplatesContext
{
   @Structure
   Module module;

   public SelectedTemplatesDTO selectedtemplates()
   {
      ValueBuilder<SelectedTemplatesDTO> builder = module.valueBuilderFactory().newValueBuilder( SelectedTemplatesDTO.class );

      DefaultPdfTemplate.Data defaultTemplate = role( DefaultPdfTemplate.Data.class );
      if (defaultTemplate.defaultPdfTemplate().get() != null)
      {
         Attachment attachment = defaultTemplate.defaultPdfTemplate().get();
         ValueBuilder<LinkValue> linkBuilder = module.valueBuilderFactory().newValueBuilder( LinkValue.class );
         EntityReference ref = EntityReference.getEntityReference( attachment );
         linkBuilder.prototype().text().set( ((AttachedFile.Data) attachment).name().get() );
         linkBuilder.prototype().id().set( ref.identity() );
         linkBuilder.prototype().href().set( ref.identity() );
         builder.prototype().defaultPdfTemplate().set( linkBuilder.newInstance() );
      }

      FormPdfTemplate.Data formTemplate = role( FormPdfTemplate.Data.class );
      if (formTemplate.formPdfTemplate().get() != null)
      {
         Attachment attachment = formTemplate.formPdfTemplate().get();
         ValueBuilder<LinkValue> linkBuilder = module.valueBuilderFactory().newValueBuilder( LinkValue.class );
         EntityReference ref = EntityReference.getEntityReference( attachment );
         linkBuilder.prototype().text().set( ((AttachedFile.Data) attachment).name().get() );
         linkBuilder.prototype().id().set( ref.identity() );
         linkBuilder.prototype().href().set( ref.identity() );
         builder.prototype().formPdfTemplate().set( linkBuilder.newInstance() );
      }

      CasePdfTemplate.Data caseTemplate = role( CasePdfTemplate.Data.class );
      if (caseTemplate.casePdfTemplate().get() != null)
      {
         Attachment attachment = caseTemplate.casePdfTemplate().get();
         ValueBuilder<LinkValue> linkBuilder = module.valueBuilderFactory().newValueBuilder( LinkValue.class );
         EntityReference ref = EntityReference.getEntityReference( attachment );
         linkBuilder.prototype().text().set( ((AttachedFile.Data) attachment).name().get() );
         linkBuilder.prototype().id().set( ref.identity() );
         linkBuilder.prototype().href().set( ref.identity() );
         builder.prototype().casePdfTemplate().set( linkBuilder.newInstance() );
      }

      return builder.newInstance();
   }

   public void setdefaulttemplate( EntityValue dto )
   {
      DefaultPdfTemplate template = role( DefaultPdfTemplate.class );

      template.setDefaultPdfTemplate( dto.entity().get() == null
            ? null
            : module.unitOfWorkFactory().currentUnitOfWork().get( Attachment.class, dto.entity().get() ) );
   }

   public void setformtemplate( EntityValue dto )
   {
      FormPdfTemplate template = role( FormPdfTemplate.class );

      template.setFormPdfTemplate( dto.entity().get() == null
            ? null
            : module.unitOfWorkFactory().currentUnitOfWork().get( Attachment.class, dto.entity().get() ) );
   }

   public void setcasetemplate( EntityValue dto )
   {
      CasePdfTemplate template = role( CasePdfTemplate.class );

      template.setCasePdfTemplate( dto.entity().get() == null
            ? null
            : module.unitOfWorkFactory().currentUnitOfWork().get( Attachment.class, dto.entity().get() ) );
   }

   public List<Attachment> possibledefaulttemplates( StringValue extensionFilter )
   {
      List<Attachment> possibleAttachments = new ArrayList<Attachment>();

      Attachments.Data attachments = (Attachments.Data) role( Attachments.Data.class );
      DefaultPdfTemplate.Data template = role( DefaultPdfTemplate.Data.class );

      for (Attachment attachment : attachments.attachments())
      {
         if (!attachment.equals( template.defaultPdfTemplate().get() )
               && ((AttachedFile.Data) attachment).mimeType().get().endsWith( extensionFilter.string().get() ))
         {
            possibleAttachments.add( attachment );
         }
      }
      return possibleAttachments;
   }

   public List<Attachment> possibleformtemplates( StringValue extensionFilter )
   {
      List<Attachment> possibleAttachments = new ArrayList<Attachment>();

      Attachments.Data attachments = (Attachments.Data) role( Attachments.Data.class );
      FormPdfTemplate.Data template = role( FormPdfTemplate.Data.class );

      for (Attachment attachment : attachments.attachments())
      {
         if (!attachment.equals( template.formPdfTemplate().get() )
               && ((AttachedFile.Data) attachment).mimeType().get().endsWith( extensionFilter.string().get() ))
         {
            possibleAttachments.add( attachment );
         }
      }
      return possibleAttachments;
   }

   public List<Attachment> possiblecasetemplates( StringValue extensionFilter )
   {
      List<Attachment> possibleAttachments = new ArrayList<Attachment>();

      Attachments.Data attachments = (Attachments.Data) role( Attachments.Data.class );
      CasePdfTemplate.Data template = role( CasePdfTemplate.Data.class );

      for (Attachment attachment : attachments.attachments())
      {
         if (!attachment.equals( template.casePdfTemplate().get() )
               && ((AttachedFile.Data) attachment).mimeType().get().endsWith( extensionFilter.string().get() ))
         {
            possibleAttachments.add( attachment );
         }
      }
      return possibleAttachments;
   }

}

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
package se.streamsource.streamflow.web.context.administration.surface;

import static se.streamsource.dci.api.RoleMap.role;

import java.util.ArrayList;
import java.util.List;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.api.administration.surface.SelectedTemplatesDTO;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachments;
import se.streamsource.streamflow.web.domain.structure.attachment.CasePdfTemplate;
import se.streamsource.streamflow.web.domain.structure.attachment.DefaultPdfTemplate;
import se.streamsource.streamflow.web.domain.structure.attachment.FormPdfTemplate;


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
       FormPdfTemplate.Data formTemplate = role( FormPdfTemplate.Data.class );
       CasePdfTemplate.Data caseTemplate = role( CasePdfTemplate.Data.class );

       // STREAMFLOW-843  make fetch of possible templates fail save against missing attachment reference
       Attachment existingDefaultTemplate = null;
       Attachment existingFormTemplate = null;
       Attachment existingCaseTemplate = null;
       try{
           existingDefaultTemplate = defaultTemplate.defaultPdfTemplate().get();
           existingFormTemplate = formTemplate.formPdfTemplate().get();
           existingCaseTemplate = caseTemplate.casePdfTemplate().get();

       } catch (NoSuchEntityException nee)
       {
           // do nothing
       }

      if (existingDefaultTemplate != null)
      {
          builder.prototype().defaultPdfTemplate().set( buildAttachementLinkValue(existingDefaultTemplate) );
      }


      if ( existingFormTemplate != null)
      {
          builder.prototype().formPdfTemplate().set( buildAttachementLinkValue( existingFormTemplate ) );
      }


      if ( existingCaseTemplate != null)
      {
          builder.prototype().casePdfTemplate().set( buildAttachementLinkValue( existingCaseTemplate ) );
      }

      return builder.newInstance();
   }

    private LinkValue buildAttachementLinkValue( Attachment attachment) {
        ValueBuilder<LinkValue> linkBuilder = module.valueBuilderFactory().newValueBuilder( LinkValue.class );
        EntityReference ref = EntityReference.getEntityReference(attachment);
        linkBuilder.prototype().text().set( ((AttachedFile.Data) attachment).name().get() );
        linkBuilder.prototype().id().set( ref.identity() );
        linkBuilder.prototype().href().set( ref.identity() );
        linkBuilder.prototype().rel().set( "pdftemplate" );
        return linkBuilder.newInstance();
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

       // STREAMFLOW-843  make fetch of possible templates fail save against missing attachment reference
       Attachment existingTemplate = null;
       try{
           existingTemplate = template.defaultPdfTemplate().get();
       } catch (NoSuchEntityException nee)
       {
           // do nothing
       }

      for (Attachment attachment : attachments.attachments())
      {
          if (!attachment.equals(existingTemplate)
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

       // STREAMFLOW-843  make fetch of possible templates fail save against missing attachment reference
       Attachment existingTemplate = null;
       try{
           existingTemplate = template.formPdfTemplate().get();
       } catch (NoSuchEntityException nee)
       {
           // do nothing
       }
      for (Attachment attachment : attachments.attachments())
      {
         if (!attachment.equals( existingTemplate )
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

       // STREAMFLOW-843  make fetch of possible templates fail save against missing attachment reference
       Attachment existingTemplate = null;
       try{
           existingTemplate = template.casePdfTemplate().get();
       } catch (NoSuchEntityException nee)
       {
           // do nothing
       }

      for (Attachment attachment : attachments.attachments())
      {
         if (!attachment.equals( existingTemplate )
               && ((AttachedFile.Data) attachment).mimeType().get().endsWith( extensionFilter.string().get() ))
         {
            possibleAttachments.add( attachment );
         }
      }
      return possibleAttachments;
   }

}

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
package se.streamsource.streamflow.web.context.administration.surface.accesspoints;

import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.library.constraints.annotation.MaxLength;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.InteractionValidation;
import se.streamsource.dci.api.RequiresValid;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.api.administration.form.RequiredSignatureValue;
import se.streamsource.streamflow.api.administration.surface.AccessPointDTO;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationVisitor;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachments;
import se.streamsource.streamflow.web.domain.structure.attachment.FormPdfTemplate;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.SelectedCaseTypes;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.MailSelectionMessage;
import se.streamsource.streamflow.web.domain.structure.form.RequiredSignatures;
import se.streamsource.streamflow.web.domain.structure.form.SelectedForms;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.domain.structure.organization.*;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.project.Projects;

import java.util.ArrayList;
import java.util.List;

import static se.streamsource.dci.api.RoleMap.*;

/**
 * JAVADOC
 */
@Concerns(AccessPointAdministrationContext.SetFormConcern.class)
@Mixins(AccessPointAdministrationContext.Mixin.class)
public interface AccessPointAdministrationContext
      extends IndexContext<AccessPointDTO>, Context, DeleteContext, InteractionValidation
{
   void changedescription( @MaxLength(50) @Name("name") String name )
         throws IllegalArgumentException;

   List<Project> possibleprojects();

   void changeproject( @Name("entity") Project project);

   List<CaseType> possiblecasetypes();

   void changecasetype( @Name("entity") CaseType caseType);

   List<Form> possibleforms();

   Iterable<Form> possiblesecondforms();

   void setform( EntityValue id );

   List<Attachment> possibleformtemplates( String filteron );

   void setformtemplate( EntityValue id );

   void changemailselectionmessage( @Optional @Name("mailmessage") String message );

   void updateprimarysignactive( @Name("active") String active );

   @RequiresValid("primarySignActive")
   void updateprimarysign( @Optional @Name("active") String active,
                           @Optional @Name("name") String name,
                           @Optional @Name("description") String description );

   @RequiresValid("primarySignActive")
   void updatesecondarysignactive( @Name("active") String active );

   @RequiresValid("secondarySignActive")
   void updatesecondarysign( @Optional @Name("active") String active,
                           @Optional @Name("name") String name,
                           @Optional @Name("description") String description,
                           @Optional @Name("formid") String formid,
                           @Optional @Name("formdescription") String formdescription,
                           @Optional @Name("mandatory") String mandatory,
                           @Optional @Name("question") String question );

   void changesubject(@Name("subject") String subject);

   void changetemplate(@Name("key") String key, @Optional @Name("template") String template);

   void changecookieexpirationhours( @Optional @Name("cookieexpirationhours") String cookieExpiration );

   abstract class Mixin
         implements AccessPointAdministrationContext
   {
      @Structure
      Module module;

      public AccessPointDTO index()
      {
         ValueBuilder<AccessPointDTO> builder = module.valueBuilderFactory().newValueBuilder( AccessPointDTO.class );

         AccessPoint accessPoint = RoleMap.role( AccessPoint.class );
         AccessPointSettings.Data accessPointData = RoleMap.role( AccessPointSettings.Data.class );
         SelectedForms.Data forms = RoleMap.role( SelectedForms.Data.class );
         Labelable.Data labelsData = RoleMap.role( Labelable.Data.class );
         FormPdfTemplate.Data template = RoleMap.role( FormPdfTemplate.Data.class );
         RequiredSignatures.Data signatures = RoleMap.role( RequiredSignatures.Data.class );
         WebAPMailTemplates.Data messages = RoleMap.role( WebAPMailTemplates.Data.class );

         builder.prototype().accessPoint().set( createLinkValue( accessPoint ) );
         if (accessPointData.project().get() != null)
            builder.prototype().project().set( createLinkValue( accessPointData.project().get() ) );
         if (accessPointData.caseType().get() != null)
            builder.prototype().caseType().set( createLinkValue( accessPointData.caseType().get() ) );

         if (forms.selectedForms().toList().size() > 0)
            builder.prototype().form().set( createLinkValue( forms.selectedForms().toList().get( 0 ) ) );

         builder.prototype().cookieExpirationHours().set(accessPointData.cookieExpirationHours().get());

         Attachment attachment = null;
         try {
               attachment = template.formPdfTemplate().get();
         } catch( NoSuchEntityException nse)
         {
            // Attachment removable concern might have failed
            // to avoid errors related to not found template - set it to null
            UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.newUsecase( "Cleanup missing form template" ) );
            AccessPoint cleanAp = uow.get( accessPoint );
            cleanAp.setFormPdfTemplate( null );

            try
            {
               uow.complete();
            } catch (UnitOfWorkCompletionException e)
            {
              throw new ResourceException( Status.CLIENT_ERROR_CONFLICT, "Form pdf attachment is missing and association cleanup failed", e  );
            }

         }

         if (attachment != null)
         {
            ValueBuilder<LinkValue> linkBuilder = module.valueBuilderFactory().newValueBuilder( LinkValue.class );
            EntityReference ref = EntityReference.getEntityReference( attachment );
            linkBuilder.prototype().text().set( ((AttachedFile.Data) attachment).name().get() );
            linkBuilder.prototype().id().set( ref.identity() );
            linkBuilder.prototype().href().set( ref.identity() );
            builder.prototype().template().set( linkBuilder.newInstance() );
         }

         builder.prototype().mailSelectionMessage().set( accessPoint.getMailSelectionMessage() );

         ValueBuilder<RequiredSignatureValue> primary = module.valueBuilderFactory().newValueBuilder( RequiredSignatureValue.class );
         ValueBuilder<RequiredSignatureValue> secondary = module.valueBuilderFactory().newValueBuilder( RequiredSignatureValue.class );

         switch ( signatures.requiredSignatures().get().size() )
         {
            case 0:
               builder.prototype().primarysign().set( primary.newInstance() );
               builder.prototype().secondarysign().set( secondary.newInstance() );
               break;
            case 1:
               builder.prototype().primarysign().set( signatures.requiredSignatures().get().get( 0 ).<RequiredSignatureValue>buildWith().newInstance() );
               builder.prototype().secondarysign().set( secondary.newInstance() );
               break;
            case 2:
               builder.prototype().primarysign().set( signatures.requiredSignatures().get().get( 0 ).<RequiredSignatureValue>buildWith().newInstance() );
               builder.prototype().secondarysign().set( signatures.requiredSignatures().get().get( 1 ).<RequiredSignatureValue>buildWith().newInstance() );
         }

         builder.prototype().subject().set( messages.subject().get() );
         builder.prototype().messages().set( messages.emailTemplates().get() );
         builder.prototype().replacementValues().set( accessPoint.hasReplacements() );

         return builder.newInstance();
      }

      private LinkValue createLinkValue( Describable describable )
      {
         ValueBuilder<LinkValue> linkBuilder = module.valueBuilderFactory().newValueBuilder( LinkValue.class );
         EntityReference ref = EntityReference.getEntityReference( describable );
         linkBuilder.prototype().text().set( describable.getDescription() );
         linkBuilder.prototype().id().set( ref.identity() );
         linkBuilder.prototype().href().set( ref.identity() );
         return linkBuilder.newInstance();
      }

      public void delete()
      {
         AccessPoint accessPoint = RoleMap.role( AccessPoint.class );
         AccessPoints accessPoints = RoleMap.role( AccessPoints.class );

         accessPoints.removeAccessPoint( accessPoint );
      }

      public void changedescription( String name )
            throws IllegalArgumentException
      {
         // check if the new description is valid
         AccessPoints.Data accessPoints = RoleMap.role( AccessPoints.Data.class );
         List<AccessPoint> accessPointsList = accessPoints.accessPoints().toList();
         for (AccessPoint accessPoint : accessPointsList)
         {
            if (accessPoint.getDescription().equals( name ))
            {
               throw new IllegalArgumentException( "accesspoint_already_exists" );
            }
         }

         RoleMap.role( AccessPoint.class ).changeDescription( name );
      }

      public List<Project> possibleprojects()
      {

         final List<Project> possibleProjects = new ArrayList<Project>();
         OrganizationQueries organizationQueries = RoleMap.role( OrganizationQueries.class );
         organizationQueries.visitOrganization( new OrganizationVisitor()
         {
            @Override
            public boolean visitProject( Project project )
            {
               possibleProjects.add( project );

               return true;
            }
         }, new OrganizationQueries.ClassSpecification( OrganizationalUnits.class, Projects.class, Project.class ) );

         return possibleProjects;
      }

      public void changeproject( @Name("entity") Project project)
      {
         AccessPoint accessPoint = RoleMap.role( AccessPoint.class );

         accessPoint.changedProject( project );
      }

      public List<CaseType> possiblecasetypes()
      {
         AccessPointSettings.Data accessPoint = RoleMap.role( AccessPointSettings.Data.class );
         Project project = accessPoint.project().get();

         List<CaseType> possibleCaseTypes = new ArrayList<CaseType>();
         if (project != null)
         {
            SelectedCaseTypes.Data data = (SelectedCaseTypes.Data) project;
            for (CaseType caseType : data.selectedCaseTypes())
            {
               possibleCaseTypes.add( caseType );
            }
         }
         return possibleCaseTypes;
      }

      public void changecasetype( @Name("entity") CaseType caseType)
      {
         AccessPoint accessPoint = RoleMap.role( AccessPoint.class );

         accessPoint.changedCaseType( caseType );
      }

      public List<Form> possibleforms()
      {
         AccessPointSettings.Data accessPoint = RoleMap.role( AccessPointSettings.Data.class );
         SelectedForms.Data selected = RoleMap.role( SelectedForms.Data.class );
         CaseType caseType = accessPoint.caseType().get();

         List<Form> possibleForms = new ArrayList<Form>();

         if (caseType != null)
         {

            List<Form> forms = ((SelectedForms.Data) caseType).selectedForms().toList();
            for (Form f : forms)
            {
               if (!selected.selectedForms().contains( f ))
               {
                  possibleForms.add( f );
               }
            }
         }

         return possibleForms;
      }

      public Iterable<Form> possiblesecondforms()
      {
         AccessPointSettings.Data accessPoint = RoleMap.role( AccessPointSettings.Data.class );
         final RequiredSignatures.Data signatures = RoleMap.role( RequiredSignatures.Data.class );
         CaseType caseType = accessPoint.caseType().get();

         return Iterables.filter( new Specification<Form>()
         {
            public boolean satisfiedBy( final Form form )
            {
               return !((Identity)form).identity().get().equals( signatures.requiredSignatures().get().get( 1 ).formid().get() );
            }
         }, ((SelectedForms.Data)caseType).selectedForms() );
      }

      public void setform( EntityValue id )
      {
         SelectedForms forms = RoleMap.role( SelectedForms.class );
         SelectedForms.Data formsData = RoleMap.role( SelectedForms.Data.class );

         // remove what's there - should only be one or none
         List<Form> selectedForms = formsData.selectedForms().toList();
         for (Form f : selectedForms)
         {
            forms.removeSelectedForm( f );
         }

         // if not null, add the last selected from
         if (id.entity().get() != null)
         {
            Form form = module.unitOfWorkFactory().currentUnitOfWork().get( Form.class, id.entity().get() );

            forms.addSelectedForm( form );
         }
      }

      public List<Attachment> possibleformtemplates( final String filteron )
      {
         final List<Attachment> possibleFormPdfTemplates = new ArrayList<Attachment>();
         OrganizationQueries organizationQueries = RoleMap.role( OrganizationQueries.class );
         final FormPdfTemplate.Data accessPoint = RoleMap.role( FormPdfTemplate.Data.class );

         organizationQueries.visitOrganization( new OrganizationVisitor()
         {
            @Override
            public boolean visitOrganization( Organization org )
            {
                // STREAMFLOW-843  make fetch of possible templates fail save against missing attachment reference
                Attachment existingFormTemplate = null;
                try
                {
                   existingFormTemplate = accessPoint.formPdfTemplate().get();
                } catch( EntityNotFoundException enf )
                {
                    //do nothing
                }
               List<Attachment> allAttachments = ((Attachments.Data) org).attachments().toList();
               for (Attachment attachment : allAttachments)
               {
                  if (!attachment.equals( existingFormTemplate )
                        && ((AttachedFile.Data) attachment).mimeType().get().endsWith( filteron ))
                  {
                     possibleFormPdfTemplates.add( attachment );
                  }
               }

               return true;
            }
         }, new OrganizationQueries.ClassSpecification( Organization.class ) );

         return possibleFormPdfTemplates;
      }

      public void setformtemplate( EntityValue id )
      {
         FormPdfTemplate accessPoint = role( FormPdfTemplate.class );

         accessPoint.setFormPdfTemplate( id.entity().get() == null ? null : module.unitOfWorkFactory().currentUnitOfWork().get( Attachment.class, id.entity().get() ) );
      }

      public void changemailselectionmessage( String message )
      {
         MailSelectionMessage role = role( MailSelectionMessage.class );
         role.changeMailSelectionMessage( message );
      }

      public void updateprimarysignactive( @Name("active") String active )
      {
         updateprimarysign( active, null, null );
      }

      public void updateprimarysign( @Optional @Name("active") String active,
                              @Optional @Name("name") String name,
                              @Optional @Name("description") String description )
      {
         SelectedForms.Data forms = role( SelectedForms.Data.class );
         RequiredSignatures.Data signaturesData = role( RequiredSignatures.Data.class );
         RequiredSignatures signatures = role( RequiredSignatures.class );

         RequiredSignatureValue primarySignature = signaturesData.requiredSignatures().get().size() > 0 ? signaturesData.requiredSignatures().get().get( 0 ) : null;

         ValueBuilder<RequiredSignatureValue> valueBuilder = null;
         if( primarySignature != null )
         {
            valueBuilder = primarySignature.buildWith();
         } else
         {
            valueBuilder = module.valueBuilderFactory().newValueBuilder( RequiredSignatureValue.class );
         }

         RequiredSignatureValue updated = valueBuilder.prototype();
         if( active != null )
         {
            updated.active().set( new Boolean( active ) );
         } else if( name != null )
         {
            updated.name().set( name );
         } else if( description != null )
         {
            updated.description().set( description );
         }

         if( forms.selectedForms().get( 0 ) != null )
         {
            updated.formid().set( ((Identity)forms.selectedForms().get( 0 )).identity().get() );
            updated.formdescription().set( forms.selectedForms().get( 0 ).getDescription() );
         }

         updated.mandatory().set( Boolean.TRUE );

         List<RequiredSignatureValue> tempList = signaturesData.requiredSignatures().get();
         if( tempList.size() > 0 )
         {
            signatures.updateRequiredSignature( 0, valueBuilder.newInstance() );
         } else
         {
            signatures.createRequiredSignature( valueBuilder.newInstance() );
         }

         // if active false and secondary is active set secondary to active false as well
         if( active != null && !new Boolean( active ).booleanValue()
               && signaturesData.requiredSignatures().get().size() > 1 )
         {
            if( signaturesData.requiredSignatures().get().get( 1 ).active().get() )
            {
               updatesecondarysign( "false", null, null, null, null, null, null );
            }

         }

      }

      public void updatesecondarysignactive( @Name("active") String active )
      {
         updatesecondarysign( active, null, null, null, null, null, null );
      }

      public void updatesecondarysign( @Optional @Name("active") String active,
                                @Optional @Name("name") String name,
                                @Optional @Name("description") String description,
                                @Optional @Name("formid") String formid,
                                @Optional @Name("formdescription") String formdescription,
                                @Optional @Name("mandatory") String mandatory,
                                @Optional @Name("question") String question )
      {
         SelectedForms.Data forms = role( SelectedForms.Data.class );
         RequiredSignatures.Data signaturesData = role( RequiredSignatures.Data.class );
         RequiredSignatures signatures = role( RequiredSignatures.class );

         RequiredSignatureValue secondarySignature = signaturesData.requiredSignatures().get().size() > 1 ? signaturesData.requiredSignatures().get().get( 1 ) : null;

         ValueBuilder<RequiredSignatureValue> valueBuilder = null;
         if( secondarySignature != null )
         {
            valueBuilder = secondarySignature.buildWith();
         } else
         {
            valueBuilder = module.valueBuilderFactory().newValueBuilder( RequiredSignatureValue.class );
         }

         RequiredSignatureValue updated = valueBuilder.prototype();
         if( active != null )
         {
            updated.active().set( new Boolean( active ) );
         } else if( name != null )
         {
            updated.name().set( name );
         } else if( description != null )
         {
            updated.description().set( description );
         } else if( formid != null && formdescription != null )
         {
            updated.formid().set( formid );
            updated.formdescription().set( formdescription );
         } else if( mandatory != null )
         {
            updated.mandatory().set( new Boolean( mandatory ) );
         } else if( question != null )
         {
            updated.question().set( question );
         }


         List<RequiredSignatureValue> tempList = signaturesData.requiredSignatures().get();

         if( tempList.size() > 1 )
         {
            signatures.updateRequiredSignature( 1, valueBuilder.newInstance() );
         } else
         {
            signatures.createRequiredSignature( valueBuilder.newInstance() );
         }
      }

      public boolean isValid( String name )
      {
         RequiredSignatures.Data signaturesData = role( RequiredSignatures.Data.class );

         List<RequiredSignatureValue> signatures = signaturesData.requiredSignatures().get();

         if( "primarySignActive".equals( name ) )
         {
            return signatures.size() > 0 && signatures.get( 0 ).active().get();
         } else if( "secondarySignActive".equals( name ) )
         {
            return signatures.size() > 1 && signatures.get( 1 ).active().get();
         }
         return false;
      }

      public void changesubject(@Name("subject") String subject)
      {
         role(WebAPMailTemplates.class).changeSubject(subject);
      }

      public void changetemplate(@Name("key") String key, @Optional @Name("template") String template)
      {
         role(WebAPMailTemplates.class).changeTemplate(key, template);
      }

      public void changecookieexpirationhours( @Optional @Name("cookieexpirationhours") String cookieExpiration )
      {
         if(Strings.empty( cookieExpiration ))
         {
            role(AccessPointSettings.class).changeCookieExpirationHours(null);
         } else {
            role(AccessPointSettings.class).changeCookieExpirationHours(Integer.parseInt(cookieExpiration));
         }
      }
   }

    abstract class SetFormConcern extends
        ConcernOf<AccessPointAdministrationContext>
        implements AccessPointAdministrationContext
    {

        public void setform(EntityValue id)
        {
            next.setform(id);
            WebAPReplacedSelectionFieldValues.Data replacements = RoleMap.role( WebAPReplacedSelectionFieldValues.Data.class );
            replacements.replacements().get().clear();
        }
    }
}
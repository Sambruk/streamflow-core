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
package se.streamsource.streamflow.web.context.administration.surface.accesspoints;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.library.constraints.annotation.MaxLength;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.api.administration.surface.AccessPointDTO;
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
import se.streamsource.streamflow.web.domain.structure.form.SelectedForms;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoint;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPointSettings;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoints;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.project.Projects;

import java.util.ArrayList;
import java.util.List;

import static se.streamsource.dci.api.RoleMap.role;

/**
 * JAVADOC
 */
@Mixins(AccessPointAdministrationContext.Mixin.class)
public interface AccessPointAdministrationContext
      extends IndexContext<AccessPointDTO>, Context, DeleteContext
{
   void changedescription( @MaxLength(50) @Name("name") String name )
         throws IllegalArgumentException;

   List<Project> possibleprojects();

   void changeproject( @Name("entity") Project project);

   List<CaseType> possiblecasetypes();

   void changecasetype( @Name("entity") CaseType caseType);

   List<Form> possibleforms();

   void setform( EntityValue id );

   List<Attachment> possibleformtemplates( StringValue extensionFilter );

   void setformtemplate( EntityValue id );

   void changemailselectionmessage( @Name("mailmessage") String message );

   void resetmailselectionmessage();

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

         builder.prototype().accessPoint().set( createLinkValue( accessPoint ) );
         if (accessPointData.project().get() != null)
            builder.prototype().project().set( createLinkValue( accessPointData.project().get() ) );
         if (accessPointData.caseType().get() != null)
            builder.prototype().caseType().set( createLinkValue( accessPointData.caseType().get() ) );

         if (forms.selectedForms().toList().size() > 0)
            builder.prototype().form().set( createLinkValue( forms.selectedForms().toList().get( 0 ) ) );

         if (template.formPdfTemplate().get() != null)
         {
            Attachment attachment = template.formPdfTemplate().get();
            ValueBuilder<LinkValue> linkBuilder = module.valueBuilderFactory().newValueBuilder( LinkValue.class );
            EntityReference ref = EntityReference.getEntityReference( attachment );
            linkBuilder.prototype().text().set( ((AttachedFile.Data) attachment).name().get() );
            linkBuilder.prototype().id().set( ref.identity() );
            linkBuilder.prototype().href().set( ref.identity() );
            builder.prototype().template().set( linkBuilder.newInstance() );
         }

         ValueBuilder<StringValue> stringBuilder = module.valueBuilderFactory().newValueBuilder( StringValue.class );
         if ( accessPoint.getMailSelectionMessage() == null )
         {
            stringBuilder.prototype().string().set(  "" );
         } else
         {
            stringBuilder.prototype().string().set(  accessPoint.getMailSelectionMessage() );
         }
         builder.prototype().mailSelectionMessage().set( stringBuilder.newInstance() );

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

         accessPoint.changedProject(project);
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

         accessPoint.changedCaseType(caseType);
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

      public List<Attachment> possibleformtemplates( final StringValue extensionFilter )
      {
         final List<Attachment> possibleFormPdfTemplates = new ArrayList<Attachment>();
         OrganizationQueries organizationQueries = RoleMap.role( OrganizationQueries.class );
         final FormPdfTemplate.Data accessPoint = RoleMap.role( FormPdfTemplate.Data.class );

         organizationQueries.visitOrganization( new OrganizationVisitor()
         {
            @Override
            public boolean visitOrganization( Organization org )
            {
               List<Attachment> allAttachments = ((Attachments.Data) org).attachments().toList();
               for (Attachment attachment : allAttachments)
               {
                  if (!attachment.equals( accessPoint.formPdfTemplate().get() )
                        && ((AttachedFile.Data) attachment).mimeType().get().endsWith( extensionFilter.string().get() ))
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

      public void changemailselectionmessage( @Name("mailmessage") String message )
      {
         MailSelectionMessage role = role( MailSelectionMessage.class );
         role.changeMailSelectionMessage( message );
      }

      public void resetmailselectionmessage()
      {
         MailSelectionMessage role = role( MailSelectionMessage.class );
         role.changeMailSelectionMessage( null );
      }
   }
}
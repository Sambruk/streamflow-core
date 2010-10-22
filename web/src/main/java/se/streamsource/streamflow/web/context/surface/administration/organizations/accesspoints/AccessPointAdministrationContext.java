/*
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

package se.streamsource.streamflow.web.context.surface.administration.organizations.accesspoints;

import org.qi4j.api.constraint.Constraints;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.library.constraints.annotation.MaxLength;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.StringValueMaxLength;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.AccessPointValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationVisitor;
import se.streamsource.streamflow.web.domain.entity.project.ProjectLabelsQueries;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.attachment.SelectedTemplate;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.SelectedCaseTypes;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.SelectedForms;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoint;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPointSettings;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoints;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.project.Projects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JAVADOC
 */
@Mixins(AccessPointAdministrationContext.Mixin.class)
@Constraints(StringValueMaxLength.class)
public interface AccessPointAdministrationContext
      extends IndexContext<AccessPointValue>, Context, DeleteContext
{
   void changedescription( @MaxLength(50) StringValue name )
         throws IllegalArgumentException;

   void setproject( StringValue id );

   void setcasetype( StringValue id );

   void setform( StringValue id );

   List<Project> possibleprojects();

   List<CaseType> possiblecasetypes();

   LinksValue possiblelabels();

   List<Form> possibleforms();

   abstract class Mixin
         implements AccessPointAdministrationContext
   {
      @Structure
      Module module;

      public AccessPointValue index()
      {
         ValueBuilder<AccessPointValue> builder = module.valueBuilderFactory().newValueBuilder( AccessPointValue.class );

         AccessPoint accessPoint = RoleMap.role( AccessPoint.class );
         AccessPointSettings.Data accessPointData = RoleMap.role( AccessPointSettings.Data.class );
         SelectedForms.Data forms = RoleMap.role( SelectedForms.Data.class );
         Labelable.Data labelsData = RoleMap.role( Labelable.Data.class );
         SelectedTemplate.Data template = RoleMap.role( SelectedTemplate.Data.class );

         builder.prototype().accessPoint().set( createLinkValue( accessPoint ) );
         if (accessPointData.project().get() != null)
            builder.prototype().project().set( createLinkValue( accessPointData.project().get() ) );
         if (accessPointData.caseType().get() != null)
            builder.prototype().caseType().set( createLinkValue( accessPointData.caseType().get() ) );

         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );
         linksBuilder.addDescribables( labelsData.labels() );

         builder.prototype().labels().set( linksBuilder.newLinks() );

         if (forms.selectedForms().toList().size() > 0)
            builder.prototype().form().set( createLinkValue( forms.selectedForms().toList().get( 0 ) ) );

         if (template.selectedTemplate().get() != null)
         {
            Attachment attachment = template.selectedTemplate().get();
            ValueBuilder<LinkValue> linkBuilder = module.valueBuilderFactory().newValueBuilder( LinkValue.class );
            EntityReference ref = EntityReference.getEntityReference( attachment );
            linkBuilder.prototype().text().set( ((AttachedFile.Data) attachment).name().get() );
            linkBuilder.prototype().id().set( ref.identity() );
            linkBuilder.prototype().href().set( ref.identity() );
            builder.prototype().template().set( linkBuilder.newInstance() );
         }

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

      public void delete() throws ResourceException
      {
         AccessPoint accessPoint = RoleMap.role( AccessPoint.class );
         AccessPoints accessPoints = RoleMap.role( AccessPoints.class );

         accessPoints.removeAccessPoint( accessPoint );
      }

      public void changedescription( StringValue name )
            throws IllegalArgumentException
      {
         // check if the new description is valid
         AccessPoints.Data accessPoints = RoleMap.role( AccessPoints.Data.class );
         List<AccessPoint> accessPointsList = accessPoints.accessPoints().toList();
         for (AccessPoint accessPoint : accessPointsList)
         {
            if (accessPoint.getDescription().equals( name.string().get() ))
            {
               throw new IllegalArgumentException( "accesspoint_already_exists" );
            }
         }

         RoleMap.role( AccessPoint.class ).changeDescription( name.string().get() );
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

      public void setproject( StringValue id )
      {
         AccessPoint accessPoint = RoleMap.role( AccessPoint.class );

         Project project = module.unitOfWorkFactory().currentUnitOfWork().get( Project.class, id.string().get() );

         accessPoint.setProject( project );
      }

      public void setcasetype( StringValue id )
      {
         AccessPoint accessPoint = RoleMap.role( AccessPoint.class );

         CaseType caseType = module.unitOfWorkFactory().currentUnitOfWork().get( CaseType.class, id.string().get() );

         accessPoint.setCaseType( caseType );
      }

      public LinksValue possiblelabels()
      {
         AccessPointSettings.Data accessPoint = RoleMap.role( AccessPointSettings.Data.class );
         Labelable.Data labelsData = RoleMap.role( Labelable.Data.class );
         Project project = accessPoint.project().get();
         CaseType caseType = accessPoint.caseType().get();

         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() ).path("labels").command( "addlabel" );
         if (project != null && caseType != null)
         {
            ProjectLabelsQueries labelsQueries = (ProjectLabelsQueries) project;


            Map<Label, SelectedLabels> map = labelsQueries.possibleLabels( caseType );
            try
            {
               List<Label> labels = labelsData.labels().toList();
               for (Label label : map.keySet())
               {
                  if (!labels.contains( label ))
                  {
                     linksBuilder.addDescribable( label, ((Describable) map.get( label )).getDescription() );
                  }
               }
            } catch (IllegalArgumentException e)
            {
               linksBuilder.addDescribables( map.keySet() );
            }
         }
         return linksBuilder.newLinks();
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

      public void setform( StringValue id )
      {
         SelectedForms forms = RoleMap.role( SelectedForms.class );
         SelectedForms.Data formsData = RoleMap.role( SelectedForms.Data.class );

         // remove what's there - should only be one or none
         List<Form> selectedForms = formsData.selectedForms().toList();
         for (Form f : selectedForms)
         {
            forms.removeSelectedForm( f );
         }

         // add the last selected from
         Form form = module.unitOfWorkFactory().currentUnitOfWork().get( Form.class, id.string().get() );

         forms.addSelectedForm( form );
      }
   }
}
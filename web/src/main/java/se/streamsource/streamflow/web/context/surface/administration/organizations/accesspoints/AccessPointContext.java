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

package se.streamsource.streamflow.web.context.surface.administration.organizations.accesspoints;

import org.qi4j.api.constraint.Constraints;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.library.constraints.annotation.MaxLength;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.SubContext;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.StringValueMaxLength;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.AccessPointValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.context.structure.labels.LabelableContext;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationVisitor;
import se.streamsource.streamflow.web.domain.entity.project.ProjectLabelsQueries;
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

import java.util.List;
import java.util.Map;

/**
 * JAVADOC
 */
@Mixins(AccessPointContext.Mixin.class)
@Constraints(StringValueMaxLength.class)
public interface AccessPointContext
      extends IndexContext<AccessPointValue>, Context, DeleteContext
{
   void changedescription( @MaxLength(50) StringValue name )
         throws IllegalArgumentException;

   void setproject( StringValue id );

   void setcasetype( StringValue id );

   void setform( StringValue id );

   LinksValue possibleprojects();

   LinksValue possiblecasetypes();

   LinksValue possiblelabels();

   @SubContext
   LabelableContext labels();

   LinksValue possibleforms();

   abstract class Mixin
         extends ContextMixin
         implements AccessPointContext
   {
      public AccessPointValue index()
      {
         ValueBuilder<AccessPointValue> builder = module.valueBuilderFactory().newValueBuilder( AccessPointValue.class );

         AccessPoint accessPoint = roleMap.get( AccessPoint.class );
         AccessPointSettings.Data accessPointData = roleMap.get( AccessPointSettings.Data.class );
         SelectedForms.Data forms = roleMap.get( SelectedForms.Data.class );
         Labelable.Data labelsData = roleMap.get( Labelable.Data.class );

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
         AccessPoint accessPoint = roleMap.get( AccessPoint.class );
         AccessPoints accessPoints = roleMap.get( AccessPoints.class );

         accessPoints.removeAccessPoint( accessPoint );
      }

      public void changedescription( StringValue name )
            throws IllegalArgumentException
      {
         // check if the new description is valid
         AccessPoints.Data accessPoints = roleMap.get( AccessPoints.Data.class );
         List<AccessPoint> accessPointsList = accessPoints.accessPoints().toList();
         for (AccessPoint accessPoint : accessPointsList)
         {
            if (accessPoint.getDescription().equals( name.string().get() ))
            {
               throw new IllegalArgumentException( "accesspoint_already_exists" );
            }
         }

         roleMap.get( AccessPoint.class ).changeDescription( name.string().get() );
      }

      public LinksValue possibleprojects()
      {

         final LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );
         OrganizationQueries organizationQueries = roleMap.get( OrganizationQueries.class );
         organizationQueries.visitOrganization( new OrganizationVisitor()
         {
            @Override
            public boolean visitProject( Project project )
            {
               linksBuilder.addDescribable( project );

               return true;
            }
         }, new OrganizationQueries.ClassSpecification( OrganizationalUnits.class, Projects.class, Project.class ) );

         return linksBuilder.newLinks();
      }

      public LinksValue possiblecasetypes()
      {
         AccessPointSettings.Data accessPoint = roleMap.get( AccessPointSettings.Data.class );
         Project project = accessPoint.project().get();

         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );
         if (project != null)
         {
            SelectedCaseTypes.Data data = (SelectedCaseTypes.Data) project;
            builder.addDescribables( data.selectedCaseTypes() );
         }
         return builder.newLinks();
      }

      public void setproject( StringValue id )
      {
         AccessPoint accessPoint = roleMap.get( AccessPoint.class );

         Project project = module.unitOfWorkFactory().currentUnitOfWork().get( Project.class, id.string().get() );

         accessPoint.setProject( project );
      }

      public void setcasetype( StringValue id )
      {
         AccessPoint accessPoint = roleMap.get( AccessPoint.class );

         CaseType caseType = module.unitOfWorkFactory().currentUnitOfWork().get( CaseType.class, id.string().get() );

         accessPoint.setCaseType( caseType );
      }

      public LinksValue possiblelabels()
      {
         AccessPointSettings.Data accessPoint = roleMap.get( AccessPointSettings.Data.class );
         Labelable.Data labelsData = roleMap.get( Labelable.Data.class );
         Project project = accessPoint.project().get();
         CaseType caseType = accessPoint.caseType().get();

         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );
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

      public LinksValue possibleforms()
      {
         AccessPointSettings.Data accessPoint = roleMap.get( AccessPointSettings.Data.class );
         SelectedForms.Data selected = roleMap.get( SelectedForms.Data.class );
         CaseType caseType = accessPoint.caseType().get();

         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );

         if (caseType != null)
         {

            List<Form> forms = ((SelectedForms.Data) caseType).selectedForms().toList();
            for (Form f : forms)
            {
               if (!selected.selectedForms().contains( f ))
               {
                  builder.addDescribable( f );
               }
            }
         }

         return builder.newLinks();
      }

      public void setform( StringValue id )
      {
         SelectedForms forms = roleMap.get( SelectedForms.class );
         SelectedForms.Data formsData = roleMap.get( SelectedForms.Data.class );

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

      public LabelableContext labels()
      {
         return subContext( LabelableContext.class );
      }
   }
}
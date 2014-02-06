/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.web.context.workspace;

import org.qi4j.api.entity.Entity;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.priority.PriorityValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.SelectedCaseTypes;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;
import se.streamsource.streamflow.web.domain.structure.organization.Priorities;
import se.streamsource.streamflow.web.domain.structure.organization.Priority;
import se.streamsource.streamflow.web.domain.structure.organization.PrioritySettings;
import se.streamsource.streamflow.web.domain.structure.project.Project;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;


/**
 * General interface for fetching filter data for contexts related through project i.e. Inbox and Assignment
 */
@Mixins(AbstractFilterContext.Mixin.class)
public interface AbstractFilterContext extends Context
{
   public LinksValue possibleCaseTypes();

   public LinksValue possibleLabels();

   LinksValue priorities();

   OrderBy.Order revertSortOrder( OrderBy.Order order );

   abstract class Mixin
           implements AbstractFilterContext
   {
      @Structure
      Module module;

      public LinksValue possibleLabels()
      {
         // Fetch all labels from CaseType's ---> Organization
         HashSet<Label> labels = new HashSet<Label>();

         LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory());
         Project project = RoleMap.role(Project.class);

         // labels from project's selected case types
         for (CaseType caseType : ((SelectedCaseTypes.Data) project).selectedCaseTypes())
         {
            labels.addAll(((SelectedLabels.Data) caseType).selectedLabels().toSet());
         }

         // project's selected labels
         labels.addAll(((SelectedLabels.Data) project).selectedLabels().toSet());


         // OU hirarchy labels from bottom up
         Entity entity = (Entity) ((Ownable.Data) project).owner().get();

         while (entity instanceof Ownable)
         {
            labels.addAll(((SelectedLabels.Data) entity).selectedLabels().toSet());
            entity = (Entity) ((Ownable.Data) entity).owner().get();
         }
         // Organization's selected labels
         labels.addAll(((SelectedLabels.Data) entity).selectedLabels().toSet());

         for (Label label : labels)
         {
            builder.addDescribable( (Describable) label, "" );
         }
         return builder.newLinks();
      }

      public LinksValue possibleCaseTypes()
      {
         Project project = RoleMap.role(Project.class);
         SelectedCaseTypes.Data selectedCaseTypes = (SelectedCaseTypes.Data) project;

         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );

         for( CaseType caseType : selectedCaseTypes.selectedCaseTypes() )
         {
            Owner owner = ((Ownable.Data)caseType).owner().get();

            String title = owner != null ? ((Describable)owner).getDescription() : "";
            linksBuilder.addLink( caseType.getDescription(), ((Identity)caseType).identity().get(),"","","", title );
         }
         return linksBuilder.newLinks();
      }

      public LinksValue priorities()
      {
         Organizations.Data orgs = module.unitOfWorkFactory().currentUnitOfWork().get( OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID );
         Organization org = orgs.organization().get();
         RoleMap.current().set( org );

         Priorities.Data priorities = RoleMap.role( Priorities.Data.class );
         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );
         ValueBuilder<PriorityValue> linkBuilder = module.valueBuilderFactory().newValueBuilder( PriorityValue.class );

         List<Priority> sortedList =  priorities.prioritys().toList();
         Collections.sort( sortedList, new Comparator<Priority>()
         {
            public int compare( Priority o1, Priority o2 )
            {
               return ((PrioritySettings.Data) o1).priority().get().compareTo( ((PrioritySettings.Data) o2).priority().get() );
            }
         } );

         for(Priority priority : sortedList )
         {
            linkBuilder.prototype().id().set( EntityReference.getEntityReference( priority ).identity() );
            linkBuilder.prototype().color().set( ((PrioritySettings.Data)priority).color().get() );
            linkBuilder.prototype().priority().set( ((PrioritySettings.Data)priority).priority().get() );
            linkBuilder.prototype().href().set( "na" );
            linkBuilder.prototype().text().set( priority.getDescription() );
            builder.addLink( linkBuilder.newInstance() );
         }
         return builder.newLinks();
      }

      public OrderBy.Order revertSortOrder( OrderBy.Order order )
      {
         if( OrderBy.Order.ASCENDING.equals( order ))
            return OrderBy.Order.DESCENDING;
         else
            return OrderBy.Order.ASCENDING;
      }
   }
}

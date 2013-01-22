/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.web.context.administration.labels;

import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.api.ServiceAvailable;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.application.knowledgebase.KnowledgebaseService;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.label.LabelEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseTypes;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labels;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;

import static org.qi4j.api.query.QueryExpressions.*;
import static se.streamsource.dci.api.RoleMap.*;

/**
 * JAVADOC
 */
public class LabelContext
      implements DeleteContext
{
   @Structure
   Module module;

   @Service
   KnowledgebaseService knowledgebaseService;

   public LinksValue usages()
   {
      Iterable<SelectedLabels> selectedLabels = role( Labels.class ).usages( role( Label.class ) );
      LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );

      for( SelectedLabels labels : selectedLabels )
      {
         if( labels instanceof OrganizationEntity )
         {
            builder.addDescribable( (Describable) labels, "" );

         } else
         {
            if( !(((Removable.Data)((Ownable.Data)labels).owner().get()).removed().get()) )
               builder.addDescribable( (Describable) labels, ((Describable)((Ownable.Data)labels).owner().get()).getDescription() );
         }
      }
      return builder.newLinks();
   }

   public void delete()
   {
      Labels labels = role( Labels.class );
      Label label = role( Label.class );
      Iterable<SelectedLabels> usages = labels.usages( label );

      // Remove selections
      for (SelectedLabels selectedLabels : usages )
      {
         selectedLabels.removeSelectedLabel( label );
      }

      labels.removeLabel( label );
   }

   public Iterable<Labels> possiblemoveto()
   {
      final Labels thisLabels = role(Labels.class);

      return Iterables.filter( new Specification<Labels>()
      {
         public boolean satisfiedBy( Labels item )
         {
            Owner owner = ((Ownable.Data)item).owner().get();

            return !item.equals( thisLabels ) && !((Removable.Data)owner).removed().get();
         }
      }, module.queryBuilderFactory().newQueryBuilder( Labels.class )
            .where( and(
                  eq( templateFor( Removable.Data.class ).removed(), false ),
                  QueryExpressions.isNotNull( templateFor( Ownable.Data.class ).owner() )
            ) )
            .newQuery( module.unitOfWorkFactory().currentUnitOfWork() ) );
   }

   public void move( EntityValue to )
   {
      Labels toLabels = module.unitOfWorkFactory().currentUnitOfWork().get( Labels.class, to.entity().get() );
      Label label = RoleMap.role( Label.class );
      RoleMap.role( Labels.class ).moveLabel(label, toLabels );

      RoleMap.current().set( RoleMap.role( CaseTypes.class ), Identity.class );
   }

   @ServiceAvailable( service = KnowledgebaseService.class, availability = true )
   public LinkValue knowledgeBase()
   {
      LabelEntity label = RoleMap.role(LabelEntity.class);
      ValueBuilder<LinkValue> builder = module.valueBuilderFactory().newValueBuilder(LinkValue.class);
      builder.prototype().id().set(label.identity().get());
      builder.prototype().text().set(label.getDescription());
      builder.prototype().rel().set("knowledgebase");
      builder.prototype().href().set(knowledgebaseService.createURL(label));
      return builder.newInstance();
   }
}

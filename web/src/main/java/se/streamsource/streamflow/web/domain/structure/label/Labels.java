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
package se.streamsource.streamflow.web.domain.structure.label;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.entity.label.LabelEntity;

/**
 * JAVADOC
 */
@Mixins(Labels.Mixin.class)
public interface Labels
{
   Label createLabel( String name );

   void addLabel(Label label);

   void removeLabel( Label label );

   void moveLabel( Label label, Labels toLabels );

   void mergeLabels( Labels to );

   Query<SelectedLabels> usages( Label label );

   interface Data
   {
      @Aggregated
      ManyAssociation<Label> labels();

      Label createdLabel( @Optional DomainEvent event, String identity );

      void addedLabel( @Optional DomainEvent event, Label label );

      void removedLabel( @Optional DomainEvent event, Label label );
   }

   abstract class Mixin
         implements Labels, Data
   {
      @Service
      IdentityGenerator idGen;

      @Structure
      Module module;

      @This
      Data data;

      public Label createLabel( String name )
      {
         Label label = data.createdLabel( null, idGen.generate( LabelEntity.class ));
         label.changeDescription( name );
         return label;
      }

      public void addLabel( Label label )
      {
         if (!data.labels().contains( label ))
            addedLabel(null, label);
      }

      public void removeLabel( Label label )
      {
         if (data.labels().contains( label ))
         {
            removedLabel( null, label );
            label.removeEntity();
         }
      }

      public void moveLabel( Label label, Labels toLabels )
      {
         if (data.labels().contains( label ))
         {
            toLabels.addLabel( label );

            removedLabel( null, label );
         }
      }

      public void mergeLabels( Labels to )
      {
         while (data.labels().count() > 0)
         {
            Label label = data.labels().get( 0 );
            removedLabel( null, label );
            to.addLabel( label );
         }
      }

      public Query<SelectedLabels> usages( Label label )
      {
         SelectedLabels.Data selectedLabels = QueryExpressions.templateFor( SelectedLabels.Data.class );
         Query<SelectedLabels> labelUsages = module.queryBuilderFactory().newQueryBuilder(SelectedLabels.class).
               where( QueryExpressions.contains( selectedLabels.selectedLabels(), label ) ).
               newQuery( module.unitOfWorkFactory().currentUnitOfWork() );

         return labelUsages;
      }

      public Label createdLabel( DomainEvent event, String identity )
      {
         UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();
         Label label = uow.newEntity( Label.class, identity );
         data.labels().add( data.labels().count(), label );
         return label;
      }

      public void addedLabel( DomainEvent event, Label label )
      {
         data.labels().add( label );
      }

      public void removedLabel( @Optional DomainEvent event, Label label )
      {
         data.labels().remove( label );
      }
   }
}

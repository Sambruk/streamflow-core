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
package se.streamsource.streamflow.web.domain.structure.form;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.form.FormEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.ChangesOwner;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;

import static org.qi4j.api.query.QueryExpressions.*;

/**
 * JAVADOC
 */
@Mixins(Forms.Mixin.class)
public interface Forms
{
   Form createForm();

   @ChangesOwner
   void addForm( Form form );

   void removeForm( Form form );

   void moveForm( Form form, Forms toForms );

   void mergeForms( Forms to );

   // Queries
   Query<SelectedForms> usages( Form form );

   interface Data
   {
      @Aggregated
      ManyAssociation<Form> forms();

      Form createdForm( @Optional DomainEvent event, String id);

      void addedForm( @Optional DomainEvent create, Form form );

      void removedForm( @Optional DomainEvent event, Form removedForm );
   }

   abstract class Mixin
         implements Forms, Data
   {
      @Service
      IdentityGenerator idGen;

      @Structure
      Module module;

      @This
      Data data;

      public Form createForm()
      {
         Form form = createdForm( null, idGen.generate( Identity.class ) );
         addForm(form);

         return form;
      }

      public void addForm( Form form )
      {
         if (data.forms().contains( form ))
            return;

         data.addedForm(null, form);
      }

      public void removeForm( Form form )
      {
         if (data.forms().contains( form ))
         {
            removedForm( null, form );
            form.removeEntity();
         }
      }

      public void moveForm( Form form, Forms toForms )
      {
         toForms.addForm(form);

         removedForm(null, form);
      }

      public Form createdForm( @Optional DomainEvent event, String id )
      {
         EntityBuilder<FormEntity> builder = module.unitOfWorkFactory().currentUnitOfWork().newEntityBuilder( FormEntity.class, id );
         builder.instance().formId().set( "form"+(forms().count()+1) );
         Form form = builder.newInstance();
         data.forms().add( form );
         return form;
      }

      public void removedForm( @Optional DomainEvent event, Form removedForm )
      {
         data.forms().remove( removedForm );
      }

      public void mergeForms( Forms to )
      {
         while (data.forms().count() > 0)
         {
            Form form = data.forms().get( 0 );
            removedForm( null, form );
            to.addForm( form );
         }
      }

      public Query<SelectedForms> usages( Form form )
      {
         SelectedForms.Data selectedForms = QueryExpressions.templateFor( SelectedForms.Data.class );
         Query<SelectedForms> formUsages = module.queryBuilderFactory().newQueryBuilder( SelectedForms.class ).
               where( and(
                     eq( templateFor( Removable.Data.class ).removed(), false ),
                     isNotNull( templateFor( Ownable.Data.class ).owner() ),
                     contains( selectedForms.selectedForms(), form ) )
                     ).
                     newQuery( module.unitOfWorkFactory().currentUnitOfWork() );

         return formUsages;
      }
   }
}

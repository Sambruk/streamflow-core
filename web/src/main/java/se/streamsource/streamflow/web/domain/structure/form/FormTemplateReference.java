/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.domain.structure.form;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

import java.util.Date;

/**
 * Reference from form definition to a template.
 */
@Mixins(FormTemplateReference.Mixin.class)
public interface FormTemplateReference
{
   void synchronizeWithTemplate();

   interface Data
   {
      @Optional
      Association<FormTemplate> template();

      @Optional
      Property<Date> lastSynchronization();

      void formSynchronized( DomainEvent event );

      void removedTemplateReference( DomainEvent create );
   }

   abstract class Mixin
         implements FormTemplateReference, Data
   {
      @This
      Pages.Data pagesData;

      public void synchronizeWithTemplate()
      {
         // First remove all local fields
         for (Page page : pagesData.pages())
         {
            pagesData.removedPage( DomainEvent.CREATE, page );
         }

         // Copy fields from template
         Pages.Data templatePages = (Pages.Data) template().get();
         for (Page page : templatePages.pages())
         {
            Fields.Data templateFields = (Fields.Data) template().get();
            for (Field templateField : templateFields.fields())
            {
               Field field = page.createField( templateField.getDescription(), ((FieldValueDefinition.Data) templateField).fieldValue().get() );
               field.copyFromTemplate( templateField );
            }
            // TODO What else should be copied? Description? Note?
         }


         formSynchronized( DomainEvent.CREATE );
      }

      public void formSynchronized( DomainEvent event )
      {
         lastSynchronization().set( event.on().get() );
      }

      public void removedTemplateReference( DomainEvent create )
      {
         template().set( null );
         lastSynchronization().set( null );
      }
   }
}

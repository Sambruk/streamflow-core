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

package se.streamsource.streamflow.web.domain.structure.attachment;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * Handles selection of an attachment as some form of template.
 */
@Mixins(SelectedTemplate.Mixin.class)
public interface SelectedTemplate
{
   void addSelectedTemplate( Attachment attachment );

   void removeSelectedTemplate( @Optional Attachment attachment );

   void addCaseTemplate( Attachment attachment );

   void removeCaseTemplate( @Optional Attachment attachment );

   interface Data
   {
      @Optional
      Association<Attachment> selectedTemplate();

      @Optional
      Association<Attachment> caseTemplate();

      void selectedTemplateAdded( DomainEvent event, Attachment attachment );

      void selectedTemplateRemoved( DomainEvent event, Attachment attachment );

      void caseTemplateAdded( DomainEvent event, Attachment attachment );

      void caseTemplateRemoved( DomainEvent event, Attachment attachment );
   }

   abstract class Mixin
         implements SelectedTemplate, Data
   {
      @Structure
      ValueBuilderFactory vbf;

      @This
      Data data;

      public void addSelectedTemplate( Attachment attachment )
      {
         selectedTemplateAdded( DomainEvent.CREATE, attachment );
      }

      public void removeSelectedTemplate( Attachment attachment )
      {
         if (data.selectedTemplate().get() != null)
            selectedTemplateRemoved( DomainEvent.CREATE, attachment );
      }

      public void selectedTemplateAdded( DomainEvent event, Attachment attachment )
      {
         selectedTemplate().set( attachment );
      }

      public void selectedTemplateRemoved( DomainEvent event, Attachment attachment )
      {
         selectedTemplate().set( null );
      }

      public void addCaseTemplate( Attachment attachment )
      {
         caseTemplateAdded( DomainEvent.CREATE, attachment );
      }

      public void removeCaseTemplate( Attachment attachment )
      {
         if (data.caseTemplate().get() != null)
            caseTemplateRemoved( DomainEvent.CREATE, attachment );
      }

      public void caseTemplateAdded( DomainEvent event, Attachment attachment )
      {
         caseTemplate().set( attachment );
      }

      public void caseTemplateRemoved( DomainEvent event, Attachment attachment )
      {
         caseTemplate().set( null );
      }
   }
}
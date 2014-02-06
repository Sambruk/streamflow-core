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
package se.streamsource.streamflow.web.domain.structure.attachment;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * Handles selection of an attachment as default pdf template.
 */
@Mixins(DefaultPdfTemplate.Mixin.class)
public interface DefaultPdfTemplate
{
   void setDefaultPdfTemplate( @Optional Attachment attachment );

   interface Data
   {
      @Optional
      Association<Attachment> defaultPdfTemplate();

      void defaultPdfTemplateSet( @Optional DomainEvent event, @Optional Attachment attachment );
   }

   abstract class Mixin
         implements DefaultPdfTemplate, Data
   {
      @This
      Data data;

      public void setDefaultPdfTemplate( Attachment attachment )
      {
         defaultPdfTemplateSet( null, attachment );
      }

      public void defaultPdfTemplateSet( @Optional DomainEvent event, Attachment attachment )
      {
         defaultPdfTemplate().set( attachment );
      }
   }
}
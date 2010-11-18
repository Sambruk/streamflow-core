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
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * Handles selection of an attachment as pdf template for Case.
 */
@Mixins(CasePdfTemplate.Mixin.class)
public interface CasePdfTemplate
{
   void setCasePdfTemplate( @Optional Attachment attachment );

   interface Data
   {
      @Optional
      Association<Attachment> casePdfTemplate();

      void casePdfTemplateSet( @Optional DomainEvent event, @Optional Attachment attachment );
   }

   abstract class Mixin
         implements CasePdfTemplate, Data
   {
      @Structure
      ValueBuilderFactory vbf;

      @This
      Data data;

      public void setCasePdfTemplate( Attachment attachment )
      {
         casePdfTemplateSet( null, attachment );
      }

      public void casePdfTemplateSet( @Optional DomainEvent event, Attachment attachment )
      {
         casePdfTemplate().set( attachment );
      }
   }
}
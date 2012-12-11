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
package se.streamsource.streamflow.web.domain.structure.task;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.form.FormDraft;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedFormValue;

/**
 * 
 *
 */
@Mixins(DoubleSignatureTask.Mixin.class)
public interface DoubleSignatureTask
{

   void update( @Optional Case caze );
   void update( @Optional SubmittedFormValue submittedFormValue );
   void update( @Optional FormDraft formDraft );

   interface Data
   {
      Association<Case> caze();

      Property<SubmittedFormValue> submittedForm();

      Association<FormDraft> formDraft();

      void updated( @Optional DomainEvent event, @Optional Case caze );
      void updated( @Optional DomainEvent event, @Optional SubmittedFormValue submittedFormValue );
      void updated( @Optional DomainEvent event, @Optional FormDraft formDraft);

   }

   abstract class Mixin
         implements DoubleSignatureTask, Data
   {

      @This
      Data data;

      public void update( Case caze )
      {
         updated( null, caze );
      }

      public void updated( DomainEvent event, Case caze )
      {
         data.caze().set( caze );
      }

      public void update( SubmittedFormValue submittedFormValue )
      {
         updated( null, submittedFormValue );
      }

      public void updated( DomainEvent event, SubmittedFormValue submittedFormValue )
      {
         data.submittedForm().set( submittedFormValue );
      }

      public void update( FormDraft formDraft )
      {
         updated( null, formDraft );
      }

      public void updated( DomainEvent event, FormDraft formDraft )
      {
         data.formDraft().set( formDraft );
      }
   }

}

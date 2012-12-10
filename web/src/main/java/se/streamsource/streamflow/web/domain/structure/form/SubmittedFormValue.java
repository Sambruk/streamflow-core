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

import java.util.Date;
import java.util.List;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.util.Function;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueComposite;

import se.streamsource.streamflow.api.workspace.cases.general.FormSignatureDTO;
import se.streamsource.streamflow.api.workspace.cases.general.SecondSigneeInfoValue;
import se.streamsource.streamflow.web.domain.structure.SubmittedFieldValue;

/**
 * JAVADOC
 */
@Mixins(SubmittedFormValue.Mixin.class)
public interface SubmittedFormValue
      extends ValueComposite
{
   Property<EntityReference> submitter();

   Property<Date> submissionDate();

   Property<EntityReference> form();

   @UseDefaults
   Property<List<SubmittedPageValue>> pages();

   @UseDefaults
   Property<List<FormSignatureDTO>> signatures();

   @Optional
   Property<SecondSigneeInfoValue> secondsignee();
   
   Iterable<SubmittedFieldValue> fields();
   
   abstract class Mixin implements SubmittedFormValue
   {
      public Iterable<SubmittedFieldValue> fields()
      {
         return Iterables.flatten( Iterables.map( new Function<SubmittedPageValue, Iterable<SubmittedFieldValue>>()
         {
            public Iterable<SubmittedFieldValue> map(SubmittedPageValue submittedPage)
            {
               return submittedPage.fields().get();
            }
         }, pages().get() ) );
      }
   }
}

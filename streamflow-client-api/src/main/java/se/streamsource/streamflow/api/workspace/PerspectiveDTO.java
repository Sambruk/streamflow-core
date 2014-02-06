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
package se.streamsource.streamflow.api.workspace;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.library.constraints.annotation.MaxLength;

import java.util.Date;
import java.util.List;


public interface PerspectiveDTO
      extends ValueComposite
{
   @MaxLength(50)
   Property<String> name();

   @Optional
   Property<String> query();
   
   Property<List<String>> statuses();
   
   Property<List<String>> caseTypes();
   
   Property<List<String>> labels();
   
   Property<List<String>> assignees();

   Property<List<String>> projects();
   
   Property<List<String>> createdBy();
   
   Property<String> sortBy();

   Property<String> sortOrder();

   Property<String> groupBy();

   Property<String> createdOnPeriod();

   @Optional
   Property<Date> createdOn();

   Property<String> dueOnPeriod();

   @Optional
   Property<Date> dueOn();

   @UseDefaults
   Property<String> context();

   Property<List<Integer>> invisibleColumns();
   
}
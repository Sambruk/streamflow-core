/**
 *
 * Copyright 2009-2011 Streamsource AB
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
package se.streamsource.streamflow.web.domain.structure.caselog;

import java.util.Date;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

import se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogEntryTypes;

public interface CaseLogEntryValue extends ValueComposite
{
   
   Property<String> message();
   
   @Optional
   Property<EntityReference> entity();
   
   Property<CaseLogEntryTypes> entryType();
   
   @UseDefaults()
   Property<Boolean> availableOnMypages();

   @Immutable
   Property<Date> createdOn();

   @Optional
   Property<EntityReference> createdBy();
}

/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
package se.streamsource.streamflow.web.application.statistics;

import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

/**
 * Representation of the OU structure. This uses the Nested Dataset concept, so that
 * it is possible to do nested SQL queries for the case data.
 */
public interface OrganizationalUnitValue
   extends ValueComposite
{
   Property<String> name();
   Property<String> id();
   Property<Integer> left();
   Property<Integer> right();
   @Optional
   Property<String> parent();
}

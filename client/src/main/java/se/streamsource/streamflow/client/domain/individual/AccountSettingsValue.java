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
package se.streamsource.streamflow.client.domain.individual;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.library.constraints.annotation.Matches;
import se.streamsource.streamflow.api.Username;

/**
 * JAVADOC
 */
public interface AccountSettingsValue
      extends ValueComposite
{
   @UseDefaults
   Property<String> name();

   @UseDefaults
   @Matches("[\\w:/\\.\\-]*")
   Property<String> server();

   @Username
   Property<String> userName();


   Property<String> password();
}

/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.dci.api;

import org.qi4j.spi.structure.ModuleSPI;

import java.lang.reflect.Method;

/**
 * Service interface for checking whether a particular method or a whole class is not
 * valid at this point, for whatever reason (application state or authorization rules usually).
 */
public interface InteractionConstraints
{
   public boolean isValid( Method method, RoleMap roleMap, ModuleSPI module );
   public boolean isValid( Class resourceClass, RoleMap roleMap, ModuleSPI module );
}

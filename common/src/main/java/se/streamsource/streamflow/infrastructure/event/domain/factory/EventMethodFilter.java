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
package se.streamsource.streamflow.infrastructure.event.domain.factory;

import org.qi4j.api.common.AppliesToFilter;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

import java.lang.reflect.Method;

/**
 * Filter for Event methods. Event methods
 * have DomainEvent as their first method parameter.
 */
public class EventMethodFilter
      implements AppliesToFilter
{
   public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass )
   {
      return method.getParameterTypes().length > 0 && method.getParameterTypes()[0].equals( DomainEvent.class );
   }
}

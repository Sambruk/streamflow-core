/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.web.domain.interaction.security;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

import java.util.Map;

import static java.util.Arrays.*;
import static se.streamsource.streamflow.web.domain.interaction.security.CaseAccessType.*;

/**
 * Containing the option representing whether the
 * security setting has been selected for this case
 */
@Mixins(CaseAccessSecurityApplies.Mixin.class)
public interface CaseAccessSecurityApplies
{
   void setSecrecySetting( Boolean enable );

   interface Data
   {
      @UseDefaults
      Property<Boolean> secrecyApplies();

      void secrecyChanged( @Optional DomainEvent event, Boolean security );
   }

   abstract class Mixin
      implements CaseAccessSecurityApplies, Data
   {
      public void setSecrecySetting( Boolean enable )
      {
         if ( secrecyApplies().get() != enable ) {
            secrecyChanged(  null, enable );
         }
      }

      public void secrecyChanged( @Optional DomainEvent event, Boolean security )
      {
         secrecyApplies().set(  security );
      }
   }
}

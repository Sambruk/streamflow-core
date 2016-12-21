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
package se.streamsource.streamflow.web.domain.structure.form;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * Technical type for a field. May not be empty
 */
@Mixins(Datatype.Mixin.class)
public interface Datatype
{
   void changeDatatype( @Optional DatatypeDefinition newType);

   interface Data
   {
      @Optional
      Association<DatatypeDefinition> datatype();
   }
   
   interface Events
   {
      void changedDatatype( @Optional DomainEvent event, @Optional DatatypeDefinition newType );
   }

   class Mixin
      implements Datatype, Events
   {
      @This
      Data data;

      public void changeDatatype( @Optional DatatypeDefinition newType)
      {
         changedDatatype( null, newType );
      }
      
      public void changedDatatype( DomainEvent event, @Optional DatatypeDefinition newType )
      {
         data.datatype().set( newType );
      }
   }

}
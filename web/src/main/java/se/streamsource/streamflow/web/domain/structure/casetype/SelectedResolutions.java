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
package se.streamsource.streamflow.web.domain.structure.casetype;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.mixin.Mixins;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(SelectedResolutions.Mixin.class)
public interface SelectedResolutions
{
   void addSelectedResolution( Resolution resolution );

   void removeSelectedResolution( Resolution resolution );

   boolean hasSelectedResolutions( );

   Iterable<Resolution> getSelectedResolutions();

   interface Data
   {
      ManyAssociation<Resolution> selectedResolutions();

      void addedSelectedResolution( @Optional DomainEvent event, Resolution resolution );

      void removedSelectedResolution( @Optional DomainEvent event, Resolution resolution );
   }

   abstract class Mixin
         implements SelectedResolutions, Data
   {
      public void addSelectedResolution( Resolution resolution )
      {
         addedSelectedResolution( null, resolution );
      }

      public void removeSelectedResolution( Resolution resolution )
      {
         removedSelectedResolution( null, resolution );
      }

      public boolean hasSelectedResolutions( )
      {
         return selectedResolutions().count() > 0;
      }

      public Iterable<Resolution> getSelectedResolutions()
      {
         return selectedResolutions();
      }

      public void addedSelectedResolution( DomainEvent event, Resolution resolution )
      {
         selectedResolutions().add( resolution );
      }

      public void removedSelectedResolution( DomainEvent event, Resolution resolution )
      {
         selectedResolutions().remove( resolution );
      }
   }
}

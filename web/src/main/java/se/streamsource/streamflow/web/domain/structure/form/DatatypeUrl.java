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
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * Role for urls of entities.
 */
@Mixins(DatatypeUrl.Mixin.class)
public interface DatatypeUrl
{
  void changeUrl( @Optional String newUrl );

  String getUrl();

  interface Data
  {
     @Immutable
     Property<String> url();

     void changedUrl( @Optional DomainEvent event, String url );
  }

  public abstract class Mixin
        implements DatatypeUrl, Data
  {
     public void changeUrl( String newUrl )
     {
        if (!newUrl.equals( url().get() ))
           changedUrl( null, newUrl );
     }

     public String getUrl()
     {
        return url().get();
     }

     // State

     public void changedUrl( @Optional DomainEvent event, String url )
     {
        url().set( url );
     }
  }
}

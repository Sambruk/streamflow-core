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

package se.streamsource.streamflow.web.domain.structure.form;

import org.qi4j.api.common.Optional;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * Role for maintaining descriptions of entities.
 */
@Mixins(DataTypeUrl.Mixin.class)
public interface DataTypeUrl
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
        implements DataTypeUrl, Data
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

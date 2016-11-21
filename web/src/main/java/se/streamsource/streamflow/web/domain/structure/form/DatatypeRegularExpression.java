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
import org.qi4j.api.property.Property;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.util.Strings;

/**
 * Role for regexp-patterns of entities.
 */
@Mixins(DatatypeRegularExpression.Mixin.class)
public interface DatatypeRegularExpression
{
  void changeRegularExpression( @Optional String newExpression );

  String getRegularExpression();

  interface Data
  {
     @Optional
     Property<String> regularexpression();
  }
  
  interface Events
  {
     void changedRegularExpression( @Optional DomainEvent event, String regularexpression );
  }

  public abstract class Mixin
        implements DatatypeRegularExpression, Data, Events
  {
     public void changeRegularExpression( String newExpression )
     {
        if (Strings.empty( newExpression ))
        {
           if (!Strings.empty(regularexpression().get()))
           {
              changedRegularExpression( null, newExpression );   
           }
        } else if (!newExpression.equals(regularexpression().get()))
        {
           changedRegularExpression( null, newExpression );   
        }
     }

     public String getRegularExpression()
     {
        return regularexpression().get();
     }

     public void changedRegularExpression( @Optional DomainEvent event, String expression )
     {
        regularexpression().set( expression );
     }
  }
}

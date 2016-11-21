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
package se.streamsource.streamflow.web.context.structure;

import org.qi4j.api.constraint.Constraints;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.library.constraints.annotation.MaxLength;

import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.StringValueMaxLength;
import se.streamsource.streamflow.web.domain.Describable;

/**
 * JAVADOC
 */
@Mixins(DescribableContext.Mixin.class)
@Constraints(StringValueMaxLength.class)
public interface DescribableContext
   extends Context
{
   public void changedescription( @MaxLength(50) StringValue stringValue );

   abstract class Mixin
         implements DescribableContext
   {
      public void changedescription( StringValue stringValue )
      {
         Describable describable = RoleMap.role( Describable.class );
         describable.changeDescription( stringValue.string().get() );
      }
   }
}

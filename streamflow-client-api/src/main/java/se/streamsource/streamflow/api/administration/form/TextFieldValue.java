/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.api.administration.form;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.util.Strings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JAVADOC
 */
@Mixins( TextFieldValue.Mixin.class )
public interface TextFieldValue
      extends FieldValue
{
   Property<Integer> width();

   @Optional
   Property<String> regularExpression();

   @Optional
   Property<String> hint();

   @UseDefaults
   Property<Boolean> mandatory();

   abstract class Mixin
      implements FieldValue
   {
      @This TextFieldValue definition;

      public Boolean validate( String value )
      {
         if (!Strings.empty( value ))
         {
            if (!Strings.empty( definition.regularExpression().get() ))
            {
               if (value != null)
               {
                  Pattern pattern = Pattern.compile( definition.regularExpression().get() );
                  Matcher matcher = pattern.matcher( value );

                  return matcher.matches();
               }
               return false;
            }
            return true;
         } else {
            return !definition.mandatory().get();
         }

      }
   }
}
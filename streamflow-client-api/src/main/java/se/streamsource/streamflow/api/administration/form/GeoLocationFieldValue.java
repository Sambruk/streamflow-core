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

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

/**
 * Handles map coordinates. Single point, polyline or polygon.
 * 
 */
@Mixins( GeoLocationFieldValue.Mixin.class )
public interface GeoLocationFieldValue
      extends SelectionFieldValue
{

   @UseDefaults
   Property<Boolean> point();

   @UseDefaults
   Property<Boolean> polyline();

   @UseDefaults
   Property<Boolean> polygon();
   
   abstract class Mixin
      implements FieldValue
   {
      @This GeoLocationFieldValue definition;

      public Boolean validate( String value)
      {
         
         // TODO: How is data represented here?
         return true;
      }
   }
}
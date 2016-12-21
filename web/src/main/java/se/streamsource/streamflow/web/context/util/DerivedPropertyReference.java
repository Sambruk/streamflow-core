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
package se.streamsource.streamflow.web.context.util;

import java.lang.reflect.Method;

import org.qi4j.api.query.grammar.AssociationReference;
import org.qi4j.api.query.grammar.PropertyReference;

public abstract class DerivedPropertyReference<T> implements PropertyReference<T> {

   Class<T> propertyType;

   public DerivedPropertyReference(Class<T> propertyType) {
      this.propertyType = propertyType;
   }

   @Override
   public String propertyName() {
      throw new UnsupportedOperationException("Not available");
   }

   @Override
   public Class<?> propertyDeclaringType() {
      throw new UnsupportedOperationException("Not available");
   }

   @Override
   public Method propertyAccessor() {
      throw new UnsupportedOperationException("Not available");
   }

   @Override
   public Class<T> propertyType() {
      return propertyType;
   }

   @Override
   public AssociationReference traversedAssociation() {
      return null;
   }

   @Override
   public PropertyReference<?> traversedProperty() {
      return null;
   }
}

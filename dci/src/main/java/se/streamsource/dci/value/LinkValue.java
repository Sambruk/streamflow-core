/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.dci.value;

import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

/**
 * Link value
 */
public interface LinkValue
      extends ValueComposite
{
   /**
    * This is typically the description of the entity
    *
    * @return
    */
   Property<String> text();

   /**
    * This is typically the id of the entity
    *
    * @return
    */
   Property<String> id();

   /**
    * This is the href of the entity, relative to the producer of the LinkValue
    * @return
    */
   Property<String> href();

   /**
    * This is the type of the link, typically to help differentiate between
    * different types of resources.
    *
    * @return
    */
   @Optional
   Property<String> rel();
}
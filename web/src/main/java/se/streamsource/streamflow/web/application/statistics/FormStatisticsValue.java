/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.web.application.statistics;

import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

/**
 * Value for form data about a case in statistics. For multi-value fields
 * the same {id,form,field} may repeat many times in the statistics, with different values.
 */
public interface FormStatisticsValue
      extends ValueComposite
{
   /**
    * Case id
    * @return if of the case
    */
   Property<String> identity();

   /**
    * Name of the form
    * @return name of the form
    */
   Property<String> form();

   /**
    * Name of the field
    *
    * @return name of the field
    */
   Property<String> field();

   /**
    * Value for the field
    *
    * @return value for field
    */
   Property<String> value();
}
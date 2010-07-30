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

package se.streamsource.streamflow.util;

/**
 * Utility methods for strings
 */
public class Strings
{
   /**
    * Check if a string is not null and not equal to ""
    *
    * @param value string to be tested
    * @return true if value is not null and not equal to ""
    */
   public static boolean notEmpty(String value)
   {
      return value != null && !value.trim().equals("");
   }

}

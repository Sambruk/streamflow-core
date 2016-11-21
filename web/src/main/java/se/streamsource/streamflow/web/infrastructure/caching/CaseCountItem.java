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
package se.streamsource.streamflow.web.infrastructure.caching;

import java.io.Serializable;

public class CaseCountItem implements Serializable
{

   /**
    * 
    */
   private static final long serialVersionUID = 2215168683765433245L;

   int count;
   int unread;

   public void addToCount(int increment)
   {
      count += increment;
   }

   public void addToUnread(int increment)
   {
      unread += increment;
   }

   public int getCount()
   {
      return count;
   }

   public int getUnread()
   {
      return unread;
   }

}

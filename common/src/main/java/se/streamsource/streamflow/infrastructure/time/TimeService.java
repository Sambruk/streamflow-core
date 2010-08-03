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

package se.streamsource.streamflow.infrastructure.time;

import java.util.Date;

/**
 * Time service. You can either set a given date to be returned,
 * or else System.currentTimeMillis() will be used.
 * <p/>
 * It is convenient to set the time to a specific date for generating test data
 * that is supposed to have a date other than System.currentTimeMillis
 */
public class TimeService
      implements Time
{
   private Date now;

   public void setNow( Date newNow )
   {
      now = newNow;
   }

   public long timeNow()
   {
      return now == null ? System.currentTimeMillis() : now.getTime();
   }

   public Date dateNow()
   {
      return now == null ? new Date() : now;
   }
}

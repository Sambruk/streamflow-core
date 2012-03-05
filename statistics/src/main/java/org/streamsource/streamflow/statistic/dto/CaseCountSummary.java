/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package org.streamsource.streamflow.statistic.dto;

/**
 * Created by IntelliJ IDEA.
 * User: arvidhuss
 * Date: 2/20/12
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class CaseCountSummary
{
   private int total;
   private int withCasetype;

   public int getTotal()
   {
      return total;
   }

   public void setTotal( int total )
   {
      this.total = total;
   }

   public int getWithoutCasetype()
   {
      return withoutCasetype;
   }

   public void setWithoutCasetype( int withoutCasetype )
   {
      this.withoutCasetype = withoutCasetype;
   }

   public int getWithCasetype()
   {
      return withCasetype;
   }

   public void setWithCasetype( int withCasetype )
   {
      this.withCasetype = withCasetype;
   }

   private int withoutCasetype;

}

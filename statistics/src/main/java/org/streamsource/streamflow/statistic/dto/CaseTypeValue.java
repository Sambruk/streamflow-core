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
 * Date: 2/23/12
 * Time: 4:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class CaseTypeValue
   implements Comparable<CaseTypeValue>
{
   private String id;
   private String name;
   
   public CaseTypeValue( String id, String name )
   {
      this.id = id;
      this.name = name;
   }

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }

   public int compareTo( CaseTypeValue o )
   {
      return this.name.compareTo( o.getName() );
   }
}

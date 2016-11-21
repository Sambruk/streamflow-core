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
package org.streamsource.streamflow.statistic.dto;

/**
 * Created by IntelliJ IDEA.
 * User: arvidhuss
 * Date: 2/21/12
 * Time: 10:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class CaseCountTotal
   extends CaseCount
{
   public CaseCountTotal( String name, String[] periods)
   {
      super(name, periods);
   }
   
   public void plus( String periodLabel, int amount )
   {
      Period period = values.get( periodLabel );
      period.setCount( period.getCount() + amount );

      total = total + amount;
   }
}

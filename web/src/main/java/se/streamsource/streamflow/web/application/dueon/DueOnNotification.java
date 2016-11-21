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
package se.streamsource.streamflow.web.application.dueon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import se.streamsource.streamflow.web.domain.structure.user.Contactable;

public class DueOnNotification
{

   private Contactable recipient;

   private Set<DueOnItem> personalOverdueCases = new HashSet<DueOnItem>();
   private Set<DueOnItem> personalThresholdCases = new HashSet<DueOnItem>();
   private Set<DueOnItem> functionOverdueCases = new HashSet<DueOnItem>();
   private Set<DueOnItem> functionThresholdCases = new HashSet<DueOnItem>();
   private Set<DueOnItem> monitoredOverdueCases = new HashSet<DueOnItem>();
   private Set<DueOnItem> monitoredThresholdCases = new HashSet<DueOnItem>();
   
   public DueOnNotification(Contactable recipient) 
   {
      this.recipient = recipient;
   }

   public Contactable getRecipient()
   {
      return recipient;
   }

   public Set<DueOnItem> getPersonalOverdueCases()
   {
      return personalOverdueCases;
   }

   public Set<DueOnItem> getPersonalThresholdCases()
   {
      return personalThresholdCases;
   }

   public Set<DueOnItem> getFunctionOverdueCases()
   {
      return functionOverdueCases;
   }

   public Set<DueOnItem> getFunctionThresholdCases()
   {
      return functionThresholdCases;
   }

   public Set<DueOnItem> getMonitoredOverdueCases()
   {
      return monitoredOverdueCases;
   }

   public Set<DueOnItem> getMonitoredThresholdCases()
   {
      return monitoredThresholdCases;
   }


}

/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
package se.streamsource.streamflow.web.application.dueon;

import java.util.ArrayList;
import java.util.List;

import se.streamsource.streamflow.web.domain.structure.user.Contactable;

public class DueOnNotification
{

   private Contactable recipient;

   private List<DueOnItem> personalOverdueCases = new ArrayList<DueOnItem>();
   private List<DueOnItem> personalThresholdCases = new ArrayList<DueOnItem>();
   private List<DueOnItem> functionOverdueCases = new ArrayList<DueOnItem>();
   private List<DueOnItem> functionThresholdCases = new ArrayList<DueOnItem>();
   private List<DueOnItem> monitoredOverdueCases = new ArrayList<DueOnItem>();
   private List<DueOnItem> monitoredThresholdCases = new ArrayList<DueOnItem>();
   
   public DueOnNotification(Contactable recipient) 
   {
      this.recipient = recipient;
   }

   public Contactable getRecipient()
   {
      return recipient;
   }

   public List<DueOnItem> getPersonalOverdueCases()
   {
      return personalOverdueCases;
   }

   public List<DueOnItem> getPersonalThresholdCases()
   {
      return personalThresholdCases;
   }

   public List<DueOnItem> getFunctionOverdueCases()
   {
      return functionOverdueCases;
   }

   public List<DueOnItem> getFunctionThresholdCases()
   {
      return functionThresholdCases;
   }

   public List<DueOnItem> getMonitoredOverdueCases()
   {
      return monitoredOverdueCases;
   }

   public List<DueOnItem> getMonitoredThresholdCases()
   {
      return monitoredThresholdCases;
   }


}

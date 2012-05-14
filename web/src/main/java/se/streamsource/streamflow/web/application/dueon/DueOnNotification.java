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
package se.streamsource.streamflow.web.application.dueon;

import java.util.ArrayList;
import java.util.List;

import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.structure.user.Contactable;

public class DueOnNotification
{

   private Contactable recipient;

   private List<CaseEntity> personalOverdueCases = new ArrayList<CaseEntity>();
   private List<CaseEntity> personalThresholdCases = new ArrayList<CaseEntity>();
   private List<CaseEntity> functionOverdueCases = new ArrayList<CaseEntity>();
   private List<CaseEntity> functionThresholdCases = new ArrayList<CaseEntity>();
   private List<CaseEntity> monitoredOverdueCases = new ArrayList<CaseEntity>();
   private List<CaseEntity> monitoredThresholdCases = new ArrayList<CaseEntity>();
   
   public DueOnNotification(Contactable recipient) 
   {
      this.recipient = recipient;
   }

   public Contactable getRecipient()
   {
      return recipient;
   }

   public List<CaseEntity> getPersonalOverdueCases()
   {
      return personalOverdueCases;
   }

   public List<CaseEntity> getPersonalThresholdCases()
   {
      return personalThresholdCases;
   }

   public List<CaseEntity> getFunctionOverdueCases()
   {
      return functionOverdueCases;
   }

   public List<CaseEntity> getFunctionThresholdCases()
   {
      return functionThresholdCases;
   }

   public List<CaseEntity> getMonitoredOverdueCases()
   {
      return monitoredOverdueCases;
   }

   public List<CaseEntity> getMonitoredThresholdCases()
   {
      return monitoredThresholdCases;
   }

}

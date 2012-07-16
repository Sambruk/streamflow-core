/**
 *
 * Copyright 2009-2012 Jayway Products AB
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import se.streamsource.streamflow.util.Dates;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;

public class DueOnItem
{

   private String caseId;
   private String dueOn;
   private String owner;
   private String description;
   private String assignedTo;

   private static String shortFormatPattern = "d':e' MMMM";
   private static String longFormatPattern = "d':e' MMMM yyyy";
   
   public DueOnItem(CaseEntity caze, Locale locale)
   {
      caseId = caze.caseId().get();
      dueOn = formatDueOn( caze.dueOn().get(), locale );
      owner = ((Describable) caze.owner().get()).getDescription();
      description = caze.getDescription();
      if (caze.assignedTo() != null && caze.assignedTo().get() != null)
         assignedTo = ((Describable)caze.assignedTo().get()).getDescription();
   }
   
   private String formatDueOn(Date date, Locale locale)
   {
      if (Dates.isThisYear( date ))
         return Strings.capitalize( (new SimpleDateFormat( shortFormatPattern, locale )).format( date ));
      else
         return Strings.capitalize( (new SimpleDateFormat( longFormatPattern, locale )).format( date ));
   }

   public String getCaseId()
   {
      return caseId;
   }
   public void setCaseId(String caseId)
   {
      this.caseId = caseId;
   }
   public String getDueOn()
   {
      return dueOn;
   }
   public void setDueOn(String dueOn)
   {
      this.dueOn = dueOn;
   }
   public String getOwner()
   {
      return owner;
   }
   public void setOwner(String owner)
   {
      this.owner = owner;
   }
   public String getDescription()
   {
      return description;
   }
   public void setDescription(String description)
   {
      this.description = description;
   }
   public String getAssignedTo()
   {
      return assignedTo;
   }
   public void setAssignedTo(String assignedTo)
   {
      this.assignedTo = assignedTo;
   }
   
   
}

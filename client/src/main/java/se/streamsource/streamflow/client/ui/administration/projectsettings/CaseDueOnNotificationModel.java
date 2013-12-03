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
package se.streamsource.streamflow.client.ui.administration.projectsettings;

import org.restlet.data.Form;

import se.streamsource.dci.value.FormValue;
import se.streamsource.streamflow.client.ResourceModel;

/**
 * JAVADOC
 */
public class CaseDueOnNotificationModel
      extends ResourceModel<FormValue>
{

   public CaseDueOnNotificationModel()
   {
      relationModelMapping("recipients", RecipientsModel.class );
   }
   
   public void changeThreshold(Integer threshold)
   {
      Form form = new Form();
      form.set("threshold", threshold.toString() );
      
      client.postLink(command("changenotificationthreshold"), form);
   }

   public void activateNotifications(Boolean activate)
   {
      Form form = new Form();
      form.set("activate", activate.toString() );
      
      client.postLink(command("activatenotifications"), form);
   }
   

}

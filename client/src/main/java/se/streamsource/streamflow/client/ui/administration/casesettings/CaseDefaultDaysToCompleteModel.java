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
package se.streamsource.streamflow.client.ui.administration.casesettings;

import org.restlet.data.Form;
import se.streamsource.dci.value.FormValue;
import se.streamsource.streamflow.client.ResourceModel;

/**
 * JAVADOC
 */
public class CaseDefaultDaysToCompleteModel
      extends ResourceModel<FormValue>
{
   public void changeDefaultDaysToComplete( Integer newSetting )
   {
      Form form = new Form();
      form.set("defaultdaystocomplete", newSetting.toString());

      client.postLink(command("update"), form);
   }
}

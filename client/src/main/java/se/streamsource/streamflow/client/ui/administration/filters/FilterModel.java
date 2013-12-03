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
package se.streamsource.streamflow.client.ui.administration.filters;

import org.restlet.data.Form;
import se.streamsource.streamflow.client.ResourceModel;

/**
 * Represents a Project in the administration model
 */
public class FilterModel
   extends ResourceModel<Form>
{
   public FilterModel()
   {
      relationModelMapping("rules", RulesModel.class);
      relationModelMapping("actions", ActionsModel.class);
   }

   public Form getIndex()
   {
      return client.query("index", Form.class);
   }

   public void update(Form form)
   {
      client.putCommand("update", form);
   }
}

/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.client.ui.workspace.cases;

import se.streamsource.streamflow.client.ResourceModel;

/**
 * TODO
 */
public class CasesModel
      extends ResourceModel
{
   public CaseModel newCaseModel(String href)
   {
      return module.objectBuilderFactory().newObjectBuilder(CaseModel.class).use(this, client.getClient(href)).newInstance();
   }
}
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
package se.streamsource.streamflow.client.ui;

import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.spi.structure.ApplicationSPI;
import org.restlet.Client;
import org.restlet.data.Protocol;
import se.streamsource.streamflow.client.assembler.StreamflowClientModelAssembler;
import se.streamsource.streamflow.client.ui.account.AccountModel;
import se.streamsource.streamflow.client.ui.account.AccountsModel;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceModel;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseModel;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseTableValue;
import se.streamsource.streamflow.client.ui.workspace.cases.general.CaseGeneralModel;
import se.streamsource.streamflow.client.ui.workspace.search.SearchResultTableModel;
import se.streamsource.streamflow.client.ui.workspace.table.CasesTableModel;

import java.beans.Beans;

/**
 * TODO
 */
@Ignore
public class ClientModelTest
{
//   @Test
   public void test() throws Exception
   {
      Client client = new Client(Protocol.HTTP);
      client.start();

      Energy4Java qi4j = new Energy4Java();
      ApplicationSPI app = qi4j.newApplication(new StreamflowClientModelAssembler(client));
      app.activate();

      Module module = app.findModule("Model","Account");

      AccountsModel accountsModel = module.objectBuilderFactory().newObject(AccountsModel.class);
      AccountModel accountModel = accountsModel.accountModel(accountsModel.getAccounts().get(0));

      WorkspaceModel workspaceModel = accountModel.newWorkspaceModel();
      SearchResultTableModel search = workspaceModel.newSearchModel();
      search.search("gatan");

      for (CaseTableValue caseTableValue : search.getEventList())
      {
         System.out.println(caseTableValue.description().get());
      }

      workspaceModel.refresh();
      for (ContextItem contextItem : workspaceModel.getItems())
      {
         if (contextItem.getGroup().equals("Kontaktcenter") && contextItem.getName().equals("Inbox"))
         {
            CasesTableModel cases = workspaceModel.newCasesTableModel(contextItem.getClient());
            cases.refresh();

            CaseModel caseModel = workspaceModel.newCasesModel().newCaseModel(cases.getEventList().get(1).href().get());

            CaseGeneralModel generalModel = caseModel.newGeneralModel();
            generalModel.refresh();
            System.out.println(generalModel.getGeneral().description().get());

            System.out.println("Kontaktcenter has "+cases.getEventList().size()+" cases open");
         }
      }

   }
}

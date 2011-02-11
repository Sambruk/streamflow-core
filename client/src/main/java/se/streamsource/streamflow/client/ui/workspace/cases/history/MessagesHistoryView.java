/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.client.ui.workspace.cases.history;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;

import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.MessagesView;

public class MessagesHistoryView extends MessagesView
{

   private static final long serialVersionUID = 8844722606127717377L;

   public MessagesHistoryView(@Service ApplicationContext context, @Uses CommandQueryClient client,
         @Structure ObjectBuilderFactory obf)
   {
      super(context,client,obf);
   }
   
   @Action
   public void writeMessage()
   {
      super.writeMessage();
   }
   
   @Action
   public Task sendMessage()
   {
      return super.sendMessage();
   }
   
   @Action
   public void cancelNewMessage()
   {
      super.cancelNewMessage();
   }

   @Action
   public void closeMessageDetails()
   {
      super.closeMessageDetails();
   }
}

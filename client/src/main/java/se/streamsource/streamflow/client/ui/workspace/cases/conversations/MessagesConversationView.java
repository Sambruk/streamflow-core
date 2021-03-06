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
package se.streamsource.streamflow.client.ui.workspace.cases.conversations;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;

public class MessagesConversationView extends MessagesView
{

   private static final long serialVersionUID = 8844722606127717377L;

   public MessagesConversationView(@Service ApplicationContext context, @Uses MessagesModel model)
   {
      super(context,model);
   }
   
   @Action
   public void writeMessage()
   {
      super.writeMessage();
   }
   
   @Action
   public Task createMessage()
   {
      return super.createMessage();
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

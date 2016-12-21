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

import com.jgoodies.forms.factories.Borders;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;

import javax.swing.JPanel;
import java.awt.BorderLayout;

public class ConversationView extends JPanel
{
   public ConversationView( @Service final ApplicationContext context,
                                @Structure Module module,
                                @Uses ConversationModel model)
   {
      super( new BorderLayout() );

      this.setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

      add( module.objectBuilderFactory().newObjectBuilder(ConversationParticipantsView.class).use(model.newParticipantsModel()).newInstance(), BorderLayout.NORTH );
      add( module.objectBuilderFactory().newObjectBuilder(MessagesConversationView.class).use( model.newMessagesModel()).newInstance(), BorderLayout.CENTER );
      
   }
}

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

package se.streamsource.streamflow.client.ui.workspace.cases.conversations;

import com.jgoodies.forms.factories.*;
import org.jdesktop.application.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.object.*;
import se.streamsource.dci.restlet.client.*;

import javax.swing.*;
import java.awt.*;

public class ConversationView extends JPanel
{
   public ConversationView( @Service final ApplicationContext context,
                                @Structure ObjectBuilderFactory obf,
                                @Uses CommandQueryClient client)
   {
      super( new BorderLayout() );

      this.setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

      add( obf.newObjectBuilder( ConversationParticipantsView.class ).use(client.getSubClient( "participants" )).newInstance(), BorderLayout.NORTH );
      add( obf.newObjectBuilder( MessagesConversationView.class ).use( client.getSubClient("messages" )).newInstance(), BorderLayout.CENTER );
      
   }
}

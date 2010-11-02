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

package se.streamsource.streamflow.client.ui.administration.templates;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.ui.workspace.cases.attachments.AttachmentsView;

import javax.swing.JPanel;
import java.awt.BorderLayout;

import static java.awt.BorderLayout.*;

public class TemplatesView extends JPanel
{
   public TemplatesView( @Service ApplicationContext appContext,
                         @Uses CommandQueryClient client,
                         @Structure ObjectBuilderFactory obf )
   {

      this.setLayout( new BorderLayout( ) );

      add( CENTER, obf.newObjectBuilder( AttachmentsView.class ).use( client).newInstance());

      add( EAST, obf.newObjectBuilder( SelectedTemplatesView.class ).use(client.getClient( "../templates/" )).newInstance());

   }
}

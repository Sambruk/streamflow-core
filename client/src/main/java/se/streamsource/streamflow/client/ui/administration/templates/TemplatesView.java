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

package se.streamsource.streamflow.client.ui.administration.templates;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.ui.workspace.cases.attachments.AttachmentsView;

import javax.swing.*;
import java.awt.*;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.EAST;

public class TemplatesView extends JPanel
{
   public TemplatesView( @Service ApplicationContext appContext,
                         @Uses SelectedTemplatesModel model,
                         @Structure ObjectBuilderFactory obf )
   {

      this.setLayout( new BorderLayout( ) );

      add( CENTER, obf.newObjectBuilder( AttachmentsView.class ).use( model.newAttachmentsModel()).newInstance());

      add( EAST, obf.newObjectBuilder( SelectedTemplatesView.class ).use(model).newInstance());

   }
}

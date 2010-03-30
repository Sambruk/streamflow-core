/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.ui.administration.organization;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.value.LinkValue;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * JAVADOC
 */
public class OrganizationsAdminView
      extends JSplitPane
{
   @Structure
   ObjectBuilderFactory obf;

   public OrganizationsAdminView( @Uses final OrganizationsView organizationsView )
   {
      super();

      setLeftComponent( organizationsView );
      setRightComponent( new JPanel() );

      setDividerLocation( 250 );

      final JList list = organizationsView.getOrganizationsList();

      list.addListSelectionListener( new ListSelectionListener()
      {
         public void valueChanged( ListSelectionEvent e )
         {
            if (!e.getValueIsAdjusting())
            {
               int idx = list.getSelectedIndex();
               if (idx < list.getModel().getSize() && idx >= 0)
               {
                  LinkValue organizationValue = (LinkValue) list.getModel().getElementAt( idx );
                  OrganizationUsersView organizationView = obf.newObjectBuilder(
                        OrganizationUsersView.class ).use( organizationsView.getModel()
                        .getOrganizationUsersModel( organizationValue.id().get() ) ).newInstance();
                  setRightComponent( organizationView );
               } else
               {
                  setRightComponent( new JPanel() );
               }
            }
         }
      } );
   }

}
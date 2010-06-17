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

package se.streamsource.streamflow.client.ui.administration.surface;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.ui.caze.CaseLabelsView;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class AccessPointsAdminView
   extends JSplitPane
{
   @Structure
   ObjectBuilderFactory obf;

   private AccessPointsModel accessPointsModel;
   public AccessPointsAdminView( @Uses final AccessPointsView accessPointsView,
                                 @Uses final AccessPointsModel accessPointsModel)
   {
      super();
      this.accessPointsModel = accessPointsModel;

      setBorder( BorderFactory.createEmptyBorder());
      setLeftComponent( accessPointsView );
      setRightComponent( new JPanel() );

      setDividerLocation( 200 );

      final JList list = accessPointsView.getAccessPointsList();
      list.addListSelectionListener( new ListSelectionListener()
      {
         public void valueChanged( ListSelectionEvent e )
         {
            if (!e.getValueIsAdjusting())
            {
               int idx = list.getSelectedIndex();
               if (idx < list.getModel().getSize() && idx >= 0)
               {
                  LinkValue accessPointValue = (LinkValue) list.getModel().getElementAt( idx );
                  AccessPointModel accessPointModel = accessPointsModel.getAccessPointModel( accessPointValue.id().get() );
                  CaseLabelsView labels = obf.newObject( CaseLabelsView.class );
                  AccessPointView view = obf.newObjectBuilder( AccessPointView.class ).use(
                        accessPointModel,
                        labels ).newInstance();
                  setRightComponent( view );
               } else
               {
                  setRightComponent( new JPanel() );
               }
            }
         }
      } );


      accessPointsView.addAncestorListener( new RefreshWhenVisible( accessPointsModel, this ) );

   }
}

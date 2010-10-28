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

package se.streamsource.streamflow.client.ui;

import ca.odell.glazedlists.EventList;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.TitledLinkValue;
import se.streamsource.streamflow.client.util.GroupedFilteredList;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.UsersAndGroupsModel;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.GridLayout;
import java.util.HashSet;
import java.util.Set;

/**
 * JAVADOC
 */
public class SelectUsersAndGroupsDialog
      extends JPanel
{
   private ValueBuilderFactory vbf;

   private GroupedFilteredList groupList;
   private GroupedFilteredList userList;

   private Set<String> usersAndGroups;
   private Set<LinkValue> selectedEntities;

   public SelectUsersAndGroupsDialog( @Service ApplicationContext context,
                                      @Uses UsersAndGroupsModel model,
                                      @Structure ObjectBuilderFactory obf,
                                      final @Structure ValueBuilderFactory vbf)
   {
      super( new GridLayout(1, 2) );
      setActionMap( context.getActionMap( this ) );
      getActionMap().put( JXDialog.CLOSE_ACTION_COMMAND, getActionMap().get("cancel" ));

      setName( i18n.text( AdministrationResources.search_users_or_groups) );

      selectedEntities = new HashSet<LinkValue>();
      EventList<TitledLinkValue> groups = model.getPossibleGroups();

      groupList = new GroupedFilteredList();
      groupList.setEventList( groups );

      add( new JScrollPane( groupList ));

      userList = new GroupedFilteredList();
      EventList<TitledLinkValue> users = model.getPossibleUsers();

      userList.setEventList(users);

      add( new JScrollPane( userList ));
   }

   public Set<LinkValue> getSelectedEntities()
   {
      return selectedEntities;
   }

   @Action
   public void execute()
   {
      for (Object value : groupList.getList().getSelectedValues())
      {
         selectedEntities.add( (LinkValue) value );
      }

      for (Object value : userList.getList().getSelectedValues())
      {
         selectedEntities.add( (LinkValue) value );
      }
      WindowUtils.findWindow( this ).dispose();
   }

   @Action
   public void cancel()
   {
      WindowUtils.findWindow( this ).dispose();
   }
}
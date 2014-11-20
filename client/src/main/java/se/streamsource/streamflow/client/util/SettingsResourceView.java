/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.client.util;

import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.ResourceModel;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.FormOnRemoveView;
import se.streamsource.streamflow.client.ui.administration.casesettings.CaseAccessDefaultsView;
import se.streamsource.streamflow.client.ui.administration.casesettings.CaseArchivalSettingView;
import se.streamsource.streamflow.client.ui.administration.casesettings.CaseDefaultDaysToCompleteView;
import se.streamsource.streamflow.client.ui.administration.casesettings.PriorityOnCaseView;
import se.streamsource.streamflow.client.ui.administration.casesettings.FormOnCloseView;
import se.streamsource.streamflow.client.ui.administration.casetypes.CaseTypeDetailView;
import se.streamsource.streamflow.client.ui.administration.organisationsettings.MailRestrictionsView;
import se.streamsource.streamflow.client.ui.administration.projectsettings.CaseDueOnNotificationView;
import se.streamsource.streamflow.client.ui.administration.projectsettings.RequiresCaseTypeView;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

import static se.streamsource.dci.value.link.Links.*;
import static se.streamsource.streamflow.client.util.i18n.*;

/**
 * Show settings for a REST resource, with each part as its own panel with a separator. To determine panels, do a query
 * to the resources directory URL ("/") to get a ResourceValue. Then iterate through the registered views and check if
 * they are in ResourceValue.resources(). By using xzm
 * the order of the registered views we ensure that the UI order of the tabs is always the same, regardless of the order
 * returned from the server.
 */
public class SettingsResourceView
      extends JPanel
{
   private static final Map<String, Class<? extends JComponent>> views = new LinkedHashMap<String, Class<? extends JComponent>>();
   private static final Map<String, Enum> settingsNames = new LinkedHashMap<String, Enum>();

   static
   {
      addSettings("caseaccessdefaults", AdministrationResources.caseaccessdefaults_separator, CaseAccessDefaultsView.class);

      addSettings("defaultdaystocomplete", AdministrationResources.default_days_to_complete_separator, CaseDefaultDaysToCompleteView.class);

      addSettings("dueonnotification", AdministrationResources.dueon_notification_separator, CaseDueOnNotificationView.class);

      addSettings("requirescasetype", AdministrationResources.requires_casetype_seperator, RequiresCaseTypeView.class);

      addSettings( "casetypedetail", AdministrationResources.detail_separator, CaseTypeDetailView.class);

      addSettings("archival", AdministrationResources.archival_settings_separator, CaseArchivalSettingView.class);

      addSettings( "priorityoncase", AdministrationResources.casepriority_separator, PriorityOnCaseView.class );

      addSettings( "formonclose", AdministrationResources.formonclose_separator, FormOnCloseView.class );

      addSettings( "restrictions", AdministrationResources.restrictions_settings_separator,  CaseAccessDefaultsView.class);

       addSettings( "mailrestrictions", AdministrationResources.mailrestrictions_separator, MailRestrictionsView.class);

       addSettings( "mailrestrictions", AdministrationResources.mailrestrictions_separator, MailRestrictionsView.class);

       addSettings( "formondelete", AdministrationResources.formondelete_separator, FormOnRemoveView.class );

      }

   private static void addSettings(String name, Enum tabName, Class<? extends JComponent> viewClass)
   {
      settingsNames.put( name, tabName );
      views.put( name, viewClass );
   }

   public SettingsResourceView(@Uses ResourceModel model, @Structure Module module)
   {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      
      model.refresh();
      EventList<LinkValue> resources = model.getResources();
      for (Map.Entry<String, Class<? extends JComponent>> stringClassEntry : views.entrySet())
      {
         LinkValue linkedResource = Iterables.first(Iterables.filter(withRel(stringClassEntry.getKey()), resources));
         if (linkedResource != null)
         {
            String separatorText = text( settingsNames.get( stringClassEntry.getKey() ) );
            Class<? extends JComponent> tabClass = stringClassEntry.getValue();
            try
            {
               Object resourceModel = model.newResourceModel(linkedResource);

               JLabel jLabel = new JLabel(separatorText, JLabel.LEFT);
               jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
               jLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
               add(jLabel);
               JComponent view = module.objectBuilderFactory().newObjectBuilder(tabClass).use(resourceModel).newInstance();
               view.setAlignmentX(JComponent.LEFT_ALIGNMENT);
               view.setBorder(BorderFactory.createEmptyBorder(0,0,20,0));
               //view.setBorder( BorderFactory.createLineBorder(Color.RED, 3));
               add(view);
            } catch (Exception e)
            {
               e.printStackTrace();
            }
         }
      }
      add(Box.createVerticalGlue());
      
   }
}

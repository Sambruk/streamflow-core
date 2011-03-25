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

package se.streamsource.streamflow.client.ui.administration.surface;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.domain.organization.EmailAccessPointValue;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * TODO
 */
public class EmailAccessPointView
        extends JPanel
        implements TransactionListener
{
   public EmailAccessPointView(@Uses CommandQueryClient client)
   {
      FormLayout layout = new FormLayout(
              "75dlu, 5dlu, 120dlu", "pref, pref, pref");
      DefaultFormBuilder formBuilder = new DefaultFormBuilder(layout, this);

      EmailAccessPointValue emailAccessPoint = client.query("index", EmailAccessPointValue.class);

      formBuilder.append(i18n.text(AdministrationResources.email), new JLabel(emailAccessPoint.email().get()));
      formBuilder.nextLine();
      formBuilder.append(i18n.text(AdministrationResources.accesspoint));
   }

   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }
}

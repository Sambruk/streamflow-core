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
package se.streamsource.streamflow.client.ui.administration.external;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.JPanel;


public class IntegrationPointView
      extends JPanel
      implements Refreshable, TransactionListener
{
   @Service
   DialogService dialogs;

   @Structure
   Module module;

   private IntegrationPointModel model;

   public IntegrationPointView( @Service ApplicationContext appContext,
                                @Uses IntegrationPointModel model,
                                @Structure Module module )
   {
      this.model = model;
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
   }

   public void refresh()
   {
   }
}

/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui.events;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import se.streamsource.streamflow.client.StreamFlowApplication;
import se.streamsource.streamflow.client.infrastructure.events.EventFetcher;
import se.streamsource.streamflow.client.ui.AccountSelector;
import se.streamsource.streamflow.client.ui.administration.AccountModel;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * When account is selected, start subscribing to events from its event-stream.
 */
@Mixins(ClientEventSubscriptionService.Mixin.class)
public interface ClientEventSubscriptionService
        extends Activatable, ServiceComposite
{
    public class Mixin
            implements Activatable
    {
        AccountSelector selector;
        AccountModel selectedAccount;

        @Service
        EventFetcher fetcher;

        public Mixin( @Service StreamFlowApplication app )
        {
            selector = app.getAccountSelector();
        }

        public void activate() throws Exception
        {
                selector.addListSelectionListener( new ListSelectionListener()
                {
                    public void valueChanged( ListSelectionEvent e )
                    {
                        if (!e.getValueIsAdjusting())
                        {
                            AccountModel accountModel = selector.getSelectedAccount();
                            if (accountModel != null && accountModel != selectedAccount)
                            {
                                selectedAccount = accountModel;
                                fetcher.fetchFromResource( accountModel.serverResource().events() );
                            } else
                            {
                                fetcher.stopFetching();
                            }
                        }
                    }
                } );

        }

        public void passivate() throws Exception
        {
        }
    }
}

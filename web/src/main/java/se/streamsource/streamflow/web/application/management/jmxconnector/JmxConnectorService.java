/*
 * Copyright (c) 2009, Arvid Huss. All Rights Reserved.
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

package se.streamsource.streamflow.web.application.management.jmxconnector;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import se.streamsource.streamflow.web.domain.user.User;
import se.streamsource.streamflow.web.domain.user.UserEntity;

import javax.management.MBeanServer;
import javax.management.remote.*;
import javax.security.auth.Subject;
import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Mixins(JmxConnectorService.JmxConnectorMixin.class)
public interface JmxConnectorService
    extends Configuration, ServiceComposite, Activatable
{

    abstract class JmxConnectorMixin
        implements JmxConnectorService
    {
        @This
        Configuration<JmxConnectorConfiguration> config;

        @Service
        MBeanServer server;

        @Structure
        UnitOfWorkFactory uowf;

        Registry registry;
        JMXConnectorServer connector;

        public void activate() throws Exception
        {
            if(config.configuration().enabled().get())
            {
                // see java.rmi.server.ObjID
                System.setProperty("java.rmi.server.randomIDs", "true");
                if(connector != null)
                {
                    throw new IOException("CustomJmxConnector already started on " + connector.getAddress());
                }

                int jmxAgentPort = config.configuration().port().get();

                try
                {
                    registry = LocateRegistry.createRegistry(jmxAgentPort);
                } catch(RemoteException re)
                {
                    // Do nothing - registry on that port already exists
                }

                String hostName = InetAddress.getLocalHost().getHostName();
                JMXServiceURL url = new JMXServiceURL(
                                        "service:jmx:rmi://" + hostName +":" + jmxAgentPort
                                                + "/jndi/rmi://"+ hostName +":" + jmxAgentPort + "/jmxrmi");
                Map env = new HashMap();
                env.put(JMXConnectorServer.AUTHENTICATOR, new StreamflowJmxAuthenticator());

                connector = JMXConnectorServerFactory.newJMXConnectorServer(url, env, server);
                connector.start();
            }
        }

        public void passivate() throws Exception
        {

            if(connector != null)
                connector.stop();
            connector = null;

        }

        class StreamflowJmxAuthenticator implements JMXAuthenticator {

            public Subject authenticate(Object credentials)
            {

                UnitOfWork unitOfWork = uowf.newUnitOfWork();
                Subject subject = null;

                try
                {

                    if (!(credentials instanceof String[]))
                    {
                        // Special case for null so we get a more informative message
                        if (credentials == null) {
                            throw new SecurityException("Credentials required");
                        }
                        throw new SecurityException("Credentials should be String[]");
                    }

                    final String[] aCredentials = (String[]) credentials;
                    if (aCredentials.length != 2)
                    {
                        throw new SecurityException("Credentials should have 2 elements");
                    }

                    String username = (String) aCredentials[0];
                    String password = (String) aCredentials[1];

                    User user = unitOfWork.get(User.class, username);

                    if (!user.login(password))
                    {
                        throw new SecurityException("User/password combination not valid.");
                    }


                    if (((UserEntity)user).isAdministrator())
                    {
                        subject = new Subject(true,
                                           Collections.singleton(new JMXPrincipal(username)),
                                           Collections.EMPTY_SET,
                                           Collections.EMPTY_SET);
                    } else {
                        throw new SecurityException("Invalid credentials");
                    }

                    unitOfWork.complete();

                } catch(ConcurrentEntityModificationException e)
                {
                   unitOfWork.discard();

                }catch(UnitOfWorkCompletionException e)
                {
                    unitOfWork.discard();
                }

                return subject;
            }
        }
    }
}

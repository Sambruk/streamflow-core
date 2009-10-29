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
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.web.domain.user.UserAuthentication;

import javax.management.MBeanServer;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXPrincipal;
import javax.management.remote.JMXServiceURL;
import javax.security.auth.Subject;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This service starts a JMX RMI connector. It also creates an RMI-registry
 * to register the connector. The service is configured by changing the
 * settings in the JmxConnectorConfiguration.
 *
 * Authentication is done using the "administrator" account in StreamFlow.
 *
 */
@Mixins(JmxConnectorService.JmxConnectorMixin.class)
public interface JmxConnectorService
    extends Configuration, ServiceComposite, Activatable
{

    class JmxConnectorMixin
        implements Activatable
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

                int jmxAgentPort = config.configuration().port().get();

                registry = LocateRegistry.createRegistry(jmxAgentPort);

                String hostName = InetAddress.getLocalHost().getHostName();
                JMXServiceURL url = new JMXServiceURL(
                                        "service:jmx:rmi://" + hostName +":" + jmxAgentPort
                                                + "/jndi/rmi://"+ hostName +":" + jmxAgentPort + "/jmxrmi");
                Map env = new HashMap();
                env.put(JMXConnectorServer.AUTHENTICATOR, new StreamFlowJmxAuthenticator());

                try
                {
                    connector = JMXConnectorServerFactory.newJMXConnectorServer(url, env, server);
                    connector.start();
                } catch (Exception e)
                {
                    Logger.getLogger(JmxConnectorService.class.getName()).log(Level.SEVERE, "Could not start JMX connector", e);
                }
            }
        }

        public void passivate() throws Exception
        {
            // Stop connector
            if(connector != null)
            {
                connector.stop();
                connector = null;
            }

            // Remove registry
            if (registry != null)
            {
                UnicastRemoteObject.unexportObject(registry, true);
                registry = null;
            }
        }

        class StreamFlowJmxAuthenticator implements JMXAuthenticator {

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

                    String username = aCredentials[0];
                    String password = aCredentials[1];

                    UserAuthentication user = unitOfWork.get( UserAuthentication.class, username);

                    if (!user.login(password))
                    {
                        throw new SecurityException("User/password combination not valid.");
                    }


                    if (((UserAuthentication.Data)user).isAdministrator())
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

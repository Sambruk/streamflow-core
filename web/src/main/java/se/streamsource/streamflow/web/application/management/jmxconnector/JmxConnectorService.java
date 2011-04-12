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

package se.streamsource.streamflow.web.application.management.jmxconnector;

import org.qi4j.api.configuration.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.service.*;
import org.qi4j.api.unitofwork.*;
import org.qi4j.api.usecase.*;
import org.slf4j.*;
import se.streamsource.streamflow.web.domain.interaction.security.*;
import se.streamsource.streamflow.web.domain.structure.user.*;

import javax.management.*;
import javax.management.remote.*;
import javax.security.auth.*;
import java.net.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.*;

/**
 * This service starts a JMX RMI connector. It also creates an RMI-registry
 * to register the connector. The service is configured by changing the
 * settings in the JmxConnectorConfiguration.
 * <p/>
 * Authentication is done using the "administrator" account in Streamflow.
 */
@Mixins(JmxConnectorService.JmxConnectorMixin.class)
public interface JmxConnectorService
        extends Configuration, ServiceComposite, Activatable
{

   class JmxConnectorMixin
           implements Activatable
   {
      final Logger logger = LoggerFactory.getLogger(JmxConnectorService.class.getName());
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
         if (config.configuration().enabled().get())
         {
            // see java.rmi.server.ObjID
            System.setProperty("java.rmi.server.randomIDs", "true");

            int jmxAgentPort = config.configuration().port().get();

            try
            {
               registry = LocateRegistry.createRegistry(jmxAgentPort);
            } catch (RemoteException e)
            {
               registry = LocateRegistry.getRegistry(jmxAgentPort);
            }

            String hostName = InetAddress.getLocalHost().getHostName();
            JMXServiceURL url = new JMXServiceURL(
                    "service:jmx:rmi://" + hostName + ":" + jmxAgentPort
                            + "/jndi/rmi://" + hostName + ":" + jmxAgentPort + "/jmxrmi");
            Map env = new HashMap();
            env.put(JMXConnectorServer.AUTHENTICATOR, new StreamflowJmxAuthenticator());

            try
            {
               connector = JMXConnectorServerFactory.newJMXConnectorServer(url, env, server);
               connector.start();
            } catch (Exception e)
            {
               logger.error("Could not start JMX connector", e);
            }
         }
      }

      public void passivate() throws Exception
      {
         // Stop connector
         if (connector != null)
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

      class StreamflowJmxAuthenticator implements JMXAuthenticator
      {

         public Subject authenticate(Object credentials)
         {

            UnitOfWork unitOfWork = uowf.newUnitOfWork(UsecaseBuilder.newUsecase("Authenticate JMX user"));
            Subject subject = null;

            try
            {

               if (!(credentials instanceof String[]))
               {
                  // Special case for null so we get a more informative message
                  if (credentials == null)
                  {
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

               Authentication user = unitOfWork.get(Authentication.class, username);

               if (!user.login(password))
               {
                  throw new SecurityException("User/password combination not valid.");
               }


               if (((UserAuthentication.Data) user).isAdministrator())
               {
                  subject = new Subject(true,
                          Collections.singleton(new JMXPrincipal(username)),
                          Collections.EMPTY_SET,
                          Collections.EMPTY_SET);
               } else
               {
                  throw new SecurityException("Invalid credentials");
               }

               unitOfWork.complete();

            } catch (Throwable e)
            {
               unitOfWork.discard();
            }

            return subject;
         }
      }
   }
}

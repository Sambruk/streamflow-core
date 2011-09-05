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

package se.streamsource.streamflow.web.management;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.util.Iterables;
import org.slf4j.LoggerFactory;
import se.streamsource.infrastructure.circuitbreaker.CircuitBreaker;
import se.streamsource.infrastructure.jmx.MBeanTracker;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TODO
 */
@Mixins(InstantMessagingAdminService.Mixin.class)
public interface InstantMessagingAdminService
      extends ServiceComposite, Activatable, Configuration<InstantMessagingAdminConfiguration>
{
   class Mixin
         implements Activatable
   {

      @Service
      MBeanServer mbeanServer;

      Map<String, ObjectName> selectedMBean = new HashMap<String, ObjectName>();
      Map<String, Appender> appenders = new HashMap<String, Appender>();

      protected XMPPConnection connection;

      @This
      Configuration<InstantMessagingAdminConfiguration> config;

      MBeanTracker circuitBreakerTracker;

      public Mixin(@Service MBeanServer mbeanServer)
            throws Exception
      {
         circuitBreakerTracker = new MBeanTracker(new ObjectName("*:*,name=Circuit breaker"), mbeanServer);
         circuitBreakerTracker.registerCallback(new MBeanTracker.TrackerCallback()
         {
            NotificationListener listener = new NotificationListener()
                  {
                     public void handleNotification(Notification notification, Object o)
                     {
                        if (notification instanceof AttributeChangeNotification)
                        {
                           updatePresence();
                        }
                     }
                  };

            public void addedMBean(ObjectName newMBean, MBeanServer server)
                  throws Throwable
            {
               server.addNotificationListener(newMBean, listener , null, null);
            }

            public void removedMBean(ObjectName removedMBean, MBeanServer server)
                  throws Throwable
            {
               server.removeNotificationListener(removedMBean, listener);
            }
         });
      }

      public void activate() throws Exception
      {
         final InstantMessagingAdminConfiguration configuration = config.configuration();
         if (configuration.enabled().get())
         {
            // Check that all necessary config is there
            if (configuration.server().get() != null &&
                  configuration.server().get() != null &&
                  configuration.server().get() != null)
            {
               connection = new XMPPConnection(configuration.server().get());
               try
               {
                  connection.connect();
                  connection.login(configuration.user().get(), configuration.password().get());
                  circuitBreakerTracker.start();

                  connection.getRoster().setSubscriptionMode(Roster.SubscriptionMode.accept_all);

                  connection.getChatManager().addChatListener(new ChatManagerListener()
                  {
                     public void chatCreated(Chat chat, boolean b)
                     {
                        chat.addMessageListener(new MessageListener()
                        {
                           public void processMessage(final Chat chat, Message message)
                           {
                              try
                              {
                                 String txt = message.getBody();
                                 if (txt != null)
                                 {
                                    txt = txt.toLowerCase();
                                    if (txt.equals("help"))
                                    {
                                       StringWriter msg = new StringWriter();
                                       PrintWriter out = new PrintWriter(msg);
                                       out.println("Available commands:");
                                       out.println("* status");
                                       out.println("* info");
                                       out.println("* threaddump");
                                       out.println("* configuration");
                                       out.println("* configure <service-name>");
                                       out.println("* subscribe <debug/info/warn/error>");
                                       out.println("* unsubscribe");

                                       chat.sendMessage(msg.toString());
                                    } else if (txt.equals("status"))
                                    {
                                       StringWriter msg = new StringWriter();
                                       PrintWriter out = new PrintWriter(msg);
                                       for (ObjectName breaker : circuitBreakerTracker.getTracked())
                                       {
                                          out.println(breaker.getKeyProperty("service") + ":" + mbeanServer.getAttribute(breaker, "ServiceLevel"));
                                          if (mbeanServer.getAttribute(breaker, "Status").equals("off"))
                                          {
                                             out.println("  Circuit breaker is off:" + mbeanServer.getAttribute(breaker, "LastErrorMessage") + "(" + mbeanServer.getAttribute(breaker, "TrippedOn") + ")");
                                          }
                                       }

                                       chat.sendMessage(msg.toString());
                                    } else if (txt.equals("info"))
                                    {
                                       StringWriter msg = new StringWriter();
                                       PrintWriter out = new PrintWriter(msg);

                                       out.println("Installed in:" + new File(".").getAbsolutePath());
                                       out.println("System:" + System.getProperty("os.name") + " " + System.getProperty("os.arch") + " " + System.getProperty("os.version"));
                                       out.println("Java:" + System.getProperty("java.vm.name") + " " + System.getProperty("java.version"));
                                       out.println("Max memory: " + Runtime.getRuntime().maxMemory() / (1024 * 1000) + "M");
                                       out.println("Total memory: " + Runtime.getRuntime().totalMemory() / (1024 * 1000) + "M");
                                       out.println("Available memory: " + Runtime.getRuntime().freeMemory() / (1024 * 1000) + "M");

                                       chat.sendMessage(msg.toString());
                                    } else if (txt.equals("threaddump"))
                                    {
                                       Map<Thread, StackTraceElement[]> dumps = Thread.getAllStackTraces();

                                       StringWriter msg = new StringWriter();
                                       PrintWriter out = new PrintWriter(msg);
                                       for (Map.Entry<Thread, StackTraceElement[]> threadEntry : dumps.entrySet())
                                       {
                                          out.println(threadEntry.getKey().getName() + ":");
                                          for (StackTraceElement stackTraceElement : threadEntry.getValue())
                                          {
                                             out.println(stackTraceElement.toString());
                                          }
                                          out.println();
                                       }
                                       chat.sendMessage(msg.toString());
                                    } else if (txt.equals("configuration"))
                                    {
                                       Set<ObjectName> breakers = mbeanServer.queryNames(new ObjectName("*:*,name=Configuration"), null);
                                       StringWriter msg = new StringWriter();
                                       PrintWriter out = new PrintWriter(msg);
                                       for (ObjectName breaker : breakers)
                                       {
                                          out.println(breaker.getKeyProperty("service"));
                                       }
                                       out.println("Configure service with 'configure <name>'");
                                       chat.sendMessage(msg.toString());

                                    } else if (txt.startsWith("configure"))
                                    {
                                       String service = txt.split(" ")[1];
                                       ObjectName configuration = Iterables.first(mbeanServer.queryNames(new ObjectName("*:*,service=" + service + ",name=Configuration"), null));
                                       if (configuration != null)
                                       {
                                          StringWriter msg = new StringWriter();
                                          PrintWriter out = new PrintWriter(msg);

                                          for (MBeanAttributeInfo mBeanAttributeInfo : mbeanServer.getMBeanInfo(configuration).getAttributes())
                                          {
                                             Object value = mbeanServer.getAttribute(configuration, mBeanAttributeInfo.getName());
                                             out.println(mBeanAttributeInfo.getName() + ": " + value);
                                          }

                                          chat.sendMessage(msg.toString());

                                          selectedMBean.put(chat.getParticipant(), configuration);
                                       }
                                    } else if (txt.startsWith("subscribe"))
                                    {
                                       if (appenders.get(chat.getParticipant()) != null)
                                          Logger.getRootLogger().removeAppender(appenders.get(chat.getParticipant()));

                                       String level = txt.split(" ")[1];
                                       AppenderSkeleton appender = new AppenderSkeleton()
                                       {
                                          @Override
                                          protected void append(LoggingEvent event)
                                          {
                                             try
                                             {
                                                if (event.getThrowableStrRep() == null)
                                                   chat.sendMessage(layout.format(event));
                                                else
                                                {
                                                   String msg = layout.format(event);
                                                   for (String s : event.getThrowableStrRep())
                                                   {
                                                      msg+=s+"\n";
                                                   }
                                                   chat.sendMessage(msg);
                                                }
                                             } catch (XMPPException e)
                                             {
                                                // Ignore
                                             }
                                          }

                                          public void close()
                                          {
                                             //To change body of implemented methods use File | Settings | File Templates.
                                          }

                                          public boolean requiresLayout()
                                          {
                                             return false;
                                          }
                                       };
                                       appender.setLayout(new SimpleLayout());
                                       try
                                       {
                                          appender.setThreshold(Level.toLevel(level));
                                       } catch (IllegalArgumentException e)
                                       {
                                          e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                       }
                                       Logger.getRootLogger().addAppender(appender);
                                       appenders.put(chat.getParticipant(), appender);
                                       chat.sendMessage("Subscribe to log messages with priority "+level);
                                    } else if (txt.equals("unsubscribe"))
                                    {
                                       if (appenders.get(chat.getParticipant()) != null)
                                       {
                                          Logger.getRootLogger().removeAppender(appenders.get(chat.getParticipant()));
                                          appenders.remove(chat.getParticipant()).close();
                                          chat.sendMessage("Unsubscribed from log messages");
                                       }
                                    } else
                                    {
                                       ObjectName selected = selectedMBean.get(chat.getParticipant());
                                       if (selected != null)
                                       {
                                          if (txt.equals("restart"))
                                          {
                                             mbeanServer.invoke(selected, "restart", null, null);
                                             chat.sendMessage("Service restarted");
                                          } else
                                          {
                                             chat.sendMessage("Unknown command. Try 'help'");
                                          }

                                       } else
                                       {
                                          chat.sendMessage("Unknown command. Try 'help'");
                                       }
                                    }
                                 }
                              } catch (Exception e)
                              {
                                 LoggerFactory.getLogger(InstantMessagingAdminService.class).error("Chat error", e);
                              }
                           }
                        });
                     }
                  });
               } catch (Exception ex)
               {
                  LoggerFactory.getLogger(InstantMessagingAdminService.class).warn("Could not connect to IM server", ex);
               }
            }
         }
      }

      public void passivate() throws Exception
      {
         if (connection != null)
         {
            connection.disconnect();
            connection = null;
         }

         circuitBreakerTracker.stop();
      }

      private void updatePresence()
      {
         if (connection == null || !connection.isConnected())
            return;

         try
         {
            List<String> offBreakers = new ArrayList<String>();
            for (ObjectName circuitBreaker : circuitBreakerTracker.getTracked())
            {
               String status = (String)mbeanServer.getAttribute(circuitBreaker, "Status");
               if (status.equals(CircuitBreaker.Status.off.name()))
               {
                  offBreakers.add(circuitBreaker.getKeyProperty("service"));
               }
            }

            if (offBreakers.isEmpty())
               connection.sendPacket(new Presence(Presence.Type.available, "Status ok", 0, Presence.Mode.available));
            else
               connection.sendPacket(new Presence(Presence.Type.available, "Circuit breakers off:"+offBreakers, 0, Presence.Mode.away));

         } catch (Exception e)
         {
            LoggerFactory.getLogger(InstantMessagingAdminService.class).error("Could not update presence", e);
         }

      }
   }
}

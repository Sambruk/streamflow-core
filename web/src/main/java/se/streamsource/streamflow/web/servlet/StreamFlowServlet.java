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

package se.streamsource.streamflow.web.servlet;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.ext.servlet.ServerServlet;
import se.streamsource.streamflow.web.rest.StreamFlowRestApplication;

/**
 * JAVADOC
 */
public class StreamFlowServlet
      extends ServerServlet
{
   @Override
   protected Component createComponent()
   {
      Component component = super.createComponent();
      component.getClients().add( Protocol.CLAP );
      component.getClients().add( Protocol.FILE );

      return component;
   }

   @Override
   protected Application createApplication( Context context )
   {
      try
      {
         return new StreamFlowRestApplication( context.createChildContext() );
      } catch (Exception e)
      {
         // TODO This sucks
         e.printStackTrace();
         return null;
      }
   }

   @Override
   public void destroy()
   {
      if ((getApplication() != null) && (getApplication().isStarted()))
      {
         try
         {
            getApplication().stop();
         } catch (Exception e)
         {
            log( "Error during the stopping of the Restlet Application", e );
         }
      }

      super.destroy();
   }
}

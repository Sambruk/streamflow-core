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

package se.streamsource.dci.restlet.client;

import org.qi4j.api.value.ValueComposite;
import org.restlet.resource.ResourceException;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * JAVADOC
 */
public class SwingCommandQueryClient
   extends CommandQueryClient
{
   @Override
   public void putCommand( final String operation, final ValueComposite command ) throws ResourceException
   {
      SwingWorker worker = new SwingWorker()
      {
         @Override
         protected Object doInBackground() throws Exception
         {
            try
            {
               SwingCommandQueryClient.super.putCommand( operation, command );
            } catch (final ResourceException e)
            {
               SwingUtilities.invokeLater( new Runnable()
               {
                  public void run()
                  {
                     throw e;
                  }
               });
            }
            return null;
         }
      };

      worker.execute();
   }
}

/*
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

package se.streamsource.streamflow.client.ui;

import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.StreamflowApplication;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.source.helper.TransactionCollector;

import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;

/**
 * All Swing actions that want to trigger commands in the domain model
 * should use this base-class. It will gather upp all TransactionEvent's from the invocations
 * and distribute them to the StreamflowApplication, which in turn delegates to all the windows.
 */
public abstract class CommandTask
      extends Task<Iterable<TransactionEvents>, Object>
{
   public CommandTask()
   {
      super( Application.getInstance() );
   }

   protected abstract void command()
         throws Exception;

   @Override
   protected Iterable<TransactionEvents> doInBackground() throws Exception
   {
      StreamflowApplication application = (StreamflowApplication) getApplication();
      EventSource source = application.getSource();

      TransactionCollector collector = new TransactionCollector();
      source.registerListener( collector );

      try
      {
         command();

         return collector.transactions();
      } finally
      {
         source.unregisterListener( collector );
      }
   }

   @Override
   protected void succeeded( final Iterable<TransactionEvents> transactionEventsIterable )
   {
      final Application application = Application.getInstance();
      SwingUtilities.invokeLater( new Runnable()
      {
         public void run()
         {
            ((TransactionListener)application).notifyTransactions( transactionEventsIterable );
         }
      });
   }

   @Override
   protected void failed( Throwable throwable )
   {
      throw new Error(throwable);
   }
}

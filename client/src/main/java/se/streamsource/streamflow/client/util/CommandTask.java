/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.util;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;
import org.qi4j.api.util.Iterables;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.StreamflowApplication;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.EventStream;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

import static se.streamsource.streamflow.api.ErrorResources.*;
import static se.streamsource.streamflow.client.util.i18n.*;

/**
 * All Swing actions that want to trigger commands in the domain model
 * should use this base-class. It will gather upp all TransactionEvent's from the invocations
 * and distribute them to the StreamflowApplication, which in turn delegates to all the windows.
 */
public abstract class CommandTask
      extends Task<Iterable<TransactionDomainEvents>, Object>
      implements TransactionListener
{

   protected Object source;

   public CommandTask()
   {
      super( Application.getInstance() );

      source = null;
      try
      {
         source = EventQueue.getCurrentEvent().getSource();
      } catch (NullPointerException npe)
      {
         // STREAMFLOW-75 Unchaught exception visitor does not popup on
         // Windows
         // therefor we have to consume any nullpointer here to be able to
         // continue
      }
   }

   private List<TransactionDomainEvents> transactionDomains = new ArrayList<TransactionDomainEvents>( );

   protected abstract void command()
         throws Exception;

   @Override
   protected Iterable<TransactionDomainEvents> doInBackground() throws Exception
   {
      StreamflowApplication application = (StreamflowApplication) getApplication();
      EventStream stream = application.getSource();

      stream.registerListener( this );

      try
      {
         command();

         return transactionDomains;
      } catch ( Throwable t)
      {
         failed( t );
         return transactionDomains;
      }finally
      {
         stream.unregisterListener( this );
      }
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      Iterables.addAll( this.transactionDomains, transactions );
   }

   @Override
   protected void succeeded( final Iterable<TransactionDomainEvents> transactionEventsIterable )
   {
      final Application application = Application.getInstance();
      SwingUtilities.invokeLater( new Runnable()
      {
         public void run()
         {
            ((TransactionListener) application).notifyTransactions( transactionEventsIterable );
         }
      } );
   }

   @Override
   protected void failed( Throwable throwable )
   {
      if (throwable instanceof ResourceException)
      {
         ResourceException re = (ResourceException) throwable;
         if (re.getStatus().equals(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY) ||
               re.getStatus().equals( Status.SERVER_ERROR_INTERNAL ) ||
               //re.getStatus().equals( Status.CLIENT_ERROR_CONFLICT ) ||
               re.getStatus().equals( Status.REDIRECTION_NOT_MODIFIED ))
         {
            String exceptionMessage = re.getStatus().getDescription();
            if( valueOf( exceptionMessage ) != null )
            {
               exceptionMessage = text( valueOf( exceptionMessage ) );
            }
            // Show error dialog
            final Frame frame = source instanceof Component ? (Frame) SwingUtilities.getAncestorOfClass(Frame.class,
                  (Component) source) : ((SingleFrameApplication)Application.getInstance()).getMainFrame();

            JOptionPane.showMessageDialog(frame, new JLabel( exceptionMessage ), "", JOptionPane.ERROR_MESSAGE);
         } else if ( re.getStatus().equals(  Status.CLIENT_ERROR_CONFLICT  ) )
         {
            throw new OperationException( error, throwable );
         }

      } else if (throwable instanceof OperationException)
         throw (OperationException) throwable;
      else
         throw new OperationException( error, throwable );
   }
}

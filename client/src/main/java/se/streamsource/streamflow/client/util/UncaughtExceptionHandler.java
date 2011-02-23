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

package se.streamsource.streamflow.client.util;

import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.injection.scope.Service;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.application.error.ErrorResources;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.StreamflowApplication;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.util.dialog.DialogService;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.logging.Level;

import static se.streamsource.streamflow.client.util.i18n.text;

/**
 * JAVADOC
 */
public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler
{
   private @Service
   StreamflowApplication main;

   private @Service
   DialogService dialogs;

   private boolean processing = false;

   public void uncaughtException(Throwable e)
   {
      uncaughtException(Thread.currentThread(), e);
   }

   public void uncaughtException(Thread t, final Throwable e)
   {
      final Throwable ex = unwrap(e);

      Object source = null;
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
      final Frame frame = source instanceof Component ? (Frame) SwingUtilities.getAncestorOfClass(Frame.class,
            (Component) source) : main.getMainFrame();

      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            if (!processing)
            {
               try
               {
                  processing = true;
                  Throwable cause = ex instanceof OperationException ? ex.getCause() : ex;
                  if (cause instanceof ResourceException)
                  {
                     ResourceException re = (ResourceException) cause;
                     if (re.getStatus().equals(Status.CLIENT_ERROR_FORBIDDEN))
                     {
                        // User is not allowed to do this operation
                        JXDialog dialog = new JXDialog(frame, new JLabel(i18n
                              .text(StreamflowResources.operation_not_permitted)));
                        dialog.setLocationRelativeTo(frame);
                        dialog.pack();
                        dialog.setVisible(true);
                        main.show(dialog);
                        return;
                     } else if (re.getStatus().equals(Status.CLIENT_ERROR_CONFLICT)
                           || re.getStatus().equals(Status.CLIENT_ERROR_PRECONDITION_FAILED))
                     {
                        showErrorDialog(ex, frame, text(ErrorResources.concurrent_change));
                        return;
                     } else if (re.getStatus().equals(Status.CLIENT_ERROR_UNAUTHORIZED))
                     {
                        dialogs.showMessageDialog(frame, i18n.text(ErrorResources.unauthorized_access), "Info");
                        main.manageAccounts();
                        return;
                     } else if (re.getStatus().equals(Status.CONNECTOR_ERROR_COMMUNICATION))
                     {
                        showErrorDialog(ex, frame, text(ErrorResources.communication_error));
                        main.selectAccount();
                        return;
                     } else if (re.getStatus().equals(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY ) )
                     {
                        showErrorDialog(ex, frame, re.getMessage() + "\n" + re.getStatus().getUri(),
                              re.getStatus().getDescription());
                        return;
                     }
                     else
                     {
                        showErrorDialog(ex, frame, re.getMessage() + "\n" + re.getStatus().getUri());
                        return;
                     }
                  }

                  try
                  {
                     String message = ex.getMessage();
                     if (message != null)
                     {
                        message = HtmlErrorMessageExtractor.parse(ex.getMessage());
                        showErrorDialog(ex, frame,
                              text(StreamflowResources.valueOf(HtmlErrorMessageExtractor.parse(ex.getMessage()))));
                     } else
                     {
                        // once again in case the resource enum does not exist
                        showErrorDialog(ex, frame);
                     }
                  } catch (IllegalArgumentException iae)
                  {
                     // once again in case the resource enum does not exist -
                     // now
                     // showing non translated error msg
                     showErrorDialog(ex, frame);
                  }
               } finally
               {
                  processing = false;
               }
            }
         }
      });
   }

   private void showErrorDialog(Throwable ex, Frame frame, String errorMsg)
   {
      JXErrorPane pane = new JXErrorPane();
      pane.setErrorInfo(new ErrorInfo(text(ErrorResources.error), errorMsg, null, "Error", ex, Level.SEVERE,
            Collections.<String, String> emptyMap()));
      pane.setPreferredSize(new Dimension(700, 400));
      JXErrorPane.showDialog(frame, pane);
   }

   private void showErrorDialog(Throwable ex, Frame frame, String errorMsg, String detailedErrorMsg)
   {
      JXErrorPane pane = new JXErrorPane();
      pane.setErrorInfo(new ErrorInfo(text(ErrorResources.error), errorMsg, detailedErrorMsg, "Error", ex, Level.SEVERE,
            Collections.<String, String> emptyMap()));
      pane.setPreferredSize(new Dimension(700, 400));
      JXErrorPane.showDialog(frame, pane);
   }

   private void showErrorDialog(Throwable ex, Frame frame)
   {
      JXErrorPane pane = new JXErrorPane();
      pane.setErrorInfo(new ErrorInfo(text(ErrorResources.error), ex.getMessage(), null, "Error", ex, Level.SEVERE,
            Collections.<String, String> emptyMap()));
      pane.setPreferredSize(new Dimension(700, 400));
      JXErrorPane.showDialog(frame, pane);
   }

   private Throwable unwrap(Throwable e)
   {
      if (e instanceof OperationException)
         return e;

      if (e.getCause() != null)
      {
         if (e instanceof Error)
         {
            return unwrap(e.getCause());
         } else if (e instanceof InvocationTargetException)
         {
            return unwrap(e.getCause());
         } else if (e instanceof ConstructionException)
         {
            return e;
         } else
            return unwrap(e.getCause());
      }

      return e;
   }
}

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

package se.streamsource.streamflow.client.infrastructure.ui;

import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;
import org.qi4j.api.injection.scope.Service;
import se.streamsource.streamflow.client.StreamFlowApplication;

import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.logging.Level;

/**
 * JAVADOC
 */
public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler
{
    private
    @Service
    StreamFlowApplication main;

    public void uncaughtException(Throwable e)
    {
        uncaughtException(Thread.currentThread(), e);
    }

    public void uncaughtException(Thread t, Throwable e)
    {
        final Throwable ex = unwrap(e);

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                JXErrorPane pane = new JXErrorPane();
                pane.setErrorInfo(new ErrorInfo("Uncaught exception", ex.getMessage(), null, "Error", ex, Level.SEVERE, Collections.<String, String>emptyMap()));
                pane.setPreferredSize(new Dimension(700, 400));
                JXErrorPane.showDialog(main.getMainFrame(), pane);
            }
        });
    }

    private Throwable unwrap(Throwable e)
    {
        if (e instanceof Error)
        {
            return unwrap(e.getCause());
        } else if (e instanceof InvocationTargetException)
        {
            return unwrap(e.getCause());
        } else
        {
            return e;
        }
    }
}

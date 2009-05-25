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

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.util.EmptyStackException;

/**
 * JAVADOC
 */
public class UnitOfWorkEventQueue
        extends EventQueue
{
    protected
    @Structure
    UnitOfWorkFactory uowf;

    private
    @Service
    ContainerUnitOfWorkService cuow;

    @Override
    protected void dispatchEvent(AWTEvent event)
    {
        if (event.getSource() instanceof Component)
        {
            UnitOfWork uow = cuow.get((Component) event.getSource());
            if (uow != null)
            {
                if (uow.isPaused())
                {
                    // This window had its own UoW
                    uow.resume();

                    try
                    {
                        super.dispatchEvent(event);
                        return;
                    } finally
                    {
                        UnitOfWork afterUoW = uowf.currentUnitOfWork();

                        if (afterUoW != null)
                        {
                            afterUoW.pause();
                        }
                    }
                } else
                {
                    super.dispatchEvent(event);
                    return;
                }

            }
        } else
        {
            super.dispatchEvent(event);
            return;
        }
    }

    @Override
    public void pop() throws EmptyStackException
    {
        super.pop();
    }
}

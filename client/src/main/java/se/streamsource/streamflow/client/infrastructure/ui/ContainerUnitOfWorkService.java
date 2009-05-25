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

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;

import java.awt.Component;
import java.awt.Container;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * JAVADOC
 */
@Mixins(ContainerUnitOfWorkService.ContainerUnitOfWorkMixin.class)
public interface ContainerUnitOfWorkService
        extends ContainerUnitOfWork, ServiceComposite
{
    class ContainerUnitOfWorkMixin
            implements ContainerUnitOfWork
    {
        Map<Component, UnitOfWork> mappings = new WeakHashMap<Component, UnitOfWork>();

        public void register(Container container, UnitOfWork uow)
        {
            mappings.put(container, uow);
        }

        public void discard(Container container)
        {
            UnitOfWork uow = mappings.remove(container);
            if (uow != null && uow.isOpen())
            {
                if (uow.isPaused())
                    uow.resume();
                uow.discard();
            }
        }

        public UnitOfWork get(Component component)
        {
            while (component != null)
            {
                UnitOfWork uow = mappings.get(component);
                if (uow != null)
                    return uow;

                component = component.getParent();
            }

            return null;
        }
    }
}

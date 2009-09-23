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

package se.streamsource.streamflow.infrastructure.event;

import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.common.AppliesToFilter;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.EntityStateHolder;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.spi.Qi4jSPI;
import se.streamsource.streamflow.domain.roles.Removable;

import java.beans.Introspector;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic mixin for simple command methods that remove an entity from a collection. They have to follow this pattern:
 * boolean removeFoo(SomeType entity)
 * This will check if the entity exists in the ManyAssociation called "foos" and then call the event method
 * fooRemoved(DomainEvent.CREATE, entity);
 * The method returns true if the entity was removed, and false if not.
 */
@AppliesTo(CommandEntityRemoveMixin.CommandEntityRemoveAppliesTo.class)
public class CommandEntityRemoveMixin
        implements InvocationHandler
{
    private static Map<Method, Method> methodMappings = new ConcurrentHashMap();
    private static Map<Method, Method> manyAssociationMappings = new ConcurrentHashMap();

    @Service
    IdentityGenerator idGen;

    @State
    EntityStateHolder state;

    @Structure
    UnitOfWorkFactory uowf;

    @Structure
    Qi4jSPI spi;

    @This
    EntityComposite composite;

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        //Method eventMethod = methodMappings.get(method);
        Method eventMethod;
        //if (eventMethod == null)
        {
            // removeFoo -> fooRemoved
            String name = method.getName().substring("remove".length());
            name = Introspector.decapitalize(name) + "Removed";
            Class[] parameterTypes = new Class[]{DomainEvent.class, method.getParameterTypes()[0]};
            eventMethod = composite.getClass().getMethod(name, parameterTypes);
            //methodMappings.put(method, eventMethod);
        }

        //Method manyAssociationMethod = manyAssociationMappings.get(method);
        Method manyAssociationMethod;
        //if (manyAssociationMethod == null)
        {
            // removeFoo -> foos
            String name = method.getName().substring("remove".length());
            name = Introspector.decapitalize(name) + "s";
            manyAssociationMethod = composite.getClass().getMethod(name);
            //manyAssociationMappings.put(method, manyAssociationMethod);
        }

        ManyAssociation manyAssociation = (ManyAssociation) manyAssociationMethod.invoke(proxy);

        if (!manyAssociation.contains(args[0]))
                return false; // ManyAssociation does not contain entity

        eventMethod.invoke(composite, DomainEvent.CREATE, args[0]);

        // Call Removable.removeEntity()
        if (args[0] instanceof Removable)
        {
            Removable removable = (Removable) args[0];
            removable.removeEntity();
        }

        return true;
    }

    public static class CommandEntityRemoveAppliesTo
            implements AppliesToFilter
    {
        public boolean appliesTo(Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass)
        {
            return method.getName().startsWith("remove") && method.getParameterTypes().length == 1;
        }
    }
}
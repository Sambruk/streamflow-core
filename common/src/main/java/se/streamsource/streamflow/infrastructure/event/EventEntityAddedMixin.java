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
import org.qi4j.api.entity.association.EntityStateHolder;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

import java.beans.Introspector;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic mixin for simple event methods that add an entity to a collection. They have to follow this pattern:
 * void addedFoo(DomainEvent event, SomeType)
 * This will add the entity (2nd parameter) to the ManyAssociation called "foos".
 */
@AppliesTo(EventEntityAddedMixin.EventEntityAddedAppliesTo.class)
public class EventEntityAddedMixin
        implements InvocationHandler
{
    private static Map<Method, Method> methodMappings = new ConcurrentHashMap();

    @State
    EntityStateHolder state;

    @Structure
    UnitOfWorkFactory uowf;

    @This
    EntityComposite composite;

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        Method manyAssociationMethod = methodMappings.get(method);
        if (manyAssociationMethod == null)
        {
            // Find ManyAssociation method
            String addedName = Introspector.decapitalize( method.getName().substring("added".length())) + "s";
            manyAssociationMethod = composite.getClass().getInterfaces()[0].getMethod(addedName);
            methodMappings.put(method, manyAssociationMethod);
        }

        // Create entity
        EntityComposite entity = (EntityComposite) args[1];

        // Lookup the ManyAssociation
        ManyAssociation<Object> manyAssociation = state.getManyAssociation(manyAssociationMethod);

        // Add entity to ManyAssociation
        manyAssociation.add(entity);

        return entity;
    }

    public static class EventEntityAddedAppliesTo
            implements AppliesToFilter
    {
        public boolean appliesTo(Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass)
        {
            return method.getParameterTypes().length == 2 &&
                    method.getParameterTypes()[0].equals(DomainEvent.class) && method.getName().startsWith("added") &&
                    method.getReturnType().equals(Void.TYPE);
        }
    }
}
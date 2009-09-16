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
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic mixin for simple event methods that create an entity and add it to a collection. They have to follow this pattern:
 * SomeType fooCreated(DomainEvent event, String id)
 * This will instantiate an EntityComposite with the "SomeType" type and id "id",
 * and then add it to the ManyAssociation called "foos". The new entity is then returned from the method.
 */
@AppliesTo(EventEntityCreatedMixin.EventEntityCreatedAppliesTo.class)
public class EventEntityCreatedMixin
        implements InvocationHandler
{
    private static Map<Method, Method> methodMappings = new ConcurrentHashMap();

    @State
    EntityStateHolder state;

    @Structure
    UnitOfWorkFactory uowf;

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        Method manyAssociationMethod = methodMappings.get(method);
        if (manyAssociationMethod == null)
        {
            // Find ManyAssociation method
            manyAssociationMethod = method.getDeclaringClass().getMethod(method.getName().substring(0, method.getName().length() - "Created".length())+"s");
            methodMappings.put(method, manyAssociationMethod);
        }

        // Create entity
        EntityComposite entity = (EntityComposite) uowf.currentUnitOfWork().newEntity(method.getReturnType(), (String) args[1]);

        // Lookup the ManyAssociation
        ManyAssociation<Object> manyAssociation = state.getManyAssociation(manyAssociationMethod);

        // Add entity to ManyAssociation
        manyAssociation.add(entity);

        return entity;
    }

    public static class EventEntityCreatedAppliesTo
            implements AppliesToFilter
    {
        public boolean appliesTo(Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass)
        {
            return method.getParameterTypes().length == 2 &&
                    method.getParameterTypes()[0].equals(DomainEvent.class) && method.getName().endsWith("Created") &&
                    !method.getReturnType().equals(Void.TYPE);
        }
    }
}
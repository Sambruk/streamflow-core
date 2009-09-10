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
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.property.StateHolder;

import java.beans.Introspector;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic mixin for simple command methods that update a property.
 *
 * It can implement methods that follow these rules:
 * * The method must have one parameter
 * * The name must start with "change"
 *
 * Example: void changeFoo(String newValue) -> invoke event method "fooChanged(newValue);"
 *
 */
@AppliesTo(CommandPropertyChangeMixin.CommandPropertyChangeAppliesTo.class)
public class CommandPropertyChangeMixin
        implements InvocationHandler
{
    private static Map<Method, Method> methodMappings = new ConcurrentHashMap();

    @State
    StateHolder state;

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        Method eventMethod = methodMappings.get(method);

        if (eventMethod == null)
        {
            // changeFoo -> fooChanged
            String name = method.getName().substring("change".length());
            name = Introspector.decapitalize(name) + "Changed";
            Class[] parameterTypes = new Class[]{DomainEvent.class, method.getParameterTypes()[0]};
            eventMethod = proxy.getClass().getMethod(name, parameterTypes);
            methodMappings.put(method, eventMethod);
        }

        eventMethod.invoke(proxy, DomainEvent.CREATE, args[0]);

        return null;
    }

    public static class CommandPropertyChangeAppliesTo
            implements AppliesToFilter
    {
        public boolean appliesTo(Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass)
        {
            return method.getParameterTypes().length == 1 && method.getName().startsWith("change");
        }
    }
}
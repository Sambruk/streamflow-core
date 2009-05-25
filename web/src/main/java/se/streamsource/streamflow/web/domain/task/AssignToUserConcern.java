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

package se.streamsource.streamflow.web.domain.task;

import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.common.AppliesToFilter;
import org.qi4j.api.concern.GenericConcern;
import org.qi4j.api.injection.scope.This;

import java.lang.reflect.Method;

/**
 * If the first parameter to a call is Assignable, then assign it to the user
 * represented by "this".
 */
@AppliesTo(AssignToUserConcern.AssignToUserAppliesTo.class)
public class AssignToUserConcern
        extends GenericConcern
{
    @This
    Assignee assignee;

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        Assignable assignable = (Assignable) args[0];
        assignable.assignTo(assignee);
        return next.invoke(proxy, method, args);
    }

    public static class AssignToUserAppliesTo
            implements AppliesToFilter
    {
        public boolean appliesTo(Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass)
        {
            return method.getParameterTypes().length >= 1 &&
                    Assignable.class.isAssignableFrom(method.getParameterTypes()[0]);
        }
    }

}

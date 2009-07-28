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
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.sideeffect.GenericSideEffect;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

import java.lang.reflect.Method;
import java.util.Date;

/**
 * Generate event
 */
@AppliesTo(Event.class)
public class EventSideEffect
    extends GenericSideEffect
{
    @This
    Identity identity;

    @Structure
    ValueBuilderFactory vbf;

    @Service
    IdentityGenerator idGenerator;

    @Override
    protected void invoke(Method method, Object[] args) throws Throwable
    {
        ValueBuilder<DomainEvent> builder = vbf.newValueBuilder(DomainEvent.class);

        DomainEvent prototype = builder.prototype();
        prototype.name().set(method.getName());
        prototype.on().set(new Date());
        prototype.entity().set(identity.identity().get());
        prototype.by().set("anonymous"); // TODO
        prototype.identity().set(idGenerator.generate(DomainEvent.class));

        DomainEvent event = builder.newInstance();

        System.out.println(event.toJSON());
    }
}

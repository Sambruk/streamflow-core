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

package se.streamsource.streamflow.web.infrastructure.event;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * Tester of domain events and commands.
 *
 * All methods are using the generic mixins which
 * provide default implementations of entity create/remove
 * and property change.
 */
public interface Tester
{
    TestEntity createTester();
    boolean removeTester(Tester entity);

    void changeProp(String aParameter);

    interface TestState
    {
        ManyAssociation<Tester> testers();

        @Optional
        Property<String> prop();

        TestEntity testerCreated(DomainEvent event, String id);
        void testerAdded(DomainEvent event, TestEntity tester);
        void testerRemoved(DomainEvent event, Tester tester);

        void propChanged(DomainEvent event, String aParameter);
    }
}

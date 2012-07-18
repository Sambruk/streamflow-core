/**
 *
 * Copyright 2009-2012 Jayway Products AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.infrastructure.event.domain;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

import java.util.Date;

/**
 * Representation of a domain-event. An event is triggered by calling a method
 * that is of the form:
 *
 * void someName(DomainEvent event, SomeParam param);
 * <p/>
 * The "event" argument should be invoked with null, as it will be created during
 * the method call. If it is not null, then the method call is a replay of previously
 * created events.
 */
public interface DomainEvent
      extends ValueComposite, Identity
{
   // Usecase

   Property<String> usecase();

   // Name of method/event

   Property<String> name();

   // Id of the entity that generated the event

   Property<String> entity();

   // When the event occurred

   Property<Date> on();

   // Who performed the event

   @Optional
   Property<String> by();

   // Method parameters as JSON

   Property<String> parameters();

   // Type of the entity being invoked

   Property<String> entityType();

   // Version of the application

   Property<String> version();

   // Dummy event class
}

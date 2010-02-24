/*
 * Copyright (c) 2009, Arvid Huss. All Rights Reserved.
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

package se.streamsource.streamflow.resource.conversation;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import se.streamsource.streamflow.infrastructure.application.LinkValue;

import java.util.Date;

public interface ConversationDTO
   extends LinkValue
{
   Property<EntityReference> creator();

   Property<Date> creationDate();

   Property<Integer> messages();

   Property<Integer> participants();
}

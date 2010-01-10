/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.web.domain.interaction.gtd;

import se.streamsource.streamflow.web.domain.interaction.gtd.HasStatus;
import se.streamsource.streamflow.domain.interaction.gtd.States;
import static se.streamsource.streamflow.domain.interaction.gtd.States.ACTIVE;
import static se.streamsource.streamflow.domain.interaction.gtd.States.DONE;

/**
 * JAVADOC
 */
public interface Completable
{
   @HasStatus(ACTIVE)
   void complete();

   @HasStatus(ACTIVE)
   void done();

   @HasStatus(DONE)
   void finish();

   @HasStatus({ACTIVE, DONE})
   void drop();

   @HasStatus(States.COMPLETED)
   void reopen();
}

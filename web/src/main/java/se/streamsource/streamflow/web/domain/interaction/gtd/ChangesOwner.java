/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.web.domain.interaction.gtd;

import org.qi4j.api.sideeffect.*;

import java.lang.annotation.*;

/**
 * Add this to methods which when invoked change the ownership
 * of the first argument to the entity being invoked.
 *
 * Example:
 * bar.addFoo(Foo foo);
 * -> invoke changeOwner on foo with bar as Owner.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@SideEffects(ChangeOwnerSideEffect.class)
public @interface ChangesOwner
{
}

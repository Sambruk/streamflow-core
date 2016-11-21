/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.domain.interaction.gtd;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.qi4j.api.sideeffect.SideEffects;

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

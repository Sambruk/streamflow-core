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

package se.streamsource.dci.context;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.context.Context;

/**
 * JAVADOC
 */
public abstract class ContextMixin
   implements Context
{
   protected @Uses
   InteractionContext context;

   protected @Structure
   Module module;

   public InteractionContext context()
   {
      return context;
   }

   protected <T extends Context> T subContext( Class<T> compositeClass )
   {
      return module.transientBuilderFactory().newTransientBuilder( compositeClass ).use( context ).newInstance();
   }
}

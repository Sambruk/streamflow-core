/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.dci.api;

import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;

/**
 * All mixins with interactions should extend this base class. It primarily provides
 * easy access to the Context and makes it possible to create new sub-contexts.
 */
public abstract class InteractionsMixin
{
   public
   @Uses
   Context context;

   protected
   @Structure
   Module module;

   protected <T> T subContext( Class<T> interactionsClass )
   {
      Context subContext = new Context( context );

      module.unitOfWorkFactory().currentUnitOfWork().metaInfo().set( subContext );

      if (TransientComposite.class.isAssignableFrom( interactionsClass ))
         return module.transientBuilderFactory().newTransientBuilder( interactionsClass ).use( subContext ).newInstance();
      else
         return module.objectBuilderFactory().newObjectBuilder( interactionsClass ).use( subContext ).newInstance();
   }
}

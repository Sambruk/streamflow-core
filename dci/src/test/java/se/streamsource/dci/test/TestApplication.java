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

package se.streamsource.dci.test;

import org.apache.velocity.app.VelocityEngine;
import org.qi4j.api.injection.scope.Service;
import se.streamsource.dci.context.InteractionContext;
import se.streamsource.dci.restlet.server.CommandQueryServerResource;
import se.streamsource.dci.test.context.RootContext;

/**
 * JAVADOC
 */
public class TestApplication
   extends CommandQueryServerResource
{
   public TestApplication( @Service VelocityEngine templates )
         throws Exception
   {
      super( templates );
   }

   @Override
   protected Object getRoot( InteractionContext context )
   {
      return tbf.newTransientBuilder( RootContext.class ).use(context).newInstance();
   }
}

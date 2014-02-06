/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.web.application.console.commands;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.EntityStateHolder;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.spi.Qi4jSPI;

import bsh.CallStack;
import bsh.Interpreter;

/**
 * JAVADOC
 */
public class state
{
   public static void invoke( final Interpreter env, CallStack callstack, Object entity )
   {
      try
      {
         Qi4jSPI qi4j = (Qi4jSPI) env.get( "qi4j" );

         env.println( entity.toString() );

         EntityStateHolder state = qi4j.getState( (EntityComposite) entity );

         state.visitState( new EntityStateHolder.EntityStateVisitor()
         {
            public void visitAssociation( QualifiedName name, Object association )
            {
               env.println( name.name() + ": " + association );
            }

            public void visitManyAssociation( QualifiedName name, ManyAssociation association )
            {
            }

            public void visitProperty( QualifiedName name, Object value )
            {
               env.println( name.name() + ": " + value );
            }
         } );
      } catch (Throwable throwable)
      {
         throwable.printStackTrace();
      }
   }
}

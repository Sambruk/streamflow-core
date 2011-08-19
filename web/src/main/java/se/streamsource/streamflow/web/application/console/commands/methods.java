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

package se.streamsource.streamflow.web.application.console.commands;

import bsh.CallStack;
import bsh.Interpreter;

import java.lang.reflect.Method;

/**
 * JAVADOC
 */
public class methods
{
   public static void invoke( final Interpreter env, CallStack callstack, Object entity )
   {
      try
      {
         env.println( entity.toString() );
         for (Method method : entity.getClass().getMethods())
         {
            env.println( method.toGenericString() );
         }
      } catch (Throwable throwable)
      {
         throwable.printStackTrace();
      }
   }
}
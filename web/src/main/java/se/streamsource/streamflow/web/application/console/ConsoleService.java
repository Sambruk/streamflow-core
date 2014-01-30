/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
package se.streamsource.streamflow.web.application.console;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.qi4j.api.Qi4j;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilder;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * JAVADOC
 */
@Mixins(ConsoleService.Mixin.class)
public interface ConsoleService
      extends Console, ServiceComposite
{
   class Mixin
         implements Activatable, Console
   {
      @Structure
      Module module;

      @Structure
      Qi4j qi4j;

      public void activate() throws Exception
      {
      }

      public void passivate() throws Exception
      {
      }

      public ConsoleResultValue executeScript( ConsoleScriptValue script ) throws Exception
      {
         if (script.language().get() == Console.Language.BEANSHELL)
         {
            return executeBeanshell( script );
         }

         return null;
      }

      private ConsoleResultValue executeBeanshell( ConsoleScriptValue script ) throws Exception
      {
         ValueBuilder<ConsoleResultValue> builder = module.valueBuilderFactory().newValueBuilder(ConsoleResultValue.class);

         Interpreter interpreter = new Interpreter();

         // Bind default stuff
         UnitOfWork unitOfWork = module.unitOfWorkFactory().newUnitOfWork(UsecaseBuilder.newUsecase("Script"));
         interpreter.set( "uow", unitOfWork );
         interpreter.set( "query", module.queryBuilderFactory() );
         interpreter.set( "services", module.serviceFinder() );
         interpreter.set( "qi4j", qi4j );

         // Import commands
         interpreter.eval( "importCommands(\"se.streamsource.streamflow.web.application.console.commands\");" );

         // Bind given values
         for (Map.Entry<String, Object> entry : script.bindings().get().entrySet())
         {
            interpreter.set( entry.getKey(), entry.getValue() );
         }

         // Replace output streams
         ByteArrayOutputStream stream = new ByteArrayOutputStream();
         PrintStream out = new PrintStream( stream, true, "UTF-8" );

         interpreter.setOut( out );
         interpreter.setErr( out );

         // Add log handler
         Logger root = Logger.getLogger( "" );
         final List<LogRecord> log = builder.prototype().log().get();
         Handler handler = new Handler()
         {
            public void publish( LogRecord record )
            {
               log.add( record );
            }

            public void flush()
            {
            }

            public void close() throws SecurityException
            {
            }
         };
         root.addHandler( handler );

         // Eval script
         try
         {
            interpreter.eval( script.script().get() );

            builder.prototype().out().set( new String( stream.toByteArray(), "UTF-8" ) );

            if (script.completeUnitOfWork().get())
            {
               unitOfWork.complete();
            } else
            {
               unitOfWork.discard();
            }

         } catch (EvalError evalError)
         {
            evalError.printStackTrace();
            builder.prototype().out().set( evalError.toString() );
         } finally
         {

            // Remove handler
            root.removeHandler( handler );
         }

         return builder.newInstance();
      }
   }
}

/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.util;

import org.qi4j.api.specification.Specification;
import org.qi4j.api.util.Function;
import org.qi4j.api.util.Iterables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * TODO
 */
public final class ForEach<T>
      implements Iterable<T>
{
   public static <T> ForEach<T> forEach(Iterable<T> iterable)
   {
      return new ForEach<T>(iterable);
   }

   private Iterable<T> iterable;

   public ForEach(Iterable<T> iterable)
   {
      this.iterable = iterable;
   }

   public Iterator<T> iterator()
   {
      return iterable.iterator();
   }

   public ForEach<T> filter(Specification<T> specification)
   {
      return new ForEach<T>(Iterables.filter(specification, iterable));
   }

   public <TO> ForEach<TO> map(Function<T, TO> function)
   {
      return new ForEach<TO>(Iterables.map(function, iterable));
   }

   public <ThrowableType extends Throwable> boolean visit(final Visitor<T, ThrowableType> visitor)
         throws ThrowableType
   {
      for (T item : iterable)
      {
         if (!visitor.visit(item))
            return false;
      }

      return true;
   }

   public <ThrowableType extends Throwable> boolean visit(final Visitor<T, ThrowableType> visitor, ExecutorService executor)
         throws ThrowableType
   {
      // Execute visiting in parallel
      List<Future<Boolean>> tasks = new ArrayList<Future<Boolean>>();
      for (final T item : iterable)
      {
         tasks.add(executor.submit(new Callable<Boolean>()
         {
            public Boolean call() throws Exception
            {
               try
               {
                  return visitor.visit(item);
               } catch (Exception exception)
               {
                  throw exception;
               } catch (Throwable exception)
               {
                  throw (RuntimeException) exception;
               }
            }
         }));
      }

      // Check result
      boolean ok = true;
      for (Future<Boolean> task : tasks)
      {
         try
         {
            ok &= task.get();
         } catch (InterruptedException e)
         {
            e.printStackTrace();
         } catch (ExecutionException e)
         {
            throw (ThrowableType) e.getCause();
         }
      }

      return ok;
   }
}

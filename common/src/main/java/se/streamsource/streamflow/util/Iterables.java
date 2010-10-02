/**
 *
 * Copyright 2009-2010 Streamsource AB
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

import java.util.Arrays;
import java.util.Iterator;

/**
 * Utility methods for working with Iterables
 */
public class Iterables
{
   public static <X> Iterable<X> filter( Predicate<X> predicate, Iterable<X> i )
   {
      return new FilterIterable<X>( i, predicate );
   }

   public static <X> Iterable<X> flatten( Iterable<X>... multiIterator )
   {
      return new FlattenIterable<X>( Arrays.asList( multiIterator) );
   }

   public interface Predicate<T>
   {
      boolean eval(T object);
   }

   private static class FilterIterable<T> implements Iterable<T>
   {
      private Iterable<T> iterable;

      private Predicate<T> predicate;

      public FilterIterable( Iterable<T> iterable, Predicate<T> predicate)
      {
         this.iterable = iterable;
         this.predicate = predicate;
      }

      public Iterator<T> iterator()
      {
         return new FilterIterator<T>( iterable.iterator(), predicate );
      }

      static class FilterIterator<T> implements Iterator<T>
      {
         private Iterator<T> iterator;

         private Predicate<T> predicate;

         private T currentValue;
         boolean finished = false;
         boolean nextConsumed = true;

         public FilterIterator( Iterator<T> iterator, Predicate<T> predicate )
         {
            this.predicate = predicate;
            this.iterator = iterator;
         }

         public boolean moveToNextValid()
         {
            boolean found = false;
            while (!found && iterator.hasNext())
            {
               T currentValue = iterator.next();
               if (predicate.eval( currentValue ))
               {
                  found = true;
                  this.currentValue = currentValue;
                  nextConsumed = false;
               }
            }
            if (!found)
            {
               finished = true;
            }
            return found;
         }

         public T next()
         {
            if (!nextConsumed)
            {
               nextConsumed = true;
               return currentValue;
            } else
            {
               if (!finished)
               {
                  if (moveToNextValid())
                  {
                     nextConsumed = true;
                     return currentValue;
                  }
               }
            }
            return null;
         }

         public boolean hasNext()
         {
            return !finished &&
                  (!nextConsumed || moveToNextValid());
         }

         public void remove()
         {
         }
      }
   }

   private static class FlattenIterable<T> implements Iterable<T>
   {
      private Iterable<Iterable<T>> iterable;

      public FlattenIterable( Iterable<Iterable<T>> iterable )
      {
         this.iterable = iterable;
      }

      public Iterator<T> iterator()
      {
         return new FlattenIterator<T>( iterable.iterator() );
      }

      static class FlattenIterator<T> implements Iterator<T>
      {
         private Iterator<Iterable<T>> iterator;
         private Iterator<T> currentIterator;

         public FlattenIterator( Iterator<Iterable<T>> iterator )
         {
            this.iterator = iterator;
            currentIterator = null;
         }

         public boolean hasNext()
         {
            if (currentIterator == null)
            {
               if (iterator.hasNext())
               {
                  currentIterator = iterator.next().iterator();
               } else
               {
                  return false;
               }
            }

            while (!currentIterator.hasNext() &&
                  iterator.hasNext())
            {
               currentIterator = iterator.next().iterator();
            }

            return currentIterator.hasNext();
         }

         public T next()
         {
            return currentIterator.next();
         }

         public void remove()
         {
            if (currentIterator == null)
               throw new IllegalStateException();

            currentIterator.remove();
         }
      }
   }
}

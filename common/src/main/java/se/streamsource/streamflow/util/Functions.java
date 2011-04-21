package se.streamsource.streamflow.util;

import org.qi4j.api.util.Function;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods for Function
 */
public class Functions
{
   public static <T> Comparator<T> comparator( final Function<T, Comparable> comparableFunction)
   {
       return new Comparator<T>()
       {
           Map<T, Comparable> compareKeys = new HashMap<T, Comparable>();

           public int compare( T o1, T o2 )
           {
               Comparable key1 = compareKeys.get( o1 );
               if (key1 == null)
               {
                   key1 = comparableFunction.map( o1 );
                   compareKeys.put(o1, key1);
               }

               Comparable key2 = compareKeys.get( o2 );
               if (key2 == null)
               {
                   key2 = comparableFunction.map( o2 );
                   compareKeys.put(o2, key2);
               }

               return key1.compareTo( key2 );
           }
       };
   }
}

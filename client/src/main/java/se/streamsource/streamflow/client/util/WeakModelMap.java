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
package se.streamsource.streamflow.client.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Map that helps maintain models. The model instances are weakly referenced
 * so that they may be GC'ed if noone is using them outside of the map.
 */
public abstract class WeakModelMap<K, V>
      implements Iterable<V>
{
   Map<K, WeakReference<V>> models = new HashMap<K, WeakReference<V>>();

   /**
    * Get model for the corresponding key. If none exists
    * then it will be instantiated on the fly.
    *
    * @param key model key
    * @return model for key
    */
   public V get( K key )
   {
      WeakReference<V> model = models.get( key );
      if (model == null || model.get() == null)
      {
         model = new WeakReference<V>( newModel( key ) );
         models.put( key, model );
      }
      return model.get();
   }

   /**
    * Implement this method to instantiate a new model given
    * the key that describes the model.
    *
    * @param key model key
    * @return new model
    */
   abstract protected V newModel( K key );

   public void clear()
   {
      models.clear();
   }

   public Iterator<V> iterator()
   {
      List<V> list = new ArrayList<V>();
      for (WeakReference<V> vWeakReference : models.values())
      {
         if (vWeakReference.get() != null)
            list.add( vWeakReference.get() );
      }
      return list.iterator();
   }

   public void remove( K key )
   {
      models.remove( key );
   }
}

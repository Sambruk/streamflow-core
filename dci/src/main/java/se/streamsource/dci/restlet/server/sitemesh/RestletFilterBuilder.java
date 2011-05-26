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

package se.streamsource.dci.restlet.server.sitemesh;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Filter;
import org.sitemesh.builder.BaseSiteMeshBuilder;

/**
 * JAVADOC
 */
public class RestletFilterBuilder
        extends BaseSiteMeshBuilder<RestletFilterBuilder, RestletContext, Filter>
{
   Context context;
   Restlet next;

   public RestletFilterBuilder setContext( Context context )
   {
      this.context = context;
      return this;
   }

   public RestletFilterBuilder setNext( Restlet next )
   {
      this.next = next;
      return this;
   }

   /**
     * Create the SiteMesh Filter.
     */
    public Filter create() {
        return new SiteMeshRestletFilter(context, next,
                getContentProcessor(),
                getDecoratorSelector());
    }

}

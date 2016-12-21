/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.rest.resource;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.CacheDirective;
import org.restlet.routing.Filter;

/**
 * Add the noCache HTTP header info to the response
 * 
 * @author henrikreinhold 
 */
class NoCacheFilter extends Filter
{
   public NoCacheFilter( Context context, Restlet restlet )
   {
      super( context, restlet );
   }

   @Override
   protected int doHandle(Request request, Response response)
   {
      int result = super.doHandle( request, response );
      response.getCacheDirectives().add(CacheDirective.noCache());
      return result;
   }
}

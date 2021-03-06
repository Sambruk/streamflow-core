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
package se.streamsource.dci.restlet.server;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Preference;
import org.restlet.routing.Filter;
import org.restlet.service.MetadataService;

import java.util.Collections;
import java.util.List;

/**
 * JAVADOC
 */
public class ExtensionMediaTypeFilter
    extends Filter
{

    public ExtensionMediaTypeFilter()
    {
    }

    public ExtensionMediaTypeFilter( Context context )
    {
        super( context );
    }

    public ExtensionMediaTypeFilter( Context context, Restlet next )
    {
        super( context, next );
    }

    @Override
    protected int beforeHandle( Request request, Response response )
    {
       List<String> segments = request.getResourceRef().getSegments();
       if (segments.get( segments.size()-1 ).equals(""))
         return Filter.CONTINUE;

        String extensions = request.getResourceRef().getExtensions();
        if( extensions != null )
        {
            int idx = extensions.lastIndexOf( "." );
            if( idx != -1 )
            {
                extensions = extensions.substring( idx + 1 );
            }

            MetadataService metadataService = getApplication().getMetadataService();
            Metadata metadata = metadataService.getMetadata( extensions );
            if( metadata != null && metadata instanceof MediaType)
            {
                request.getClientInfo()
                    .setAcceptedMediaTypes( Collections.singletonList( new Preference<MediaType>( (MediaType) metadata ) ) );
                String path = request.getResourceRef().getPath();
                path = path.substring( 0, path.length() - extensions.length() - 1 );
                request.getResourceRef().setPath( path );
            }
        }

        return Filter.CONTINUE;
    }
}
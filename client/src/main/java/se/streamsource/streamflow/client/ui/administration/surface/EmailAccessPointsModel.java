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

package se.streamsource.streamflow.client.ui.administration.surface;

import org.qi4j.api.value.ValueBuilder;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.value.*;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.util.LinkValueListModel;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

/**
 * TODO
 */
public class EmailAccessPointsModel
   extends LinkValueListModel
        implements TransactionListener
{
   public EmailAccessPointModel createEmailAccessPointModel(LinkValue detailLink)
   {
      return module.objectBuilderFactory().newObjectBuilder(EmailAccessPointModel.class).use(client.getClient( detailLink )).newInstance();
   }

   public void create( String name )
   {
      ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder( StringValue.class );
      builder.prototype().string().set( name );
      try
      {
         client.postCommand( "create", builder.newInstance() );
      } catch (ResourceException e)
      {
         handleException( e );
      }
   }
}

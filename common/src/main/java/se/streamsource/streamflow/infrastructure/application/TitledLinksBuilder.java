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

package se.streamsource.streamflow.infrastructure.application;

import org.qi4j.api.value.*;
import se.streamsource.dci.value.link.*;

/**
 * An extension of LinksBuilder to make it handle TitledLinksValue
 */
public class TitledLinksBuilder
   extends LinksBuilder
{
   public TitledLinksBuilder( ValueBuilderFactory vbf )
   {
      super(vbf);
      linksBuilder = vbf.newValueBuilder( TitledLinksValue.class );
   }

   public void addTitle( String title )
   {
      TitledLinksValue links = (TitledLinksValue) linksBuilder.prototype();
      links.title().set( title );
   }

   /*@Override
   public TitledLinksValue newLinks()
   {
      return (TitledLinksValue) super.newLinks();
   } */
}
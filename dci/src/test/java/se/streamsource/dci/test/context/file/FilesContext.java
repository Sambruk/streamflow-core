/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.dci.test.context.file;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.IndexContext;
import se.streamsource.dci.context.SubContexts;
import se.streamsource.dci.value.LinksBuilder;
import se.streamsource.dci.value.LinksValue;

import java.io.File;

/**
 * JAVADOC
 */
@Mixins(FilesContext.Mixin.class)
public interface FilesContext
   extends Context, IndexContext<LinksValue>, SubContexts<FileContext>
{
   abstract class Mixin
      extends ContextMixin
      implements FilesContext
   {
      public LinksValue index()
      {
         File file = new File("").getAbsoluteFile();

         LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory()).rel( "file" );

         for (String fileName : file.list())
         {
            builder.addLink( fileName, fileName );
         }

         return builder.newLinks();
      }

      public FileContext context( String id )
      {
         context.playRoles( new File(id) );

         return subContext( FileContext.class );
      }
   }
}
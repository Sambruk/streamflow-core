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
package se.streamsource.streamflow.web.domain.structure.attachment;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

import java.util.Date;

/**
 * Settings for where an attached file reside
 */
public interface AttachedFile
{
   /**
    * Change filename, such as "MyDocument.doc"
    *
    * @param name of the file
    */
   void changeName(String name);

   /**
    * Change mimetype of the file. Example: "text/xml"
    *
    * @param mimeType of the file
    */
   void changeMimeType(String mimeType);

   /**
    * Change the URI where the binary data of this attachment is stored.
    * This URI is opaque and so it's "filename" does not have to be the same as this file.
    *
    * @param uri of the file
    */
   void changeUri(String uri);

   /**
    * Change the last modification date of the file.
    *
    * @param date last modification date of the file
    */
   void changeModificationDate( Date date);

   /**
    * Change the size of the file.
    *
    * @param size of the file
    */
   void changeSize(long size);

   interface Data
   {
      @UseDefaults
      Property<String> name();

      @UseDefaults
      Property<String> mimeType();

      @UseDefaults
      Property<String> uri();

      @Optional
      Property<Date> modificationDate();

      @Optional
      Property<Long> size();

      void changedName( @Optional DomainEvent event, String newName);
      void changedMimeType( @Optional DomainEvent event, String newMimeType);
      void changedUri( @Optional DomainEvent event, String newMimeType);
      void changedModificationDate( @Optional DomainEvent event, Date newModificationDate);
      void changedSize(@Optional DomainEvent event, long size);
   }
}

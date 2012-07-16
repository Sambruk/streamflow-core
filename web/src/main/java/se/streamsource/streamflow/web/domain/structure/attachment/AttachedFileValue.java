/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

import java.util.Date;

/**
 * Representation of an attached file. Maintains all the metainfo about the attachment.
 */
public interface AttachedFileValue
   extends ValueComposite
{
   Property<String> name();

   @Optional
   Property<String> mimeType();

   /**
    * URI where the binary data is stored. The protocol has to be "store:"
    * @return
    */
   Property<String> uri();

   Property<Date> modificationDate();

   Property<Long> size();
}

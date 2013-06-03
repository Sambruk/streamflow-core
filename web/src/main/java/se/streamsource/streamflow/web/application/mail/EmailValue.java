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
package se.streamsource.streamflow.web.application.mail;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFileValue;

import java.util.List;
import java.util.Map;

/**
 * Represents an email
 */
public interface EmailValue
   extends ValueComposite
{
   @Optional Property<String> fromName();
   @Optional Property<String> from();
   @Optional Property<String> replyTo();
   Property<String> to();
   @Optional Property<String> subject();
   Property<String> content();
   Property<String> contentType();
   @Deprecated @Optional Property<String> contentHtml();
   @Optional Property<String> messageId();

   @UseDefaults
   Property<List<AttachedFileValue>> attachments();

   @UseDefaults
   Property<Map<String,String>> headers();

   @Deprecated @Optional @UseDefaults
   Property<String> footer();
}

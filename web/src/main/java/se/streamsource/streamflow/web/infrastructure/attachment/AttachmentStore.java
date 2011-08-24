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

package se.streamsource.streamflow.web.infrastructure.attachment;

import org.qi4j.api.io.Input;
import se.streamsource.streamflow.util.Visitor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Store for the binary part of an attachment
 */
public interface AttachmentStore
{
   /**
    * Read the binary data and save, and return a URL
    * pointing to the file.
    *
    * @param input of binary data for the file
    * @return id for the file
    */
   String storeAttachment( Input<ByteBuffer, IOException> input) throws IOException;

   Input<ByteBuffer, IOException> attachment(String id) throws FileNotFoundException;

   void attachment(String id, Visitor<InputStream, IOException> visitor) throws IOException;

   Input<String, IOException> text(String id) throws FileNotFoundException;

   void deleteAttachment(String id) throws IOException;

   long getAttachmentSize(String id) throws IOException;
}

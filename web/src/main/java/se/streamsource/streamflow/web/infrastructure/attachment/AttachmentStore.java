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

import java.io.IOException;
import java.io.InputStream;

/**
 * Store for the binary part of an attachment
 */
public interface AttachmentStore
{
   /**
    * Read the binary data and save, and return a URL
    * pointing to the file.
    *
    * @param in stream of binary data for the file
    * @return id for the file
    */
   String storeAttachment( InputStream in) throws IOException;

   InputStream getAttachment(String id) throws IOException;

   void deleteAttachment(String id) throws IOException;
}

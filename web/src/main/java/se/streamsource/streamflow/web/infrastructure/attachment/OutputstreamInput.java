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
package se.streamsource.streamflow.web.infrastructure.attachment;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.qi4j.api.io.Input;
import org.qi4j.api.io.Output;
import org.qi4j.api.io.Receiver;
import org.qi4j.api.io.Sender;

import se.streamsource.streamflow.util.Visitor;

/**
 * An Input that allows a Visitor to write to an OutputStream. The stream is a BufferedOutputStream, so when enough
 * data has been gathered it will send this in chunks of the given size to the Output it is transferred to. The Visitor does not have to call
 * close() on the OutputStream, but should ensure that any wrapper streams or writers are flushed so that all data is sent.
 * <p/>
 * TODO Replace this with Inputs.output() in Qi4j v2.0 later.
 *
 */
public class OutputstreamInput
      implements Input<ByteBuffer, IOException>
{
   public OutputstreamInput(Visitor<OutputStream, IOException> outputVisitor, int bufferSize)
   {
      this.outputVisitor = outputVisitor;
      this.bufferSize = bufferSize;
   }

   private Visitor<OutputStream, IOException> outputVisitor;
   private int bufferSize;

   public <ReceiverThrowableType extends Throwable> void transferTo(Output<? super ByteBuffer, ReceiverThrowableType> output) throws IOException, ReceiverThrowableType
   {
      output.receiveFrom(new Sender<ByteBuffer, IOException>()
      {
         public <ReceiverThrowableType extends Throwable> void sendTo(final Receiver<? super ByteBuffer, ReceiverThrowableType> receiver) throws ReceiverThrowableType, IOException
         {
            OutputStream out = new BufferedOutputStream(new OutputStream()
            {
               @Override
               public void write(int b) throws IOException
               {
                  // Ignore
               }

               @Override
               public void write(byte[] b, int off, int len) throws IOException
               {
                  try
                  {
                     ByteBuffer byteBuffer = ByteBuffer.wrap(b, 0, len);
                     receiver.receive(byteBuffer);
                  } catch (Throwable ex)
                  {
                     throw new IOException(ex);
                  }
               }
            }, bufferSize);

            try
            {
               outputVisitor.visit(out);
            } catch (IOException ex)
            {
               throw (ReceiverThrowableType) ex.getCause();
            } finally
            {
               out.close();
            }
         }
      });
   }
}

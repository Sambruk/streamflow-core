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
package se.streamsource.streamflow.client.ui.workspace.cases.forms;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Form;
import org.restlet.representation.Representation;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.api.workspace.cases.form.SubmittedFormDTO;
import se.streamsource.streamflow.client.util.Downloadable;
import se.streamsource.streamflow.client.util.Refreshable;

import java.io.IOException;

public class CaseSubmittedFormModel
   implements Refreshable, Downloadable
{
   @Uses CommandQueryClient client;

   @Uses Integer index;

   @Structure
   Module module;
   SubmittedFormDTO form;

   public void refresh()
   {
      Form form = new Form();
      form.set("index", index.toString());
      this.form = client.query( "submittedform", SubmittedFormDTO.class, form);
   }

   public SubmittedFormDTO getForm()
   {
      return form;
   }

   public Representation download( String attachmentId ) throws IOException
   {
      ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder(StringValue.class);
      builder.prototype().string().set( attachmentId );

      Form form = new Form();
      form.set("id", attachmentId);
      return client.query( "download", Representation.class, form );
   }

   public void resenddoublesignemail()
   {
      Form form = new Form( );
      form.set( "secondsigntaskref", this.form.secondSignee().get().secondsigneetaskref().get() );

      client.postCommand( "resenddoublesignemail", form.getWebRepresentation() );
   }

   public void read()
   {
      Form form = new Form();
      form.set("index", index.toString());
      client.postCommand( "read", form );
   }
}
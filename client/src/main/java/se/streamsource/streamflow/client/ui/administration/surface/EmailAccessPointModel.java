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
package se.streamsource.streamflow.client.ui.administration.surface;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Form;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.surface.EmailAccessPointDTO;
import se.streamsource.streamflow.client.ui.workspace.cases.general.CaseLabelsModel;
import se.streamsource.streamflow.client.util.Refreshable;

/**
 * TODO
 */
public class EmailAccessPointModel
        implements Refreshable
{
   @Structure
   Module module;

   @Uses
   protected CommandQueryClient client;

   ValueBuilder<EmailAccessPointDTO> value;

   public void refresh()
   {
      value = client.query("index", EmailAccessPointDTO.class).buildWith();
   }

   public EmailAccessPointDTO getValue()
   {
      return value.prototype();
   }

   public Object getPossibleProjects()
   {
      BasicEventList<LinkValue> list = new BasicEventList<LinkValue>();

      LinksValue listValue = client.query( "possibleprojects",
            LinksValue.class );
      list.addAll( listValue.links().get() );

      return list;
   }

   public void changeProject(LinkValue link)
   {
      client.postLink( link );
   }

   public EventList<LinkValue> getPossibleCaseTypes()
   {
      BasicEventList<LinkValue> list = new BasicEventList<LinkValue>();

      LinksValue listValue = client.query( "possiblecasetypes",
            LinksValue.class );
      list.addAll( listValue.links().get() );

      return list;
   }

   public void changeCaseType(LinkValue link)
   {
      client.postLink(link);
   }

   public void changeSubject(String text)
   {
      Form form = new Form();
      form.set("subject", text);
      client.postCommand("changesubject", form.getWebRepresentation());
   }

   public void updateTemplate(String key, String template)
   {
      Form form = new Form();
      form.set("key", key);
      form.set("template", template);
      client.postCommand("changetemplate", form.getWebRepresentation());
   }

   public CaseLabelsModel createLabelsModel()
   {
      return module.objectBuilderFactory().newObjectBuilder(CaseLabelsModel.class).use(client.getSubClient("labels")).newInstance();
   }
}

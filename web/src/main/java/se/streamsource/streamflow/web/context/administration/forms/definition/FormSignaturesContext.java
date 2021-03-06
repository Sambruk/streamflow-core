/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.context.administration.forms.definition;

import java.util.List;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;

import se.streamsource.dci.api.CreateContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.form.RequiredSignatureValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.structure.form.RequiredSignatures;

/**
 * JAVADOC
 */
public class FormSignaturesContext
      implements CreateContext<RequiredSignatureValue, RequiredSignatures>, IndexContext<LinksValue>
{
   @Structure
   Module module;

   public RequiredSignatures create( RequiredSignatureValue requiredSignature )
   {
      RequiredSignatures signatures = RoleMap.role( RequiredSignatures.class );

      signatures.createRequiredSignature( requiredSignature );
      return signatures;
   }

   public LinksValue index()
   {
      List<RequiredSignatureValue> signatureValues = RoleMap.role( RequiredSignatures.Data.class ).requiredSignatures().get();
      int index = 0;
      LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );
      builder.rel("resource");
      for (RequiredSignatureValue signatureValue : signatureValues)
      {
         builder.addLink( signatureValue.name().get(), "" + index );
         index++;
      }

      return builder.newLinks();
   }
}

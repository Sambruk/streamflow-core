/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.web.context.organizations.forms;

import org.qi4j.api.constraint.Constraints;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.ContextNotFoundException;
import se.streamsource.dci.api.CreateContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValueMaxLength;
import se.streamsource.streamflow.domain.form.RequiredSignatureValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.structure.form.RequiredSignatures;

import java.util.List;

/**
 * JAVADOC
 */
@Mixins(FormSignaturesContext.Mixin.class)
@Constraints(StringValueMaxLength.class)
public interface FormSignaturesContext
      extends SubContexts<FormSignatureContext>, CreateContext<RequiredSignatureValue>, Context, IndexContext<LinksValue>
{
   abstract class Mixin
         extends ContextMixin
         implements FormSignaturesContext
   {
      public void create( RequiredSignatureValue requiredSignature )
      {
         RequiredSignatures signatures = roleMap.get( RequiredSignatures.class );

         signatures.createRequiredSignature( requiredSignature );
      }

      public FormSignatureContext context( String id )
      {
         Integer index = Integer.decode( id );
         roleMap.set( index, Integer.class );

         List<RequiredSignatureValue> signatureValues = roleMap.get( RequiredSignatures.Data.class ).requiredSignatures().get();
         if (index < signatureValues.size())
         {
            RequiredSignatureValue signature = signatureValues.get( index );
            roleMap.set( signature, RequiredSignatureValue.class );

            return subContext( FormSignatureContext.class );
         } else
         {
            throw new ContextNotFoundException();
         }
      }

      public LinksValue index()
      {
         List<RequiredSignatureValue> signatureValues = roleMap.get( RequiredSignatures.Data.class ).requiredSignatures().get();
         int index = 0;
         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );
         for (RequiredSignatureValue signatureValue : signatureValues)
         {
            builder.addLink( signatureValue.name().get(), "" + index );
            index++;
         }

         return builder.newLinks();
      }
   }
}

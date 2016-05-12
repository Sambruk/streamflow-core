/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.web.application.knowledgebase;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;

import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.label.Label;

/**
 * Integration of external Knowledgebase.
 */
@Mixins(KnowledgebaseService.Mixin.class)
public interface KnowledgebaseService
      extends ServiceComposite, Activatable, Configuration<KnowledgebaseConfiguration>
{
   /**
    * Given an entity, create a URL in the external website for it.
    *
    * Can be used with CaseTypes and Labels.
    *
    * @param entity
    * @return
    * @throws IllegalArgumentException
    */
   String createURL(EntityComposite entity)
         throws IllegalArgumentException;

   abstract class Mixin
         implements KnowledgebaseService
   {
      @This
      Configuration<KnowledgebaseConfiguration> config;

      @Service
      VelocityEngine velocity;

      public void activate() throws Exception
      {
         // force instantiation of config
         config.configuration().enabled().get();
      }

      public void passivate() throws Exception
      {

      }

      public String createURL(EntityComposite entity) throws IllegalArgumentException
      {
         try
         {
            if (entity instanceof CaseType)
            {
               VelocityContext context = new VelocityContext();
               context.put("id", entity.toString());
               context.put("description", URLEncoder.encode(((Describable) entity).getDescription(), "UTF-8"));
               return createURL(context, config.configuration().caseTypeTemplate().get());
            } else if (entity instanceof Label)
            {
               VelocityContext context = new VelocityContext();
               context.put("id", entity.toString());
               context.put("description", ((Describable) entity).getDescription());
               return createURL(context, config.configuration().labelTemplate().get());
            } else
               throw new IllegalArgumentException("Cannot create URL for this type:" + entity.type());
         } catch (UnsupportedEncodingException e)
         {
            throw new IllegalArgumentException("Cannot create URL", e);
         }
      }

      private String createURL(VelocityContext context, String template)
      {
         StringWriter writer = new StringWriter();
         try
         {
            velocity.evaluate(context, writer, "knowledgebase", template);

            return writer.toString();
         } catch (Exception e)
         {
            throw new IllegalArgumentException("Could not create URL", e);
         }
      }
   }
}

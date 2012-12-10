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
package se.streamsource.streamflow.web.rest.service.conversation;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.UsecaseBuilder;

import se.streamsource.streamflow.web.domain.structure.organization.EmailTemplates;

/**
 * When the list of possible templates for emails has been updated this service
 * will sync existing EmailAccessPoints to the defaults.
 */
@Mixins(EmailTemplatesUpdateService.Mixin.class)
public interface EmailTemplatesUpdateService
      extends Activatable, ServiceComposite
{
   class Mixin
         implements Activatable
   {
      @Structure
      private Module module;

      Map<String, String> templateDefaults = new HashMap<String, String>();

      public void activate() throws Exception
      {
         // Get defaults for emails
         ResourceBundle bundle = ResourceBundle.getBundle(EmailTemplates.class.getName());
         for (String key : bundle.keySet())
         {
            templateDefaults.put(key, bundle.getString(key));
         }

         // Update all email->AccessPoint template lists
         UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork(UsecaseBuilder.newUsecase("Synchronize templates"));

         try
         {
            Query<EmailTemplates> eaps = module.queryBuilderFactory().newQueryBuilder(EmailTemplates.class).newQuery(uow);
            for (EmailTemplates emailTemplates : eaps)
            {
               emailTemplates.synchronizeTemplates();
            }
            uow.complete();
         } catch (Exception e)
         {
            uow.discard();
         }
      }

      public void passivate() throws Exception
      {
      }
   }
}
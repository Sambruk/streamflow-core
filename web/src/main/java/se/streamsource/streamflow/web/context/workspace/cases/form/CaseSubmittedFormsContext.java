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
package se.streamsource.streamflow.web.context.workspace.cases.form;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.io.Input;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.api.SkipResourceValidityCheck;
import se.streamsource.streamflow.api.administration.form.GeoLocationFieldValue;
import se.streamsource.streamflow.api.administration.form.LocationDTO;
import se.streamsource.streamflow.api.workspace.cases.form.*;
import se.streamsource.streamflow.web.application.defaults.SystemDefaultsService;
import se.streamsource.streamflow.web.domain.entity.form.SubmittedFormsQueries;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;
import se.streamsource.streamflow.web.domain.structure.task.DoubleSignatureTask;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;
import se.streamsource.streamflow.web.rest.service.mail.MailSenderService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.text.MessageFormat;

/**
 * JAVADOC
 */
@Concerns(UpdateCaseCountSubmittedFormsConcern.class)
@Mixins(CaseSubmittedFormsContext.Mixin.class)
public interface CaseSubmittedFormsContext extends IndexContext<SubmittedFormsListDTO>, Context
{

   @SkipResourceValidityCheck
   SubmittedFormDTO submittedform(@Name("index") int index);

   Input<ByteBuffer, IOException> download(@Name("id") String id) throws IOException, URISyntaxException;

   void resenddoublesignemail(@Name("secondsigntaskref") String secondsigntaskref);

   void read(@Name("index") int index);

   abstract class Mixin implements CaseSubmittedFormsContext
   {

      @Service
      AttachmentStore store;

      @Structure
      Module module;

      @Optional
      @Service
      MailSenderService mailSender;

      @Service
       SystemDefaultsService systemDefaultsService;

      public SubmittedFormsListDTO index()
      {
         SubmittedFormsQueries forms = RoleMap.role( SubmittedFormsQueries.class );
         return forms.getSubmittedForms();
      }

      public SubmittedFormDTO submittedform(@Name("index") int index)
      {
         SubmittedFormsQueries forms = RoleMap.role( SubmittedFormsQueries.class );
          ValueBuilder<SubmittedFormDTO> builder = module.valueBuilderFactory().newValueBuilder(SubmittedFormDTO.class).withPrototype( forms.getSubmittedForm(index));
          for( SubmittedPageDTO pageDTO : builder.prototype().pages().get() )
          {
              for(FieldDTO fieldDTO : pageDTO.fields().get())
              {
                  // translate field value to url if type GeoLocationFieldValue
                  if ( fieldDTO.fieldType().get().equals(GeoLocationFieldValue.class.getName()))
                  {
                      String text = "";
                      //SF-867 Make Location parsing more robust. JSON cannot deal with empty string - needs at least curly braces!!∫
                      LocationDTO locationDTO = module.valueBuilderFactory().newValueFromJSON( LocationDTO.class, "".equals( fieldDTO.value().get() ) ? "{}" : fieldDTO.value().get() );
                      text += locationDTO.street().get() + ", " + locationDTO.zipcode().get() + ", " + locationDTO.city().get() + "<br>";
                      String locationString = locationDTO.location().get();
                      if (locationString != null) {
                          locationString = locationString.replace( ' ', '+' );
                          if (locationString.contains( "(" )) {
                              String[] positions = locationString.split( "\\),\\(");
                              locationString = positions[0].substring( 1, positions[0].length() -1 );
                          }
                      }
                      // f.ex.  "<a href=\"http://maps.google.com/maps?z=13&t=m&q={0}\" alt=\"Google Maps\">Klicka här för att visa karta</a>"
                      text += MessageFormat.format(systemDefaultsService.config().configuration().mapDefaultUrlPattern().get(), locationString);
                      fieldDTO.value().set( text );
                  }
              }
          }
          return builder.newInstance();
      }

      public Input<ByteBuffer, IOException> download(@Name("id") String id) throws IOException, URISyntaxException
      {
         SubmittedFormsQueries forms = RoleMap.role( SubmittedFormsQueries.class );
         AttachmentFieldSubmission value = forms.getAttachmentFieldValue( id );
         if (value != null)
         {
            AttachedFile.Data data = module.unitOfWorkFactory().currentUnitOfWork().get( AttachedFile.Data.class, id );
            final String fileId = new URI( data.uri().get() ).getSchemeSpecificPart();

            return store.attachment( fileId );
         } else
         {
            // 404
            throw new IllegalArgumentException( "No such attached file:" + id );
         }
      }

      public void resenddoublesignemail(@Name("secondsigntaskref") String secondsigntaskref)
      {
         DoubleSignatureTask task = module.unitOfWorkFactory().currentUnitOfWork()
               .get( DoubleSignatureTask.class, secondsigntaskref );
         mailSender.sentEmail( ((DoubleSignatureTask.Data) task).email().get() );
         task.updateLastReminderSent( new DateTime( DateTimeZone.UTC ) );
      }

      public void read(@Name("index") int index)
      {
         RoleMap.role( SubmittedForms.class ).read( index );
      }
   }
}

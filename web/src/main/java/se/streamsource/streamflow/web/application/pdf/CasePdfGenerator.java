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

package se.streamsource.streamflow.web.application.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.io.*;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.domain.form.AttachmentFieldSubmission;
import se.streamsource.streamflow.domain.form.AttachmentFieldValue;
import se.streamsource.streamflow.domain.form.EffectiveFieldValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.resource.caze.CaseOutputConfigValue;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.domain.entity.caze.CaseDescriptor;
import se.streamsource.streamflow.web.domain.entity.caze.CaseOutput;
import se.streamsource.streamflow.web.domain.entity.form.FieldEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.*;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversation;
import se.streamsource.streamflow.web.domain.structure.conversation.Message;
import se.streamsource.streamflow.web.domain.structure.conversation.Messages;
import se.streamsource.streamflow.web.domain.structure.created.Creator;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.text.DateFormat;
import java.util.*;
import java.util.List;

/**
 * A specialisation of CaseOutput that is responsible for exporting a case in PDF format;
 * The provided configuration tells what parts of the case are included in the export.
 */
public class CasePdfGenerator
      implements CaseOutput
{
   @Structure
   UnitOfWorkFactory uowf;

   @Structure
   ValueBuilderFactory vbf;

   @Service
   AttachmentStore store;

   private PdfDocument document;
   private ResourceBundle bundle;
   private final CaseOutputConfigValue config;
   private Locale locale;
   private String templateUri;

   private PdfFont h1Font = new PdfFont( PDType1Font.HELVETICA_BOLD, 16 );
   private PdfFont valueFont = new PdfFont( PDType1Font.HELVETICA, 12 );
   private PdfFont valueFontBold = new PdfFont( PDType1Font.HELVETICA_BOLD, 12 );
   private PdfFont headerFont = new PdfFont( PDType1Font.HELVETICA, 10 );

   private String caseId = "";
   private String printedOn = "";

   public CasePdfGenerator( @Uses CaseOutputConfigValue config, @Optional @Uses String templateUri, @Uses Locale locale )
   {
      this.config = config;
      this.locale = locale;
      this.templateUri = templateUri;
      bundle = ResourceBundle.getBundle( CasePdfGenerator.class.getName(), locale );
      document = new PdfDocument( PDPage.PAGE_SIZE_A4, 50 );
      document.init();
   }

   public void outputCase( CaseDescriptor cazeDescriptor ) throws Throwable
   {
      Case caze = cazeDescriptor.getCase();

      caseId = ((CaseId.Data) caze).caseId().get();
      printedOn = DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT, locale ).format( new Date() );

      document.print( "", valueFont ).changeColor( Color.BLUE )
            .println( bundle.getString( "caseSummary" ) + " - " + caseId, h1Font )
            .line().changeColor( Color.BLACK )
            .print( "", valueFont )
            .print( "", valueFont );

      float tabStop = document.calculateTabStop( valueFontBold, bundle.getString( "title" ),
            bundle.getString( "createdOn" ), bundle.getString( "createdBy" ), bundle.getString( "owner" ),
            bundle.getString( "assignedTo" ) );

      document.printLabelAndText( bundle.getString( "title" ) + ": ", valueFontBold, caze.getDescription(), valueFont, tabStop );
      document.printLabelAndText( bundle.getString( "createdOn" ) + ": ", valueFontBold,
            DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT, locale ).format( caze.createdOn().get() ), valueFont, tabStop );

      Creator creator = caze.createdBy().get();
      if (creator != null)
      {
         document.printLabelAndText( bundle.getString( "createdBy" ) + ": ", valueFontBold, ((Describable) creator).getDescription(), valueFont, tabStop );
      }

      Owner owner = ((Ownable.Data) caze).owner().get();
      if (owner != null)
      {
         document.printLabelAndText( bundle.getString( "owner" ) + ": ", valueFontBold, ((Describable) owner).getDescription(), valueFont, tabStop );
      }

      Assignee assignee = ((Assignable.Data) caze).assignedTo().get();
      if (assignee != null)
      {
         document.printLabelAndText( bundle.getString( "assignedTo" ) + ": ", valueFontBold, ((Describable) assignee).getDescription(), valueFont, tabStop );
      }

      String note = caze.getNote();
      if (Strings.notEmpty( note ))
      {
         document.changeColor( Color.BLUE );
         document.println( bundle.getString( "note" ), valueFontBold );
         document.changeColor( Color.BLACK );
         document.print( note, valueFont );
         document.print( "", valueFont );
      }

      // traverse structure
      if (config.contacts().get())
      {
         generateContacts( cazeDescriptor.contacts() );
      }

      if (config.effectiveFields().get())
      {
         generateEffectiveFields( cazeDescriptor.effectiveFields() );
      }

      if (config.conversations().get())
      {
         generateConversations( cazeDescriptor.conversations() );
      }

      if (config.attachments().get())
      {
         generateAttachments( cazeDescriptor.attachments() );
      }
   }

   private void generateContacts( Input<ContactValue, RuntimeException> contacts ) throws IOException
   {
      final Transforms.Counter<ContactValue> counter = new Transforms.Counter<ContactValue>();
      contacts.transferTo( Transforms.map( counter, new Output<ContactValue, IOException>()
      {
         public <SenderThrowableType extends Throwable> void receiveFrom( Sender<ContactValue, SenderThrowableType> sender ) throws IOException, SenderThrowableType
         {
            sender.sendTo( new Receiver<ContactValue, IOException>()
            {
               public void receive( ContactValue value ) throws IOException
               {
                  Map<String, String> nameValuePairs = new HashMap<String, String>( 10 );
                  if (Strings.notEmpty( value.name().get() ))
                     nameValuePairs.put( bundle.getString( "name" ), value.name().get() );

                  if (!value.phoneNumbers().get().isEmpty() && Strings.notEmpty( value.phoneNumbers().get().get( 0 ).phoneNumber().get() ))
                     nameValuePairs.put( bundle.getString( "phoneNumber" ), value.phoneNumbers().get().get( 0 ).phoneNumber().get() );

                  if (!value.addresses().get().isEmpty() && Strings.notEmpty( value.addresses().get().get( 0 ).address().get() ))
                     nameValuePairs.put( bundle.getString( "address" ), value.addresses().get().get( 0 ).address().get() );

                  if (!value.emailAddresses().get().isEmpty() && Strings.notEmpty( value.emailAddresses().get().get( 0 ).emailAddress().get() ))
                     nameValuePairs.put( bundle.getString( "email" ), value.emailAddresses().get().get( 0 ).emailAddress().get() );

                  if (Strings.notEmpty( value.company().get() ))
                     nameValuePairs.put( bundle.getString( "company" ), value.company().get() );

                  if (Strings.notEmpty( value.note().get() ))
                     nameValuePairs.put( bundle.getString( "note" ), value.note().get() );

                  float tabStop = document.calculateTabStop( valueFontBold, nameValuePairs.keySet()
                        .toArray( new String[nameValuePairs.keySet().size()] ) );

                  document.changeColor( Color.BLUE );
                  document.println( bundle.getString( "contact" ) + (counter.getCount() == 1 ? "" : " " + counter.getCount()), valueFontBold );
                  document.print( "", valueFont );
                  document.changeColor( Color.BLACK );

                  for (Map.Entry<String, String> stringEntry : nameValuePairs.entrySet())
                  {
                     document.printLabelAndText( stringEntry.getKey() + ":", valueFontBold, stringEntry.getValue(), valueFont, tabStop );
                  }
               }
            } );
         }
      } ));
   }

   public void generateConversations( Input<Conversation, RuntimeException> conversations ) throws IOException
   {
      final Transforms.Counter<Conversation> counter = new Transforms.Counter<Conversation>();
      Output<Conversation, IOException> output = Transforms.map( counter, new Output<Conversation, IOException>()
      {
         public <SenderThrowableType extends Throwable> void receiveFrom( Sender<Conversation, SenderThrowableType> sender ) throws IOException, SenderThrowableType
         {
            sender.sendTo( new Receiver<Conversation, IOException>()
            {
               public void receive( Conversation conversation ) throws IOException
               {
                  if (counter.getCount() == 1)
                  {
                     document.changeColor( Color.BLUE ).println( bundle.getString( "conversations" ), valueFontBold )
                           .changeColor( Color.BLACK );
                  }


                  List<Message> messages = ((Messages.Data) conversation).messages().toList();
                  if (!messages.isEmpty())
                  {
                     document.println( conversation.getDescription(), valueFontBold ).underLine( conversation.getDescription(), valueFontBold );

                     for (Message message : messages)
                     {
                        Message.Data data = ((Message.Data) message);
                        String label = data.sender().get().getDescription() + ", "
                              + DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT, locale ).format( data.createdOn().get() )
                              + ": ";

                        document.print( label, valueFontBold ).print( extractBody( data.body().get() ), valueFont )
                              .print( "", valueFont );
                     }
                  }
               }
            } );
         }
      } );
      conversations.transferTo( output );
   }

   public void generateEffectiveFields( Input<EffectiveFieldValue, RuntimeException> effectiveFields ) throws IOException
   {
      final Map<EntityReference, List<EffectiveFieldValue>> forms = new LinkedHashMap<EntityReference, List<EffectiveFieldValue>>( 10 );

      effectiveFields.transferTo( new Output<EffectiveFieldValue, IOException>()
      {
         public <SenderThrowableType extends Throwable> void receiveFrom( Sender<EffectiveFieldValue, SenderThrowableType> sender ) throws IOException, SenderThrowableType
         {
            sender.sendTo( new Receiver<EffectiveFieldValue, IOException>()
            {
               public void receive( EffectiveFieldValue field ) throws IOException
               {

                  // sort effective fields per form
                  if (!forms.containsKey( field.form().get() ))
                  {
                     List<EffectiveFieldValue> formFields = new ArrayList<EffectiveFieldValue>();
                     formFields.add( field );
                     forms.put( field.form().get(), formFields );
                  } else
                  {
                     forms.get( field.form().get() ).add( field );
                  }
               }
            } );
         }
      } );

      if (!forms.isEmpty())
      {
         // Heading
         document.changeColor( Color.BLUE );
         document.println( bundle.getString( "submittedForms" ) + ":", valueFontBold );
         document.changeColor( Color.BLACK );

         Date lastSubmittedOn = null;
         String lastSubmittedBy = "";

         for (Map.Entry<EntityReference, List<EffectiveFieldValue>> entityReferenceListEntry : forms.entrySet())
         {
            Describable form = uowf.currentUnitOfWork().get( Describable.class, entityReferenceListEntry.getKey().identity() );

            document.println( form.getDescription() + ":", valueFontBold );
            document.underLine( form.getDescription(), valueFontBold );
            document.println( "", valueFont );

            Map<String, String> fieldKeyValues = new LinkedHashMap<String, String>();
            for (EffectiveFieldValue field : entityReferenceListEntry.getValue())
            {
               Describable fieldName = uowf.currentUnitOfWork().get( Describable.class, field.field().get().identity() );

               // convert JSON String if field type AttachmentFieldValue
               if (uowf.currentUnitOfWork().get( FieldEntity.class, field.field().get().identity() ).fieldValue().get() instanceof AttachmentFieldValue)
               {
                  AttachmentFieldSubmission attachment = vbf.newValueFromJSON( AttachmentFieldSubmission.class, field.value().get() );
                  fieldKeyValues.put( fieldName.getDescription(), attachment.name().get() );

               } else
               {
                  fieldKeyValues.put( fieldName.getDescription(), field.value().get() );
               }

               // keep track of last submitter and submission date
               if (lastSubmittedOn == null || lastSubmittedOn.before( field.submissionDate().get() ))
               {
                  lastSubmittedOn = field.submissionDate().get();
                  lastSubmittedBy = uowf.currentUnitOfWork().get( Describable.class, field.submitter().get().identity() ).getDescription();
               }
            }

            float tabStop = document.calculateTabStop( valueFontBold, bundle.getString( "lastSubmitted" ), bundle.getString( "lastSubmittedBy" ) );
            document.printLabelAndText( bundle.getString( "lastSubmitted" ) + ":", valueFontBold,
                  DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT, locale ).format( lastSubmittedOn ),
                  valueFont, tabStop );
            document.printLabelAndText( bundle.getString( "lastSubmittedBy" ) + ":", valueFontBold, lastSubmittedBy, valueFont, tabStop );
            document.print( "", valueFont );

            tabStop = document.calculateTabStop( valueFontBold, fieldKeyValues.keySet().toArray( new String[fieldKeyValues.keySet().size()] ) );

            for (Map.Entry<String, String> entry : fieldKeyValues.entrySet())
            {
               if (Strings.notEmpty( entry.getValue() ))
                  document.printLabelAndText( entry.getKey() + ":", valueFontBold, entry.getValue(), valueFont, tabStop );
            }
         }
      }
   }

   public void generateAttachments( Input<Attachment, RuntimeException> attachments ) throws IOException
   {
      final Transforms.Counter<Attachment> counter = new Transforms.Counter<Attachment>();
      attachments.transferTo( Transforms.map(counter, new Output<Attachment, IOException>()
      {
         public <SenderThrowableType extends Throwable> void receiveFrom( Sender<Attachment, SenderThrowableType> sender ) throws IOException, SenderThrowableType
         {
            sender.sendTo( new Receiver<Attachment, IOException>()
            {
               public void receive( Attachment attachment ) throws IOException
               {
                  if (counter.getCount() == 1)
                  {
                     document.changeColor( Color.BLUE ).print( bundle.getString( "attachments" ) + ":", valueFontBold )
                           .changeColor( Color.BLACK );
                  }

                  document.print( ((AttachedFile.Data) attachment).name().get(), valueFont );
               }
            });
         }
      }));
   }

   public PDDocument getPdf() throws IOException
   {
      document.closeAndReturn();
      PDDocument generatedDoc = document.generateHeaderAndPageNumbers( headerFont, caseId, bundle.getString( "printDate" ) + ": " + printedOn );


      generatedDoc.getDocumentInformation().setCreator( "Streamflow" );
      Calendar calendar = Calendar.getInstance();
      generatedDoc.getDocumentInformation().setCreationDate( calendar );
      generatedDoc.getDocumentInformation().setTitle( caseId );

      if (templateUri != null)
      {

         String attachmentId;
         try
         {
            attachmentId = new URI( templateUri ).getSchemeSpecificPart();

            InputStream templateStream = store.getAttachment( attachmentId );
            Underlay underlay = new Underlay();
            generatedDoc = underlay.underlay( generatedDoc, templateStream );

         } catch (Exception e)
         {

            e.printStackTrace();
         }
      }

      return generatedDoc;
   }

   private String extractBody( String html ) throws IOException
   {
      final StringBuffer buff = new StringBuffer();

      ParserDelegator parserDelegator = new ParserDelegator();
      HTMLEditorKit.ParserCallback parserCallback = new HTMLEditorKit.ParserCallback()
      {
         public void handleText( final char[] data, final int pos )
         {
            buff.append( new String( data ) );
         }

         public void handleStartTag( HTML.Tag tag, MutableAttributeSet attribute, int pos )
         {
         }

         public void handleEndTag( HTML.Tag t, final int pos )
         {
         }

         public void handleSimpleTag( HTML.Tag t, MutableAttributeSet a, final int pos )
         {
         }

         public void handleComment( final char[] data, final int pos )
         {
         }

         public void handleError( final java.lang.String errMsg, final int pos )
         {
         }
      };
      parserDelegator.parse( new StringReader( html ), parserCallback, true );

      return buff.toString();
   }
}

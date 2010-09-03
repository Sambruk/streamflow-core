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

package se.streamsource.streamflow.reference.contact;

import org.ccil.cowan.tagsoup.Parser;
import org.qi4j.api.Qi4j;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilder;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import se.streamsource.streamflow.server.plugin.contact.ContactAddressValue;
import se.streamsource.streamflow.server.plugin.contact.ContactLookup;
import se.streamsource.streamflow.server.plugin.contact.ContactPhoneValue;
import se.streamsource.streamflow.server.plugin.contact.ContactValue;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of contact lookup that gets info from Eniro.se
 */
public class EniroContactLookupService
      implements ContactLookup
{
   private
   @Structure
   Qi4j qi4j;

   public Iterable<ContactValue> lookup( ContactValue contactTemplate )
   {
      String searchString = "";

      if (!contactTemplate.name().get().equals( "" ))
      {
         searchString += contactTemplate.name().get();
      } else if (contactTemplate.phoneNumbers().get().size() > 0)
      {
         searchString += contactTemplate.phoneNumbers().get().get( 0 ).phoneNumber().get();
      }

      List<ContactValue> possibleContacts = new ArrayList<ContactValue>();

      // Make Eniro REST call
      Node doc;
      try
      {
         URL url = new URL( "http://personer.eniro.se/query?what=wp&lang=&ax=&search_word=" + URLEncoder.encode( searchString, "ISO-8859-1" ) + "&geo_area=Halmstad" );
         XMLReader reader = new Parser();
         reader.setFeature( Parser.namespacesFeature, false );
         reader.setFeature( Parser.namespacePrefixesFeature, false );

         Transformer transformer = TransformerFactory.newInstance().newTransformer();

         DOMResult result = new DOMResult();

         transformer.transform( new SAXSource( reader, new InputSource( new InputStreamReader(url.openStream(), "UTF-8" ) )), result );

         // here we go - an DOM built from arbitrary HTML
         doc = result.getNode();


      } catch (Exception ex)
      {
         ex.printStackTrace();
         return possibleContacts;
      }

      // Parse result
      XPathFactory factory = XPathFactory.newInstance();

      try
      {
         NodeList nodes = (NodeList) factory.newXPath().evaluate( "//li[@class='vcard person']", doc, XPathConstants.NODESET );

         for (int i = 0; i < nodes.getLength(); i++)
         {
            ValueBuilder<ContactValue> builder = contactTemplate.buildWith();
            Node node = nodes.item( i );


            String fullName = factory.newXPath().evaluate( "div//span[@class='given-name'][1]", node ) + ' ' + factory.newXPath().evaluate( "div//span[@class='given-name'][2]", node );

            builder.prototype().name().set( fullName );

            List<ContactAddressValue> addressList = builder.prototype().addresses().get();
            ValueBuilder<ContactAddressValue> addressBuilder = qi4j.getModule( contactTemplate ).valueBuilderFactory().newValueBuilder( ContactAddressValue.class );

            String address = factory.newXPath().evaluate( "div/p[@class='adr']/span[@class='street-address']", node );
            addressBuilder.prototype().address().set( address );

            String city = factory.newXPath().evaluate( "div/p[@class='adr']/span[@class='locality']", node );
            addressBuilder.prototype().city().set( city );

            String zipCode = factory.newXPath().evaluate( "div/p[@class='adr']/span[@class='postal-code']", node );
            addressBuilder.prototype().zipCode().set( zipCode );

            addressList.add( addressBuilder.newInstance() );

            ValueBuilder<ContactPhoneValue> phoneBuilder = qi4j.getModule( contactTemplate ).valueBuilderFactory().newValueBuilder( ContactPhoneValue.class );

            String phone = factory.newXPath().evaluate( "div/span[@class='tel']/a", node );
            phoneBuilder.prototype().phoneNumber().set( phone );

            builder.prototype().phoneNumbers().get().add( phoneBuilder.newInstance() );

            possibleContacts.add( builder.newInstance() );
         }
      } catch (XPathExpressionException e)
      {
         e.printStackTrace();
      }

      return possibleContacts;
   }
}
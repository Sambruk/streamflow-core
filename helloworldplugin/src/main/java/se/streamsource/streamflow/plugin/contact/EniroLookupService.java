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

package se.streamsource.streamflow.plugin.contact;

import org.apache.commons.lang.CharSet;
import org.ccil.cowan.tagsoup.Parser;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.qi4j.api.Qi4j;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.ext.xml.SaxRepresentation;
import org.restlet.security.Verifier;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import se.streamsource.streamflow.domain.contact.ContactAddressValue;
import se.streamsource.streamflow.domain.contact.ContactPhoneValue;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.TransactionVisitor;
import se.streamsource.streamflow.server.plugin.ContactLookup;
import se.streamsource.streamflow.server.plugin.contact.ContactLookup;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Implementation of contact lookup that gets info from Eniro.se
 */
public class EniroLookupService
   implements BundleActivator, ContactLookup
{
   private BundleContext bundleContext;
   public ServiceRegistration registration;

   public void start( BundleContext bundleContext ) throws Exception
   {
      this.bundleContext = bundleContext;

      registration = bundleContext.registerService( ContactLookup.class.getName(), this, new Properties() );
   }

   public void stop( BundleContext bundleContext ) throws Exception
   {
      registration.unregister();
   }

   public Iterable<ContactValue> lookup( ContactValue contactTemplate )
   {
      ServiceReference reference = bundleContext.getServiceReference( Qi4j.class.getName() );
      Qi4j qi4j = (Qi4j) bundleContext.getService( reference );

      String searchString = "";

      if (!contactTemplate.name().get().equals(""))
      {
         searchString += contactTemplate.name().get();
      }

      List<ContactValue> possibleContacts = new ArrayList<ContactValue>();

      // Make Eniro REST call
      Node doc;
      try
      {
           URL url = new URL("http://personer.eniro.se/query?what=wp&lang=&ax=&search_word="+ URLEncoder.encode( searchString, "ISO-8859-1" )+"&geo_area=%F6ebro");
           XMLReader reader = new Parser();
           reader.setFeature(Parser.namespacesFeature, false);
           reader.setFeature(Parser.namespacePrefixesFeature, false);

           Transformer transformer = TransformerFactory.newInstance().newTransformer();

           DOMResult result = new DOMResult();
           transformer.transform(new SAXSource(reader, new InputSource(url.openStream())),
                                 result);

           // here we go - an DOM built from abitrary HTML
           doc = result.getNode();
      } catch (Exception ex)
      {
         ex.printStackTrace();
         return possibleContacts;
      }

      // Parse result
      XPathFactory factory = XPathFactory.newInstance(  );

      try
      {
         NodeList nodes = (NodeList) factory.newXPath().evaluate( "//div[@id='person-card']", doc, XPathConstants.NODESET );

         for (int i = 0; i < nodes.getLength(); i++)
         {
            ValueBuilder<ContactValue> builder = contactTemplate.buildWith();
            Node node = nodes.item( i );
            List<ContactAddressValue> addressList = builder.prototype().addresses().get();
            ValueBuilder<ContactAddressValue> addressBuilder = qi4j.getModule(contactTemplate ).valueBuilderFactory().newValueBuilder( ContactAddressValue.class );

            String address = factory.newXPath().evaluate( "//div[@class='adr']/span[@class='street-address']", node );
            addressBuilder.prototype().address().set( address );
            
            String city = factory.newXPath().evaluate( "//div[@class='adr']/span[@class='locality']", node );
            addressBuilder.prototype().city().set( city );

            String zipCode = factory.newXPath().evaluate( "//div[@class='adr']/span[@class='post-code']", node );
            addressBuilder.prototype().zipCode().set( zipCode );

            addressList.add(addressBuilder.newInstance() );

            ValueBuilder<ContactPhoneValue> phoneBuilder = qi4j.getModule(contactTemplate).valueBuilderFactory().newValueBuilder( ContactPhoneValue.class );

            String phone = factory.newXPath().evaluate( "//li[@class='tel']/a", node );
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
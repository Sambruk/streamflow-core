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

package se.streamsource.streamflow.web.application.eid;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import se.sll.wsdl.soap.osif.EncodeTBSRequest;
import se.sll.wsdl.soap.osif.EncodeTBSResponse;
import se.sll.wsdl.soap.osif.GenerateChallengeRequest;
import se.sll.wsdl.soap.osif.GenerateChallengeResponse;
import se.sll.wsdl.soap.osif.Osif;
import se.sll.wsdl.soap.osif.OsifService;
import se.sll.wsdl.soap.osif.VerifyAuthenticationRequest;
import se.sll.wsdl.soap.osif.VerifyAuthenticationResponse;
import se.sll.wsdl.soap.osif.VerifyCertificateRequest;
import se.sll.wsdl.soap.osif.VerifyCertificateResponse;
import se.sll.wsdl.soap.osif.VerifySignatureRequest;
import se.sll.wsdl.soap.osif.VerifySignatureResponse;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import java.net.URL;
import java.util.Map;

/**
 * Service that is used to handle Swedish eID's through an OSIF WebService.
 */
@Mixins(OSIFService.Mixin.class)
public interface OSIFService
   extends Osif, ServiceComposite, Activatable, Configuration
{
   class Mixin
      implements Osif, Activatable
   {
      private
      @This
      Configuration<OSIFConfiguration> config;

      private Osif osif;

      public void activate() throws Exception
      {
         URL wsdl= OSIFService.class.getResource("osif.wsdl");
         OsifService osifService = new OsifService(wsdl, new QName("urn:www.sll.se/wsdl/soap/osif", "OsifService"));
         osif = osifService.getOsif();
         setEndpointAddress( osif );
      }

      private void setEndpointAddress( Osif osifService )
      {
         BindingProvider bp = (BindingProvider)osifService;
         Map<String, Object> context = bp.getRequestContext();
         context.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,config.configuration().serviceURL().get());
      }

      public void passivate() throws Exception
      {
      }

      // OSIF
      // Authentication
      public GenerateChallengeResponse generateChallenge( GenerateChallengeRequest generateChallengeRequest )
      {
         generateChallengeRequest.setPolicy( config.configuration().serviceId().get() );
         return osif.generateChallenge( generateChallengeRequest );
      }

      // Signature
      public EncodeTBSResponse encodeTBS(  EncodeTBSRequest encodeTBSRequest )
      {
         encodeTBSRequest.setPolicy( config.configuration().serviceId().get() );
         return osif.encodeTBS( encodeTBSRequest );
      }

      // Verify authentication
      public VerifyAuthenticationResponse verifyAuthentication( VerifyAuthenticationRequest verifyAuthenticationRequest )
      {
         verifyAuthenticationRequest.setPolicy( config.configuration().serviceId().get() );
         return osif.verifyAuthentication( verifyAuthenticationRequest );
      }

      // Verify signature
      public VerifySignatureResponse verifySignature( VerifySignatureRequest verifySignatureRequest )
      {
         verifySignatureRequest.setPolicy( config.configuration().serviceId().get() );
         return osif.verifySignature( verifySignatureRequest );
      }

      // Verify certificate
      public VerifyCertificateResponse verifyCertificate( VerifyCertificateRequest verifyCertificateRequest )
      {
         verifyCertificateRequest.setPolicy( config.configuration().serviceId().get() );
         return osif.verifyCertificate( verifyCertificateRequest );
      }
   }
}

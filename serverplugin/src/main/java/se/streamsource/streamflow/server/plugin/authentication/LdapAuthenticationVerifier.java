package se.streamsource.streamflow.server.plugin.authentication;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.security.SecretVerifier;

public class LdapAuthenticationVerifier extends SecretVerifier
{
   @Service
   Authenticator authenticator;

   @Structure
   ValueBuilderFactory vbf;

   @Override
   public int verify( Request request, Response response )
   {
      int result = super.verify( request, response );

      return result;
   }

   @Override
   public boolean verify( String username, char[] password )
   {
      ValueBuilder<UserIdentityValue> builder = vbf.newValueBuilder( UserIdentityValue.class );
      builder.prototype().username().set( username );
      builder.prototype().password().set( new String( password ) );

      try
      {
         authenticator.authenticate( builder.newInstance() );
      } catch (Exception e)
      {
         return false;
      }
      return true;
   }
}

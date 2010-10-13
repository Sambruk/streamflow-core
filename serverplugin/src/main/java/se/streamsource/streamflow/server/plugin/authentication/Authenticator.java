package se.streamsource.streamflow.server.plugin.authentication;

/**
 * Authenticates a user, given a certain password, against an external system
 */
public interface Authenticator
{

   void authenticate( UserIdentityValue user );

}

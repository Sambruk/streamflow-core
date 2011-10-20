package se.streamsource.streamflow.web.management;

/**
 * TODO
 */
public class UpdateBuilder
{
   UpdateRules rules;

   String fromVersion;

   public UpdateBuilder( String fromVersion )
   {
       this.rules = new UpdateRules();
       this.fromVersion = fromVersion;
   }

   public VersionUpdateBuilder toVersion( String toVersion )
   {
       return new VersionUpdateBuilder( this, fromVersion, toVersion );
   }

   public UpdateRules getRules()
   {
       return rules;
   }
}

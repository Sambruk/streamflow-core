package se.streamsource.streamflow.web.management;

/**
 * TODO
 */
public class UpdateRule
{
   private UpdateOperation operation;

   protected String fromVersion;
   protected String toVersion;

   public UpdateRule(String fromVersion, String toVersion, UpdateOperation operation)
   {
      this.fromVersion = fromVersion;
      this.toVersion = toVersion;
      this.operation = operation;
   }

   public String fromVersion()
   {
      return fromVersion;
   }

   public String toVersion()
   {
      return toVersion;
   }

   public UpdateOperation getOperation()
   {
      return operation;
   }
}

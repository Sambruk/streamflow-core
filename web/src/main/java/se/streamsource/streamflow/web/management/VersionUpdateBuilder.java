package se.streamsource.streamflow.web.management;

/**
 * TODO
 */
public class VersionUpdateBuilder
{
    UpdateBuilder builder;

    String fromVersion;
    String toVersion;

    public VersionUpdateBuilder( UpdateBuilder builder, String fromVersion, String toVersion )
    {
        this.builder = builder;
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
    }

    public VersionUpdateBuilder toVersion( String toVersion )
    {
        return new VersionUpdateBuilder( builder, this.toVersion, toVersion );
    }

    public VersionUpdateBuilder atStartup( UpdateOperation operation )
    {
        builder.getRules().addRule( new UpdateRule( fromVersion, toVersion, operation ) );

        return this;
    }
}

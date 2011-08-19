package se.streamsource.streamflow.web.context;

/**
 * Base class for methodful roles
 */
public class Role<T>
{
   // Self reference to the bound Data object
   protected T self;

   public Role()
   {
   }

   public Role(T self)
   {
      this.self = self;
   }

   public void bind(T newSelf)
   {
      self = newSelf;
   }

   public T self()
   {
      return this.self;
   }
}

package se.streamsource.streamflow.web.context.util;

import java.lang.reflect.Method;

import org.qi4j.api.query.grammar.AssociationReference;
import org.qi4j.api.query.grammar.PropertyReference;

public abstract class DerivedPropertyReference<T> implements PropertyReference<T> {

   Class<T> propertyType;

   public DerivedPropertyReference(Class<T> propertyType) {
      this.propertyType = propertyType;
   }

   @Override
   public String propertyName() {
      throw new UnsupportedOperationException("Not available");
   }

   @Override
   public Class<?> propertyDeclaringType() {
      throw new UnsupportedOperationException("Not available");
   }

   @Override
   public Method propertyAccessor() {
      throw new UnsupportedOperationException("Not available");
   }

   @Override
   public Class<T> propertyType() {
      return propertyType;
   }

   @Override
   public AssociationReference traversedAssociation() {
      return null;
   }

   @Override
   public PropertyReference<?> traversedProperty() {
      return null;
   }
}

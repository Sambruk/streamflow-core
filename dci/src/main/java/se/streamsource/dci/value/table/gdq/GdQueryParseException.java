package se.streamsource.dci.value.table.gdq;

public class GdQueryParseException extends RuntimeException {

   private static final long serialVersionUID = 1L;

   public GdQueryParseException() {
   }

   public GdQueryParseException(String message) {
      super(message);
   }

   public GdQueryParseException(Throwable cause) {
      super(cause);
   }

   public GdQueryParseException(String message, Throwable cause) {
      super(message, cause);
   }

   public GdQueryParseException(String message, Throwable cause,
         boolean enableSuppression, boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
   }
}

package se.streamsource.streamflow.web.context.util;

public class SearchResult<T> {

   private final Iterable<T> result;
   private final Integer unlimitedCount;

   public SearchResult(Iterable<T> result, Integer unlimitedCount) {
      this.result = result;
      this.unlimitedCount = unlimitedCount;
   }

   public Iterable<T> getResult() {
      return result;
   }

   public Integer getUnlimitedCount() {
      return unlimitedCount;
   }
}

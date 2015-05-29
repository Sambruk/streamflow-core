package se.streamsource.dci.value.table.gdq;


public class OrderByElement {
   public String name;
   public OrderByDirection direction;

   public OrderByElement(String name, OrderByDirection direction) {
      this.name = name;
      this.direction = direction;
   }
}
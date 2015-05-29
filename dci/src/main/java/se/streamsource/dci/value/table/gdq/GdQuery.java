package se.streamsource.dci.value.table.gdq;

import java.util.ArrayList;
import java.util.List;

public class GdQuery {
   public String select;
   public String where;
   public List<OrderByElement> orderBy = new ArrayList<OrderByElement>();
   public Integer limit;
   public Integer offset;
   public String options;
}
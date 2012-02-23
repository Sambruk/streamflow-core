package org.streamsource.streamflow.statistic.dto;

/**
 * Created by IntelliJ IDEA.
 * User: arvidhuss
 * Date: 2/23/12
 * Time: 2:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScatterChartValue
{
   private String xAxis;
   private String yAxis;
   
   public ScatterChartValue( String xAxis, String yAxis )
   {
      this.xAxis = xAxis;
      this.yAxis = yAxis;
   }

   public String getxAxis()
   {
      return xAxis;
   }

   public String getyAxis()
   {
      return yAxis;
   }
}

package se.streamsource.streamflow.util;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * TODO
 */
public class MessageTemplateTest
{
   @Test
   public void testTemplate()
   {
      Map<String, String> map = new HashMap<String, String>();
      map.put("foo", "bar");
      map.put("not", "definitely");
      assertThat(MessageTemplate.text("{null}This is {not} a test {foo}", map), equalTo("This is definitely a test bar"));
   }

   @Test
   public void testTemplateBuilder()
   {
      assertThat(MessageTemplate.text("{null}This is {not} a test {foo}").bind("foo", "bar").bind("not", "definitely").eval(), equalTo("This is definitely a test bar"));
   }
}

package se.streamsource.streamflow.web.application.dueon;

import java.util.ArrayList;
import java.util.List;

import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.structure.user.Contactable;

public class DueOnNotification
{

   private Contactable recipient;

   private List<CaseEntity> personalOverdueCases = new ArrayList<CaseEntity>();
   private List<CaseEntity> personalThresholdCases = new ArrayList<CaseEntity>();
   private List<CaseEntity> functionOverdueCases = new ArrayList<CaseEntity>();
   private List<CaseEntity> functionThresholdCases = new ArrayList<CaseEntity>();
   private List<CaseEntity> monitoredOverdueCases = new ArrayList<CaseEntity>();
   private List<CaseEntity> monitoredThresholdCases = new ArrayList<CaseEntity>();
   
   public DueOnNotification(Contactable recipient) 
   {
      this.recipient = recipient;
   }

   public Contactable getRecipient()
   {
      return recipient;
   }

   public List<CaseEntity> getPersonalOverdueCases()
   {
      return personalOverdueCases;
   }

   public List<CaseEntity> getPersonalThresholdCases()
   {
      return personalThresholdCases;
   }

   public List<CaseEntity> getFunctionOverdueCases()
   {
      return functionOverdueCases;
   }

   public List<CaseEntity> getFunctionThresholdCases()
   {
      return functionThresholdCases;
   }

   public List<CaseEntity> getMonitoredOverdueCases()
   {
      return monitoredOverdueCases;
   }

   public List<CaseEntity> getMonitoredThresholdCases()
   {
      return monitoredThresholdCases;
   }

}

/**
 *
 * Copyright 2009-2013 Jayway Products AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.ui.administration.surface;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.api.administration.form.RequiredSignatureValue;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.general.CaseLabelsView;
import se.streamsource.streamflow.client.ui.workspace.cases.general.RemovableLabel;
import se.streamsource.streamflow.client.util.ActionBinder;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.LinkValueConverter;
import se.streamsource.streamflow.client.util.RefreshComponents;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.ValueBinder;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.SelectLinkDialog;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

import javax.swing.ActionMap;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;

import static se.streamsource.streamflow.client.util.i18n.*;


public class AccessPointView
      extends JPanel
      implements Refreshable, TransactionListener
{
   @Service
   DialogService dialogs;

   @Structure
   Module module;

   private CaseLabelsView labels;
   private JLabel selectedCaseType = new JLabel();
   private StreamflowButton caseTypeButton;
   private StreamflowButton labelButton;
   private StreamflowButton projectButton;
   private JLabel selectedProject = new JLabel();
   private StreamflowButton formButton;
   private JLabel selectedForm = new JLabel();

   private JLabel mailSelectionLabel = new JLabel();
   private JTextField mailSelectionField = new JTextField( );

   private StreamflowButton templateButton;
   private RemovableLabel selectedTemplate = new RemovableLabel();

   private JCheckBox signActive1;
   private JTextField signName1;
   private JTextField signDescription1;


   private JCheckBox signActive2;
   private JTextField signName2;
   private JTextField signDescription2;
   private StreamflowButton form2Button;
   private JLabel selectedForm2;
   private JCheckBox mandatory2;
   private JTextField formQuestion2;

   private JList emailTemplateList = new JList();
   private JTextArea emailTemplateText = new JTextArea();
   private JTextField subject;

   private AccessPointModel model;

   private ActionBinder actionBinder;
   private ValueBinder valueBinder;


   public AccessPointView( @Service ApplicationContext appContext,
                           @Uses final AccessPointModel model,
                           @Structure Module module )
   {
      this.model = model;
      this.labels = module.objectBuilderFactory().newObjectBuilder(CaseLabelsView.class).use( model.getLabelsModel() ).newInstance();

      setLayout( new BorderLayout() );
      setActionMap( appContext.getActionMap( this ) );

      RefreshComponents refreshComponents = new RefreshComponents();
      model.addObserver( refreshComponents );

      actionBinder = module.objectBuilderFactory().newObjectBuilder( ActionBinder.class ).use( getActionMap() ).newInstance();
      valueBinder = module.objectBuilderFactory().newObject( ValueBinder.class );
      actionBinder.setResourceMap( appContext.getResourceMap( getClass() ) );
      LinkValueConverter linkValueConverter = new LinkValueConverter();

      FormLayout layout = new FormLayout( "90dlu, 5dlu, 150:grow",
            "pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 12dlu, " +
            "pref, 12dlu, pref, 2dlu, default:grow" );

      JPanel panel = new JPanel( layout );
      DefaultFormBuilder builder = new DefaultFormBuilder( layout,
            panel );
      builder.setBorder( Borders.createEmptyBorder( Sizes.DLUY8,
            Sizes.DLUX4, Sizes.DLUY2, Sizes.DLUX8 ) );

      CellConstraints cc = new CellConstraints();

      MacOsUIWrapper.convertAccelerators( appContext.getActionMap(
            AccessPointView.class, this ) );

      selectedProject.setFont( selectedProject.getFont().deriveFont( Font.BOLD ) );

      selectedCaseType.setFont( selectedCaseType.getFont().deriveFont(
            Font.BOLD ) );

      selectedForm.setFont( selectedForm.getFont().deriveFont(
            Font.BOLD ) );

      selectedTemplate.getLabel().setFont(selectedTemplate.getLabel().getFont().deriveFont(
            Font.BOLD));

      ActionMap am = getActionMap();

      // Select project
      javax.swing.Action projectAction = am.get( "project" );
      projectButton = new StreamflowButton( projectAction );
      projectButton.registerKeyboardAction( projectAction, (KeyStroke) projectAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );
      projectButton.setHorizontalAlignment( SwingConstants.LEFT );

      builder.add( projectButton, cc.xy( 1, 1 ) );

      builder.add( valueBinder.bind( "project", selectedProject, linkValueConverter ),
            new CellConstraints( 3, 1, 1, 1, CellConstraints.LEFT, CellConstraints.CENTER, new Insets( 5, 0, 0, 0 ) ) );


      // Select case type
      javax.swing.Action caseTypeAction = am.get( "casetype" );
      caseTypeButton = new StreamflowButton( caseTypeAction );
      caseTypeButton.registerKeyboardAction( caseTypeAction, (KeyStroke) caseTypeAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );
      caseTypeButton.setHorizontalAlignment( SwingConstants.LEFT );

      builder.add( caseTypeButton, cc.xy( 1, 3 ) );

      builder.add( valueBinder.bind( "caseType", selectedCaseType, linkValueConverter ),
            new CellConstraints( 3, 3, 1, 1, CellConstraints.LEFT, CellConstraints.CENTER, new Insets( 5, 0, 0, 0 ) ) );


      // Select labels
      javax.swing.Action labelAction = labels.getActionMap().get( "addLabel" );
      labelButton = new StreamflowButton( labelAction );

      labelButton.registerKeyboardAction( labelAction, (KeyStroke) labelAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );

      labelButton.setHorizontalAlignment( SwingConstants.LEFT );

      builder.add( labelButton, cc.xy( 1, 5, CellConstraints.FILL, CellConstraints.TOP ) );

      labels.setPreferredSize( new Dimension( 500, 60 ) );
      labels.setTextBold( true );
      labels.setButtonRelation( labelButton );
      builder.add( labels,
            new CellConstraints( 3, 5, 1, 1, CellConstraints.LEFT, CellConstraints.TOP, new Insets( 5, 0, 0, 0 ) ) );

      // Select form
      javax.swing.Action formAction = am.get( "form" );
      formButton = new StreamflowButton( formAction );

      formButton.registerKeyboardAction( formAction, (KeyStroke) formAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );

      formButton.setHorizontalAlignment( SwingConstants.LEFT );

      builder.add( formButton, cc.xy( 1, 7, CellConstraints.FILL, CellConstraints.TOP ) );

      builder.add( valueBinder.bind( "form", selectedForm, linkValueConverter ),
            new CellConstraints( 3, 7, 1, 1, CellConstraints.LEFT, CellConstraints.CENTER, new Insets( 5, 0, 0, 0 ) ) );

      // Select template
      javax.swing.Action templateAction = am.get( "template" );
      templateButton = new StreamflowButton( templateAction );

      templateButton.registerKeyboardAction( templateAction, (KeyStroke) templateAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );

      templateButton.setHorizontalAlignment( SwingConstants.LEFT );

      builder.add( templateButton, cc.xy( 1, 9, CellConstraints.FILL, CellConstraints.TOP ) );

      builder.add( valueBinder.bind( "template", actionBinder.bind( "removeTemplate", selectedTemplate ) ),
            new CellConstraints( 3, 9, 1, 1, CellConstraints.LEFT, CellConstraints.CENTER, new Insets( 3, 0, 0, 0 ) ) );

      mailSelectionLabel.setText( i18n.text( AdministrationResources.changeMailSelectionMessage ) );
      mailSelectionLabel.setToolTipText( i18n.text( AdministrationResources.changeMailSelectionMessageHint ) );
      builder.add( mailSelectionLabel, cc.xy( 1, 11, CellConstraints.RIGHT, CellConstraints.BOTTOM ) );

      builder.add( valueBinder.bind( "mailSelectionMessage", actionBinder.bind( "changeMailSelectionMessage", mailSelectionField ) ),
            new CellConstraints( 3, 11, 1,1 , CellConstraints.FILL, CellConstraints.BOTTOM, new Insets( 3,0,0,0 )));

      PanelBuilder signPanel = new PanelBuilder( new FormLayout( "180dlu, 15dlu, 180dlu", "default:grow" ) );
      CellConstraints signPanelCc = new CellConstraints( );

      PanelBuilder primarySignPanel = new PanelBuilder( new FormLayout( "180dlu",
            "pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, default:grow" ) );
      CellConstraints primaryCc = new CellConstraints();

      primarySignPanel.addSeparator( text( AdministrationResources.signature_1 ), primaryCc.xy( 1, 1 ));

      primarySignPanel.add( valueBinder.bind( "primarysign", actionBinder.bind( "setSignActive1", signActive1 = new JCheckBox( text( AdministrationResources.active )) ),
            new ValueBinder.Converter<RequiredSignatureValue, Boolean>()
            {
               public Boolean toComponent( RequiredSignatureValue value )
               {
                  return value != null ? value.active().get() : Boolean.FALSE;
               }
            } ), primaryCc.xy( 1, 3 ));

      primarySignPanel.addLabel( text( AdministrationResources.name_label ), primaryCc.xy( 1, 5 ) );

      primarySignPanel.add( valueBinder.bind( "primarysign", actionBinder.bind( "setSignName1", signName1 = new JTextField(  )  ),
            new ValueBinder.Converter<RequiredSignatureValue, String>()
            {
               public String toComponent( RequiredSignatureValue value )
               {
                  return value != null ? value.name().get() : "";
               }
            } ) , primaryCc.xy( 1, 7 ));
      refreshComponents.enabledOn( "updateprimarysign", signName1 );

      primarySignPanel.add( new JLabel( text( AdministrationResources.description_label ) ) , primaryCc.xy( 1, 9 ));

      primarySignPanel.add( valueBinder.bind( "primarysign", actionBinder.bind( "setSignDescription1", signDescription1 = new JTextField(  )  ),
            new ValueBinder.Converter<RequiredSignatureValue, String>()
            {
               public String toComponent( RequiredSignatureValue value )
               {
                  return value != null ? value.description().get() : "";
               }
            } ), primaryCc.xy( 1, 11 ));
      refreshComponents.enabledOn( "updateprimarysign", signDescription1 );

      signPanel.add( primarySignPanel.getPanel(), signPanelCc.xy( 1, 1, CellConstraints.LEFT, CellConstraints.TOP ));


      PanelBuilder secondarySignPanel = new PanelBuilder( new FormLayout( "180dlu",
            "pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 12dlu, pref, 2dlu, pref, 2dlu, default:grow" ) );

      CellConstraints secondaryCc = new CellConstraints();

      secondarySignPanel.addSeparator( text( AdministrationResources.signature_2), secondaryCc.xy( 1, 1) );

      PanelBuilder secondaryOptionsPanel = new PanelBuilder(  new FormLayout( "70dlu,5dlu,70dlu,pref:grow","pref" ) );
      secondaryOptionsPanel.add( valueBinder.bind( "secondarysign", actionBinder.bind( "setSignActive2", signActive2 = new JCheckBox( text( AdministrationResources.active )) ),
            new ValueBinder.Converter<RequiredSignatureValue, Boolean>()
            {
               public Boolean toComponent( RequiredSignatureValue value )
               {
                  return value != null ? value.active().get() : Boolean.FALSE;
               }
            } ), secondaryCc.xy( 1, 1 ) );
      refreshComponents.enabledOn( "updatesecondarysignactive", signActive2 );

      secondaryOptionsPanel.add( valueBinder.bind( "secondarysign", actionBinder.bind(  "setSecondMandatory", mandatory2 = new JCheckBox( text( AdministrationResources.mandatory ) ) ),
            new ValueBinder.Converter<RequiredSignatureValue, Boolean>()
            {
               public Boolean toComponent( RequiredSignatureValue value )
               {
                  return value != null ? value.mandatory().get() : Boolean.FALSE;
               }
            } ), secondaryCc.xy( 3, 1 ) );
      refreshComponents.enabledOn( "updatesecondarysign", mandatory2 );

      secondarySignPanel.add( secondaryOptionsPanel.getPanel(), secondaryCc.xy( 1, 3 ) );

      secondarySignPanel.add( new JLabel( text( AdministrationResources.name_label ) ), secondaryCc.xy( 1, 5 )  );

      secondarySignPanel.add( valueBinder.bind( "secondarysign", actionBinder.bind( "setSignName2", signName2 = new JTextField(  )  ),
            new ValueBinder.Converter<RequiredSignatureValue, String>()
            {
               public String toComponent( RequiredSignatureValue value )
               {
                  return value != null ? value.name().get() : "";
               }
            } ), secondaryCc.xy( 1, 7 ) );
      refreshComponents.enabledOn( "updatesecondarysign", signName2 );

      secondarySignPanel.add( new JLabel( text( AdministrationResources.description_label ) ), secondaryCc.xy( 1, 9 ) );

      secondarySignPanel.add( valueBinder.bind( "secondarysign", actionBinder.bind( "setSignDescription2", signDescription2 = new JTextField(  )  ),
            new ValueBinder.Converter<RequiredSignatureValue, String>()
            {
               public String toComponent( RequiredSignatureValue value )
               {
                  return value != null ? value.description().get() :"";
               }
            } ), secondaryCc.xy( 1, 11 ) );
      refreshComponents.enabledOn( "updatesecondarysign", signDescription2 );

      // Select form
      javax.swing.Action form2Action = am.get( "setSecondForm" );
      form2Button = new StreamflowButton( form2Action );

      form2Button.registerKeyboardAction( form2Action, (KeyStroke) form2Action
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );

      form2Button.setHorizontalAlignment( SwingConstants.LEFT );
      refreshComponents.enabledOn( "updatesecondarysign", form2Button );

      PanelBuilder form2ButtonPanel = new PanelBuilder( new FormLayout( "70dlu, 5dlu, 150dlu:grow", "pref" ) );
      CellConstraints form2ButtonPanelCc = new CellConstraints( );

      form2ButtonPanel.add( form2Button, form2ButtonPanelCc.xy( 1, 1, CellConstraints.FILL, CellConstraints.TOP ) );

      form2ButtonPanel.add( valueBinder.bind( "secondarysign", selectedForm2 = new JLabel( ), new ValueBinder.Converter<RequiredSignatureValue, String>()
      {
         public String toComponent( RequiredSignatureValue value )
         {
            return value != null ? value.formdescription().get() : "";
         }
      } ), form2ButtonPanelCc.xy( 3, 1, CellConstraints.LEFT, CellConstraints.CENTER ) );

      secondarySignPanel.add( form2ButtonPanel.getPanel(), secondaryCc.xy( 1, 13 ) );


      secondarySignPanel.add( new JLabel( text( AdministrationResources.question_label ) ), secondaryCc.xy( 1, 17 ) );

      secondarySignPanel.add( valueBinder.bind( "secondarysign", actionBinder.bind( "setQuestion", formQuestion2 = new JTextField( ) ),
            new ValueBinder.Converter<RequiredSignatureValue, String>()
            {
               public String toComponent( RequiredSignatureValue value )
               {
                  return value != null ? value.question().get() : "";
               }
            } ), secondaryCc.xy( 1, 19 ) );
      refreshComponents.enabledOn( "updatesecondarysign", formQuestion2 );

      signPanel.add( secondarySignPanel.getPanel(), signPanelCc.xy( 3, 1, CellConstraints.LEFT, CellConstraints.TOP ));

      builder.add( signPanel.getPanel(),
            new CellConstraints( 1, 13, 3,1 , CellConstraints.FILL, CellConstraints.FILL, new Insets( 0,0,0,0 )));

      JPanel templatePanel = new JPanel( );
      templatePanel.setVisible( false );
      FormLayout templateFormLayout
            = new FormLayout( "75dlu, 5dlu, fill:p:grow",
            "pref, pref, fill:p:grow, pref");
      DefaultFormBuilder templateFormBuilder = new DefaultFormBuilder( templateFormLayout, templatePanel );
      templateFormBuilder.addSeparator(i18n.text(AdministrationResources.emailTemplates));
      templateFormBuilder.nextLine();
      templateFormBuilder.append( i18n.text(AdministrationResources.subject),  valueBinder.bind("subject", actionBinder.bind( "changeSubject", subject = new JTextField() ) ) );
      templateFormBuilder.nextLine();
      templateFormBuilder.append(new JScrollPane(emailTemplateList));
      templateFormBuilder.append(new JScrollPane( actionBinder.bind( "save", emailTemplateText ) ) );
      templateFormBuilder.nextLine();

      emailTemplateList.addListSelectionListener(new ListSelectionListener()
      {
         public void valueChanged(ListSelectionEvent e)
         {
            if (!e.getValueIsAdjusting())
            {
               if (emailTemplateList.getSelectedIndex() != -1)
               {
                  emailTemplateText.setText(model.getAccessPointValue().messages().get().get(emailTemplateList.getSelectedValue()));
               }
            }
         }
      });

      refreshComponents.visibleOn( "updatesecondarysign", templatePanel );

      builder.add( templatePanel, new CellConstraints( 1, 15, 3,1 , CellConstraints.FILL, CellConstraints.FILL, new Insets( 0,0,0,0 )));
      add( new JScrollPane( panel ), BorderLayout.CENTER );


      new RefreshWhenShowing( this, this );
   }

   @Action
   public Task project()
   {
      final SelectLinkDialog dialog = module.objectBuilderFactory().newObjectBuilder(SelectLinkDialog.class).use( model.getPossibleProjects() ).newInstance();
      dialogs.showOkCancelHelpDialog( projectButton, dialog, i18n.text( WorkspaceResources.choose_project ) );

      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            if (dialog.getSelectedLink() != null)
            {
               model.changeProject(dialog.getSelectedLink());
            }
         }
      };
   }

   @Action
   public Task casetype()
   {
      final SelectLinkDialog dialog = module.objectBuilderFactory().newObjectBuilder(SelectLinkDialog.class).use(
            i18n.text( WorkspaceResources.choose_casetype ),
            model.getPossibleCaseTypes() ).newInstance();
      dialogs.showOkCancelHelpDialog( caseTypeButton, dialog );

      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            if (dialog.getSelectedLink() != null)
            {
               model.changeCaseType(dialog.getSelectedLink());
            }
         }
      };

   }

   @Action
   public Task form()
   {
      // TODO very odd hack - how to solve state binder update issue during use of accelerator keys.
      Component focusOwner = WindowUtils.findWindow( this ).getFocusOwner();
      if (focusOwner != null)
         focusOwner.transferFocus();

      final SelectLinkDialog dialog = module.objectBuilderFactory().newObjectBuilder(SelectLinkDialog.class).use(
            model.getPossibleForms() ).newInstance();
      dialogs.showOkCancelHelpDialog( formButton, dialog,
            i18n.text( WorkspaceResources.choose_form ) );

      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            if (dialog.getSelectedLink() != null)
            {
               model.changeForm(dialog.getSelectedLink());
            }
         }
      };

   }

   @Action
   public Task template()
   {
      final SelectLinkDialog dialog = module.objectBuilderFactory().newObjectBuilder(SelectLinkDialog.class).use(
            model.getPossibleTemplates() ).newInstance();

      dialogs.showOkCancelHelpDialog( templateButton, dialog, i18n.text( WorkspaceResources.choose_template ));

      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            if (dialog.getSelectedLink() != null)
            {
               model.setTemplate( dialog.getSelectedLink() );
            }
         }
      };

   }

   @Action
   public Task removeTemplate()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.setTemplate( null );
         }
      };
   }

   @Action
   public Task changeMailSelectionMessage()
   {
         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               model.changeMailSelectionMessage( mailSelectionField.getText() );
            }
         };
   }

   @Action
   public Task setSignActive1()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.setSignActive1( signActive1.isSelected() );
         }
      };
   }

   @Action
   public Task setSignName1()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.setSignName1( signName1.getText() );
         }
      };
   }

   @Action
   public Task setSignDescription1()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.setSignDescription1( signDescription1.getText() );
         }
      };
   }


   @Action
   public Task setSignActive2()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.setSignActive2( signActive2.isSelected() );
         }
      };
   }

   @Action
   public Task setSignName2()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.setSignName2( signName2.getText() );
         }
      };
   }

   @Action
   public Task setSignDescription2()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.setSignDescription2( signDescription2.getText() );
         }
      };
   }

   @Action
   public Task setSecondForm()
   {
      // TODO very odd hack - how to solve state binder update issue during use of accelerator keys.
      Component focusOwner = WindowUtils.findWindow( this ).getFocusOwner();
      if (focusOwner != null)
         focusOwner.transferFocus();

      final SelectLinkDialog dialog = module.objectBuilderFactory().newObjectBuilder(SelectLinkDialog.class).use(
            model.getPossibleSecondForms() ).newInstance();
      dialogs.showOkCancelHelpDialog( form2Button, dialog,
            i18n.text( WorkspaceResources.choose_form ) );

      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            if (dialog.getSelectedLink() != null)
            {
               model.changeSecondForm( dialog.getSelectedLink() );
            }
         }
      };

   }

   @Action
   public Task setSecondMandatory()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.setSecondMandatory( mandatory2.isSelected() );
         }
      };
   }

   @Action
   public Task setQuestion()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.setQuestion( formQuestion2.getText() );
         }
      };
   }

   private void updateEnabled()
   {
      if (model.getAccessPointValue().project().get() == null)
      {
         caseTypeButton.setEnabled( false );
         labelButton.setEnabled( false );
         formButton.setEnabled( false );
      } else if (model.getAccessPointValue().caseType().get() == null)
      {
         caseTypeButton.setEnabled( true );
         labelButton.setEnabled( false );
         formButton.setEnabled( false );
      } else
      {
         caseTypeButton.setEnabled( true );
         labelButton.setEnabled( true );
         formButton.setEnabled( true );
      }
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (Events.matches( Events.withNames( "addedLabel",
            "removedLabel", "addedCaseType", "addedProject",
            "addedSelectedForm", "changedProject", "changedCaseType",
            "formPdfTemplateSet", "changedMailSelectionMessage", "createdRequiredSignature",
            "updatedRequiredSignature" ), transactions ))
      {
         refresh();
      }
   }

   public void refresh()
   {
      model.refresh();

      valueBinder.update( model.getAccessPointValue() );

      int selectedIndex = emailTemplateList.getSelectedIndex();
      DefaultListModel emailTemplateListModel = new DefaultListModel();
      for (String key : model.getAccessPointValue().messages().get().keySet())
      {
         emailTemplateListModel.addElement(key);
      }
      emailTemplateList.setModel(emailTemplateListModel);
      emailTemplateList.setSelectedIndex( selectedIndex );
   }

   @org.jdesktop.application.Action
   public Task changeSubject()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.changeSubject(subject.getText());
         }
      };
   }

   @org.jdesktop.application.Action
   public Task save()
   {
      final String template = emailTemplateText.getText();
      final String key = (String) emailTemplateList.getSelectedValue();

      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.updateTemplate(key, template);
            model.getAccessPointValue().messages().get().put(key, template);
         }
      };
   }
}

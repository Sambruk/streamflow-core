/*
 * Copyright (c) 2009, Rickard √ñberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui.task;

import org.netbeans.spi.wizard.WizardController;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPanelProvider;

import javax.swing.*;
import java.util.Map;

/**
 * JAVADOC
 */
public class FormSubmissionWizardPanelProvider
    extends WizardPanelProvider
{
    public static final String FORM_SUBMISSION_STEP = "FORM_SUBMISSION_STEP";

    private FormSubmitWizardPage submitView;

    protected FormSubmissionWizardPanelProvider(FormSubmitWizardPage submitView,
                                                String title, String[] pageIds, String[] pageNames)
    {
        super(title, pageIds, pageNames);
        this.submitView = submitView;
    }

    protected JComponent createPanel(WizardController wizardController, String pageId, final Map map)
    {
        submitView.updateView(pageId);
        return submitView;
    }

    @Override
    protected Object finish(Map map) throws WizardException
    {
        submitView.submit();
        return null;
    }
}
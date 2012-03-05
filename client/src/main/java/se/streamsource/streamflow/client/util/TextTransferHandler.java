/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.client.util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
 
/**
 * An implementation of TransferHandler that adds support for the
 * import and export of text using drag and drop and cut/copy/paste.
 */
public class TextTransferHandler extends TransferHandler {
   
    /**
     * Perform the actual import.  This method supports both drag and
     * drop and cut/copy/paste.
     */
    public boolean importData(TransferHandler.TransferSupport support) {
        //If we can't handle the import, bail now.
        if (!canImport(support)) {
            return false;
        }
 
        //Fetch the data -- bail if this fails
        String data;
        try {
            data = (String)support.getTransferable().getTransferData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException e) {
            return false;
        } catch (java.io.IOException e) {
            return false;
        }
 
        JTextField tc = (JTextField)support.getComponent();      
        tc.replaceSelection(data);
        return true;
    }
 
    /**
     * These text fields handle both copy and move actions.
     */
    public int getSourceActions(JComponent c) {
        return COPY;
    }

 
    /**
     * We only support importing strings.
     */
    public boolean canImport(TransferHandler.TransferSupport support) {
        // we only import Strings
        if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return false;
        }
        return true;
    }
}
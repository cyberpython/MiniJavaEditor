/*
 * Copyright 2009 Georgios "cyberpython" Migdos cyberpython@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License
 *       at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package documentcontainer;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author cyberpython
 */
public class DocumentIOManager {

    private DocumentContainer container;

    private File lastOpenDir;
    private File lastSaveDir;

    private ArrayDeque<File> recentlyAccessed;
    private Class<?> classForPackageNode;

    private int maxRecentlyOpened;

    private String confirmOverwriteMessage;
    private String overwriteDialogTitle;

    private String fileModifiedWarningMessage;
    private String fileModifiedWarningDialog;

    public DocumentIOManager(DocumentContainer container, Class<?> classForPackageNode){

        this.container = container;

        this.lastOpenDir = null;
        this.lastSaveDir = null;

        this.recentlyAccessed = new ArrayDeque<File>();
        this.classForPackageNode = classForPackageNode;
        this.maxRecentlyOpened = 10;

        this.confirmOverwriteMessage = "File:\n    %filename%\nalready exists!\n\nDo you want to overwrite it?";
        this.overwriteDialogTitle = "Overwrite file";

        this.fileModifiedWarningMessage = "File:\n    %filename%\nhas been modified!\n\nDo you want to save changes?";
        this.fileModifiedWarningDialog = "File modified";
    }

    public String getConfirmOverwriteMessage(){
        return this.confirmOverwriteMessage;
    }

    /**
     * Sets the message that will be displayed by the confirmOverwrite() method
     * @param msg The message to be displayed. Use %filename% as a placeholder for filename.
     */
    public void setConfirmOverwriteMessage(String msg){
        this.confirmOverwriteMessage = msg;
    }

    public String getOverwriteDialogTitle(){
        return this.overwriteDialogTitle;
    }

    public void setOverwriteDialogTitle(String title){
        this.overwriteDialogTitle = title;
    }



    public String getFileModifiedWarningMessage(){
        return this.fileModifiedWarningMessage;
    }

    /**
     * Sets the message that will be displayed by the showModifiedWarning() method
     * @param msg The message to be displayed. Use %filename% as a placeholder for filename.
     */
    public void setFileModifiedWarningMessage(String msg){
        this.fileModifiedWarningMessage = msg;
    }

    public String getFileModifiedWarningDialogTitle(){
        return this.fileModifiedWarningDialog;
    }

    public void setFileModifiedWarningDialogTitle(String title){
        this.fileModifiedWarningDialog = title;
    }



    public void loadRecentlyAccessed(){
        if(this.classForPackageNode!=null){
            this.recentlyAccessed.clear();
            Preferences pref = Preferences.userNodeForPackage(this.classForPackageNode);
            String recentlyOpened = pref.get("RecentlyOpenedFiles", "");
            String[] itemPaths = recentlyOpened.split(";");
            for (String path:itemPaths) {
                File f = new File(path);
                if (f.exists()) {
                    this.recentlyAccessed.add(f);
                }
            }
            this.container.recentlyAccessedFilesChanged();
        }
    }

    private String recentlyAccessedToString(){
        StringBuffer s = new StringBuffer();
        for (Iterator<File> it = recentlyAccessed.iterator(); it.hasNext();) {
            File file = it.next();
            s.append(file.getAbsolutePath()+";");
        }
        return s.toString();
    }

    private void storeRecentlyAccessed(){
        if(this.classForPackageNode!=null){
            String recentlyOpened = this.recentlyAccessedToString();
            Preferences pref = Preferences.userNodeForPackage(this.classForPackageNode);
            pref.put("RecentlyOpenedFiles", recentlyOpened);
        }
    }

    private void addToRecentlyAccessed(File f){
        if(this.recentlyAccessed.contains(f)){
            this.recentlyAccessed.remove(f);
        }
        if(this.recentlyAccessed.size()>=this.maxRecentlyOpened){
            this.recentlyAccessed.removeLast();
        }
        this.recentlyAccessed.push(f);
        this.storeRecentlyAccessed();

        this.container.recentlyAccessedFilesChanged();

    }

    public File[] getRecentlyAccessedFiles(){
        return this.recentlyAccessed.toArray(new File[0]);
    }

    public boolean saveAs(Component parent, FileFilter filter) throws IOException {

        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(this.lastSaveDir);
        fc.setFileFilter(filter);
        int selection = fc.showSaveDialog(parent);
        if (selection == JFileChooser.APPROVE_OPTION) {
            this.lastSaveDir = fc.getCurrentDirectory();
            File f = fc.getSelectedFile();
            if (f != null) {
                if (f.exists()) {
                    if (confirmOverwrite(parent, f.getAbsolutePath())) {
                        container.saveDocument(f);
                        this.addToRecentlyAccessed(f);
                        return true;
                    } else {
                        return saveAs(parent, filter);
                    }
                } else {
                    container.saveDocument(f);
                    this.addToRecentlyAccessed(f);
                    return true;
                }
            }
        }
        return false;

    }

    public boolean confirmOverwrite(Component parent, String filename) {
        String msg = this.confirmOverwriteMessage.replace("%filename%", filename);
        int result = JOptionPane.showConfirmDialog(parent, msg, this.overwriteDialogTitle, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            return true;
        }
        return false;
    }

    public boolean save(Component parent, FileFilter filter) throws IOException {

        boolean isNew = container.isCurrentDocNew();

        if (!isNew) {
            File output = container.getCurrentDocFile();
            if (output != null) {
                container.saveDocument(output);
                this.addToRecentlyAccessed(output);
                return true;
            } else {
                return saveAs(parent, filter);
            }
        } else {
            return saveAs(parent, filter);
        }

    }

    public void open(Component parent, FileFilter filter) throws IOException {
        boolean modified = container.isCurrentDocModified();

        if (modified) {
            int saveChanges = showModifiedWarning(parent, container.getCurrentDocTitle());
            if (saveChanges == JOptionPane.YES_OPTION) {
                if (save(parent, filter)) {
                    openFile(parent, filter);
                }
            } else if (saveChanges == JOptionPane.NO_OPTION) {
                openFile(parent, filter);
            }
        } else {
            openFile(parent, filter);
        }

    }

    private void openFile(Component parent, FileFilter filter) throws IOException {

        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(this.lastOpenDir);
        fc.setFileFilter(filter);
        int selection = fc.showOpenDialog(parent);
        if (selection == JFileChooser.APPROVE_OPTION) {
            this.lastOpenDir = fc.getCurrentDirectory();
            File f = fc.getSelectedFile();
            if (f != null) {
                container.openDocument(f);
                this.addToRecentlyAccessed(f);
            }
        }

    }

    public void open(Component parent, File f, FileFilter filter) throws IOException {
        boolean modified = container.isCurrentDocModified();

        if (modified) {
            int saveChanges = showModifiedWarning(parent, container.getCurrentDocTitle());
            if (saveChanges == JOptionPane.YES_OPTION) {
                if (save(parent, filter)) {
                    openFile(parent, f);
                }
            } else if (saveChanges == JOptionPane.NO_OPTION) {
                openFile(parent, f);
            }
        } else {
            openFile(parent, f);
        }

    }

    private void openFile(Component parent, File f) throws IOException {
        if (f != null) {
            container.openDocument(f);
            this.addToRecentlyAccessed(f);
        }
    }

    public void createNew(Component parent, FileFilter filter) throws IOException {
        boolean modified = container.isCurrentDocModified();

        if (modified) {
            int saveChanges = showModifiedWarning(parent, container.getCurrentDocTitle());
            if (saveChanges == JOptionPane.YES_OPTION) {
                if (save(parent, filter)) {
                    container.newDocument();
                }
            } else if (saveChanges == JOptionPane.NO_OPTION) {
                container.newDocument();
            }
        } else {
            container.newDocument();
        }
    }

    public int showModifiedWarning(Component parent, String filename) {
        String msg = this.fileModifiedWarningMessage.replace("%filename%", filename);
        return JOptionPane.showConfirmDialog(parent, msg, this.fileModifiedWarningDialog, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
    }
}

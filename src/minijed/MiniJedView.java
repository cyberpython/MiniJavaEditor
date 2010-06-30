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
package minijed;

import documentcontainer.DocumentContainer;
import documentcontainer.DocumentIOManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import javax.swing.DefaultListModel;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.event.CaretEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.Highlighter;
import minijava.YourMiniJavaCompiler;
import minijed.compiler.infrastructure.MiniJavaCompiler;
import minijed.compiler.infrastructure.MiniJavaLexerException;
import minijed.compiler.infrastructure.MiniJavaParserException;
import minijed.editor.EditorView;
import minijed.editor.EditorViewContainer;
import minijed.filefilter.MiniJavaFileFilter;

/**
 * The application's main frame.
 */
public class MiniJedView extends FrameView implements EditorViewContainer, DocumentContainer {

    private final String applicationTitle = "MiniJed";
    private final String KEYWORDS_DEFAULT = "0x002967";
    private final String NUMBERS_DEFAULT = "0x016800";
    private final String STRINGS_DEFAULT = "0x650067";
    private final String OPERATORS_DEFAULT = "0x670001";
    private final String COMMENTS_DEFAULT = "0x114e21";
    private final String TYPES_DEFAULT = "0x002967";
    private final String IDENTIFIERS_DEFAULT = "0x000000";
    private final String DEFAULT_TEXT_DEFAULT = "0x000000";
    private final Color ERROR_HIGHLIGHT_COLOR = new Color(255, 139, 139);
    private int newDocumentsCounter;
    private JFrame frame;
    private DocumentIOManager ioManager;
    private EditorView editorView1;
    private Font editorFont;
    private String[] editorColors;
    private Object errorHighlight;
    private DefaultListModel compilerMessagesModel;
    private JTextArea tmp;
    private AppRunner appRunner;

    public MiniJedView(SingleFrameApplication app) {
        super(app);

        preInit();

        initComponents();

        postInit();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = MiniJedApp.getApplication().getMainFrame();
            aboutBox = new MiniJedAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        MiniJedApp.getApplication().show(aboutBox);
    }

    private void preInit() {
        this.getApplication().addExitListener(new ConfirmExit());
        this.frame = this.getFrame();
        this.newDocumentsCounter = 0;
        this.errorHighlight = null;
        this.frame.setTitle(applicationTitle);
        this.ioManager = new DocumentIOManager(this, null);
        this.tmp = new JTextArea();
        this.compilerMessagesModel = new DefaultListModel();
        loadPreferences();
    }

    private void postInit() {
        this.appRunner = new AppRunner(jEditorPane1);
        createDocument();
        this.jList1.setCellRenderer(new CompilerMessagesListCellRenderer());
        this.jList1.setModel(this.compilerMessagesModel);
    }

    // <editor-fold defaultstate="collapsed" desc="Settings and preferences">
    private void loadPreferences() {
        loadEditorFont();
        loadEditorColors();
    }

    private void loadEditorFont() {
        editorFont = loadDefaultFont();
    }

    private void loadEditorColors() {
        this.editorColors = new String[8];

        this.editorColors[0] = this.KEYWORDS_DEFAULT;
        this.editorColors[1] = this.NUMBERS_DEFAULT;
        this.editorColors[2] = this.STRINGS_DEFAULT;
        this.editorColors[3] = this.OPERATORS_DEFAULT;
        this.editorColors[4] = this.COMMENTS_DEFAULT;
        this.editorColors[5] = this.TYPES_DEFAULT;
        this.editorColors[6] = this.IDENTIFIERS_DEFAULT;
        this.editorColors[7] = this.DEFAULT_TEXT_DEFAULT;

    }

    private Font loadDefaultFont() {

        String[] fontsList = {"Liberation Mono", "Courier New"};
        final int DEFAULT_SIZE = 14;

        try {
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] familynames = env.getAvailableFontFamilyNames();
            Arrays.sort(familynames);

            int searchResult = 0;
            int index = 0;
            int length = fontsList.length;
            do {
                searchResult = Arrays.binarySearch(familynames, new String(fontsList[index]));
                index++;

            } while ((searchResult < 0) && (index < length));

            if (searchResult >= 0) {
                Font f = new Font(fontsList[index - 1], Font.PLAIN, DEFAULT_SIZE);
                return f;
            } else {
                return new Font("monospaced", Font.PLAIN, DEFAULT_SIZE);
            }

        } catch (Exception e) {
            return new Font("monospaced", Font.PLAIN, DEFAULT_SIZE);
        }

    }

    //</editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Document I/O">
    private void createDocument() {
        if (this.editorView1 != null) {
            this.jTabbedPane2.remove(this.editorView1);
            this.editorView1.setVisible(false);
        }
        this.editorView1 = new EditorView(++newDocumentsCounter, this);
        this.jTabbedPane2.add("Code Editor", this.editorView1);
        this.editorView1.setVisible(true);
        this.editorView1.initEditor(this.jPopupMenu1, this.editorFont, this.editorColors);

        enableDisableUndoRedo();
        this.compilerMessagesModel.clear();
    }

    private void createDocument(File f) {
        if (this.editorView1 != null) {
            this.jTabbedPane2.remove(this.editorView1);
            this.editorView1.setVisible(false);
        }
        this.editorView1 = new EditorView(f, this);
        this.jTabbedPane2.add("Code Editor", this.editorView1);
        this.editorView1.initEditor(this.jPopupMenu1, this.editorFont, this.editorColors);

        enableDisableUndoRedo();
        this.compilerMessagesModel.clear();
    }

    private void insertFile(File f) {
        if (f != null) {
            if (editorView1 != null) {
                try {
                    InputStreamReader reader = new InputStreamReader(new FileInputStream(f), "UTF-8");
                    StringBuffer buffer = new StringBuffer();
                    int c = 0;
                    try {
                        while ((c = reader.read()) != -1) {
                            buffer.append((char) c);
                        }
                    } catch (IOException ioe) {
                    }
                    editorView1.insertText(buffer.toString());
                } catch (FileNotFoundException fnfe) {
                } catch (UnsupportedEncodingException uee) {
                }
            }
        }
    }

    public void createNew() {
        try {
            ioManager.createNew(frame, new MiniJavaFileFilter());
        } catch (IOException ioe) {
        }
    }

    public void open() {
        try {
            ioManager.open(frame, new MiniJavaFileFilter());
        } catch (IOException ioe) {
        }
    }

    public void open(File f) {
        try {
            ioManager.open(frame, f, new MiniJavaFileFilter());
        } catch (IOException ioe) {
        }
    }

    public boolean save() {
        try {
            return ioManager.save(frame, new MiniJavaFileFilter());
        } catch (IOException ioe) {
            return false;
        }
    }

    public boolean saveAs() {

        try {
            return ioManager.saveAs(frame, new MiniJavaFileFilter());
        } catch (IOException ioe) {
            return false;
        }
    }

    //</editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Document editing">
    public void undo() {
        editorView1.undo();
        enableDisableUndoRedo();
    }

    public void redo() {
        editorView1.redo();
        enableDisableUndoRedo();
    }

    public void cut() {
        editorView1.cut();
    }

    public void copy() {
        editorView1.copy();
    }

    public void paste() {
        editorView1.paste();
    }

    public void deleteSelection() {
        editorView1.deleteSelection();
    }

    public void selectAll() {
        editorView1.selectAll();
    }

    //</editor-fold>
    // <editor-fold defaultstate="collapsed" desc="GUI updating">
    private void enableDisableUndoRedo() {
        if (editorView1 != null) {
            boolean canUndo = editorView1.canUndo();
            boolean canRedo = editorView1.canRedo();

            jButton5.setEnabled(canUndo);
            jMenuItem6.setEnabled(canUndo);
            jMenuItem14.setEnabled(canUndo);

            jButton6.setEnabled(canRedo);
            jMenuItem7.setEnabled(canRedo);
            jMenuItem15.setEnabled(canRedo);
        }
    }

    private void enableUndoControls() {
        if (editorView1 != null) {
            boolean canUndo = true;

            jButton5.setEnabled(canUndo);
            jMenuItem6.setEnabled(canUndo);
            jMenuItem14.setEnabled(canUndo);
        }
    }

    private void enableDisableTextManipulation() {
        if (editorView1 != null) {
            String selectedText = editorView1.getSelectedText();
            boolean textIsSelected;
            if (selectedText != null) {
                textIsSelected = (selectedText.length() > 0);
            } else {
                textIsSelected = false;
            }

            jButton7.setEnabled(textIsSelected);
            jButton8.setEnabled(textIsSelected);
            jMenuItem8.setEnabled(textIsSelected);
            jMenuItem9.setEnabled(textIsSelected);
            jMenuItem16.setEnabled(textIsSelected);
            jMenuItem17.setEnabled(textIsSelected);

        }
    }

    private void enableDisableSave() {
        if (editorView1 != null) {
            boolean canSave = editorView1.isModified();

            jButton3.setEnabled(canSave);
            jMenuItem3.setEnabled(canSave);
        }
    }

    private void updatePositionLabel(int caretX, int caretY) {
        this.statusMessageLabel.setText("Line " + caretY + ", Char. " + caretX);
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="GUI Dialogs management">
    public void showHelpContents() {
        /*   java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
        try{
        URI a = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
        File f = new File(a);
        String cp = f.getParentFile().getCanonicalPath();
        System.out.println(cp);
        File f2 = new File(cp+File.separator+"help"+File.separator+"index.html");
        desktop.open(f2);
        }catch(Exception e){
        System.err.println(e.toString());
        }       */
    }

    //</editor-fold>
    // <editor-fold defaultstate="collapsed" desc="EditorViewContainer implementation">
    public void notifyDocumentModified(String title, boolean modified) {
        frame.setTitle(applicationTitle + " - " + title);
        if (modified) {
            //statusMessageLabel.setText("Modified");
        } else {
            //statusMessageLabel.setText("");
        }
        enableDisableUndoRedo();
        enableDisableSave();
        removeErrorHighlight();
    }

    public void notifyCaretChanged(CaretEvent e) {
        enableDisableTextManipulation();
        Point p = editorView1.getCaretPosition();
        updatePositionLabel(p.x + 1, p.y + 1);
    }

    public void notifyFirstUndoableEditHappened(UndoableEditEvent evt) {
        enableUndoControls();
    }

    //</editor-fold>
    // <editor-fold defaultstate="collapsed" desc="DocumentContainer implementation">
    public boolean isCurrentDocModified() {
        return this.editorView1.isModified();
    }

    public void setCurrentDocModified(boolean modified) {
        this.editorView1.setModified(modified);
    }

    public boolean isCurrentDocNew() {
        return this.editorView1.isNewFile();
    }

    public File getCurrentDocFile() {
        return this.editorView1.getFile();
    }

    public String getCurrentDocTitle() {
        return this.editorView1.getTitle();
    }

    public boolean saveDocument(File output) {
        return this.editorView1.saveFile(output);
    }

    public boolean openDocument(File input) {
        this.createDocument(input);
        return true;
    }

    public boolean newDocument() {
        this.createDocument();
        return true;
    }

    public void recentlyAccessedFilesChanged() {
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Application exit event handling">
    class ConfirmExit implements org.jdesktop.application.Application.ExitListener {

        public boolean canExit(java.util.EventObject e) {
            Object source = (e != null) ? e.getSource() : null;
            Component owner = (source instanceof Component) ? (Component) source : null;
            return queryCloseApp();
        }

        public void willExit(java.util.EventObject e) {
        }
    }

    public boolean queryCloseApp() {
        if (this.editorView1.isModified()) {
            int res = ioManager.showModifiedWarning(frame, this.editorView1.getTitle());
            if (res == JOptionPane.YES_OPTION) {
                if (save()) {
                    return true;
                }
                return false;
            } else if (res == JOptionPane.NO_OPTION) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    //</editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Printing">
    public void print() {
        this.editorView1.print();
    }

    //</editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Compile/Run related">
    public void run() {
        if(this.appRunner.isIdle()){
            if (this.compile()) {
                String srcPath = this.getCurrentDocFile().getAbsolutePath();
                int extPosition = srcPath.lastIndexOf(".");
                String targetPath = srcPath.substring(0, extPosition) + ".class";
                File targetFile = new File(targetPath);

                if (targetFile.isFile()) {
                    this.execute();
                }
            }
        }
    }

    private void showRuntimeWindow() {
        this.jTabbedPane1.setSelectedIndex(1);
        this.getFrame().repaint();
    }

    private void execute() {

        showRuntimeWindow();

        File src = this.getCurrentDocFile();
        File srcDir = src.getParentFile();

        String fname = src.getName();
        int extPosition = fname.lastIndexOf(".");
        String target = fname.substring(0, extPosition);

        String cmd = "java " + target;
        
        appRunner = new AppRunner(jEditorPane1);
        appRunner.setParams(cmd, srcDir);
        appRunner.start();

    }

    public boolean compile() {
        if (save()) {
            removeErrorHighlight();
            this.compilerMessagesModel.clear();
            this.jTabbedPane1.setSelectedIndex(0);
            MiniJavaCompiler compiler = new YourMiniJavaCompiler();
            try {

                this.compilerMessagesModel.addElement(new CompilerMessage("Compiling..."));
                compiler.compile(new StringReader(this.editorView1.getEditorPane().getText()), this.compilerMessagesModel, this.getCurrentDocFile().getParentFile());
                this.compilerMessagesModel.addElement(new CompilerSuccessMessage("Done."));
                return true;

            } catch (MiniJavaLexerException le) {
                this.compilerMessagesModel.addElement(new CompilerErrorMessage(le.getMessage()));
                return false;
            } catch (MiniJavaParserException pe) {
                int pos = pe.getToken().getPos() - 1;
                this.compilerMessagesModel.addElement(new CompilerErrorMessage(pe.getToken().getLine(), pos, pos + pe.getToken().getText().length(), pe.getMessage()));
                return false;
            } catch (IOException ioe) {
                this.compilerMessagesModel.addElement(new CompilerErrorMessage(ioe.toString()));
                return false;
            }
        }
        return false;
    }

    public int setErrorHighlight(int line) {
        removeErrorHighlight();
        this.tmp.setText(this.editorView1.getEditorPane().getText());
        try {
            int start = tmp.getLineStartOffset(line - 1);
            int end = tmp.getLineEndOffset(line - 1);
            this.errorHighlight = this.editorView1.highlight(start, end, this.ERROR_HIGHLIGHT_COLOR);
            return start;
        } catch (Exception e) {
            return -1;
        }

    }

    public void removeErrorHighlight() {
        if ((this.editorView1 != null) && (this.errorHighlight != null)) {
            Highlighter h = this.editorView1.getEditorPane().getHighlighter();
            h.removeHighlight(this.errorHighlight);
        }
    }

    public void hilightError(int index) {
        if (index > -1) {
            removeErrorHighlight();
            Object o = jList1.getModel().getElementAt(index);
            if (o instanceof CompilerMessage) {
                CompilerMessage msg = (CompilerMessage) o;
                int line = msg.getLine();
                int start = msg.getStart();
                int end = msg.getEnd();
                int lineOffset = -1;
                System.out.println(line);
                if (line != -1) {
                    lineOffset = setErrorHighlight(line);
                }
                if ((start != -1) && (lineOffset != -1)) {
                    JEditorPane editor = this.editorView1.getEditorPane();
                    int pos = start + lineOffset;
                    editor.setSelectionStart(pos);
                    editor.setSelectionEnd(pos);
                    editor.requestFocus();
                }
            }
        }
    }

    //</editor-fold>
    public void exit() {
        this.getApplication().exit();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButton4 = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jSeparator11 = new javax.swing.JToolBar.Separator();
        jButton10 = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        jMenuItem5 = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JSeparator();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenuItem9 = new javax.swing.JMenuItem();
        jMenuItem10 = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JSeparator();
        jMenuItem11 = new javax.swing.JMenuItem();
        jMenuItem12 = new javax.swing.JMenuItem();
        jMenuItem13 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem21 = new javax.swing.JMenuItem();
        jMenuItem22 = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        jMenuItem14 = new javax.swing.JMenuItem();
        jMenuItem15 = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JSeparator();
        jMenuItem16 = new javax.swing.JMenuItem();
        jMenuItem17 = new javax.swing.JMenuItem();
        jMenuItem18 = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JSeparator();
        jMenuItem19 = new javax.swing.JMenuItem();
        jMenuItem20 = new javax.swing.JMenuItem();

        mainPanel.setName("mainPanel"); // NOI18N

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(minijed.MiniJedApp.class).getContext().getResourceMap(MiniJedView.class);
        jButton1.setIcon(resourceMap.getIcon("jButton1.icon")); // NOI18N
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setToolTipText(resourceMap.getString("jButton1.toolTipText")); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setName("jButton1"); // NOI18N
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton1);

        jButton2.setIcon(resourceMap.getIcon("jButton2.icon")); // NOI18N
        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setToolTipText(resourceMap.getString("jButton2.toolTipText")); // NOI18N
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setName("jButton2"); // NOI18N
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton2);

        jButton3.setIcon(resourceMap.getIcon("jButton3.icon")); // NOI18N
        jButton3.setText(resourceMap.getString("jButton3.text")); // NOI18N
        jButton3.setToolTipText(resourceMap.getString("jButton3.toolTipText")); // NOI18N
        jButton3.setEnabled(false);
        jButton3.setFocusable(false);
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3.setName("jButton3"); // NOI18N
        jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton3);

        jSeparator1.setName("jSeparator1"); // NOI18N
        jToolBar1.add(jSeparator1);

        jButton4.setIcon(resourceMap.getIcon("jButton4.icon")); // NOI18N
        jButton4.setText(resourceMap.getString("jButton4.text")); // NOI18N
        jButton4.setToolTipText(resourceMap.getString("jButton4.toolTipText")); // NOI18N
        jButton4.setFocusable(false);
        jButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton4.setName("jButton4"); // NOI18N
        jButton4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton4);

        jSeparator2.setName("jSeparator2"); // NOI18N
        jToolBar1.add(jSeparator2);

        jButton5.setIcon(resourceMap.getIcon("jButton5.icon")); // NOI18N
        jButton5.setText(resourceMap.getString("jButton5.text")); // NOI18N
        jButton5.setToolTipText(resourceMap.getString("jButton5.toolTipText")); // NOI18N
        jButton5.setEnabled(false);
        jButton5.setFocusable(false);
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton5.setName("jButton5"); // NOI18N
        jButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton5);

        jButton6.setIcon(resourceMap.getIcon("jButton6.icon")); // NOI18N
        jButton6.setText(resourceMap.getString("jButton6.text")); // NOI18N
        jButton6.setToolTipText(resourceMap.getString("jButton6.toolTipText")); // NOI18N
        jButton6.setEnabled(false);
        jButton6.setFocusable(false);
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton6.setName("jButton6"); // NOI18N
        jButton6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton6);

        jSeparator3.setName("jSeparator3"); // NOI18N
        jToolBar1.add(jSeparator3);

        jButton7.setIcon(resourceMap.getIcon("jButton7.icon")); // NOI18N
        jButton7.setText(resourceMap.getString("jButton7.text")); // NOI18N
        jButton7.setToolTipText(resourceMap.getString("jButton7.toolTipText")); // NOI18N
        jButton7.setEnabled(false);
        jButton7.setFocusable(false);
        jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton7.setName("jButton7"); // NOI18N
        jButton7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton7);

        jButton8.setIcon(resourceMap.getIcon("jButton8.icon")); // NOI18N
        jButton8.setText(resourceMap.getString("jButton8.text")); // NOI18N
        jButton8.setToolTipText(resourceMap.getString("jButton8.toolTipText")); // NOI18N
        jButton8.setEnabled(false);
        jButton8.setFocusable(false);
        jButton8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton8.setName("jButton8"); // NOI18N
        jButton8.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton8);

        jButton9.setIcon(resourceMap.getIcon("jButton9.icon")); // NOI18N
        jButton9.setText(resourceMap.getString("jButton9.text")); // NOI18N
        jButton9.setToolTipText(resourceMap.getString("jButton9.toolTipText")); // NOI18N
        jButton9.setFocusable(false);
        jButton9.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton9.setName("jButton9"); // NOI18N
        jButton9.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton9);

        jSeparator4.setName("jSeparator4"); // NOI18N
        jToolBar1.add(jSeparator4);

        jButton11.setIcon(resourceMap.getIcon("jButton11.icon")); // NOI18N
        jButton11.setText(resourceMap.getString("jButton11.text")); // NOI18N
        jButton11.setToolTipText(resourceMap.getString("jButton11.toolTipText")); // NOI18N
        jButton11.setFocusable(false);
        jButton11.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton11.setName("jButton11"); // NOI18N
        jButton11.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton11);

        jButton12.setIcon(resourceMap.getIcon("jButton12.icon")); // NOI18N
        jButton12.setText(resourceMap.getString("jButton12.text")); // NOI18N
        jButton12.setFocusable(false);
        jButton12.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton12.setName("jButton12"); // NOI18N
        jButton12.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton12);

        jSeparator11.setName("jSeparator11"); // NOI18N
        jToolBar1.add(jSeparator11);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(minijed.MiniJedApp.class).getContext().getActionMap(MiniJedView.class, this);
        jButton10.setAction(actionMap.get("showAboutBox")); // NOI18N
        jButton10.setIcon(resourceMap.getIcon("jButton10.icon")); // NOI18N
        jButton10.setText(resourceMap.getString("jButton10.text")); // NOI18N
        jButton10.setToolTipText(resourceMap.getString("jButton10.toolTipText")); // NOI18N
        jButton10.setFocusable(false);
        jButton10.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton10.setName("jButton10"); // NOI18N
        jButton10.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton10);

        jSplitPane1.setDividerLocation(300);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(1.0);
        jSplitPane1.setContinuousLayout(true);
        jSplitPane1.setDoubleBuffered(true);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        jTabbedPane2.setName("jTabbedPane2"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 679, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );

        jSplitPane1.setTopComponent(jPanel1);

        jPanel2.setName("jPanel2"); // NOI18N

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jList1.setBackground(resourceMap.getColor("jList1.background")); // NOI18N
        jList1.setName("jList1"); // NOI18N
        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jList1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        jTabbedPane1.addTab(resourceMap.getString("jScrollPane1.TabConstraints.tabTitle"), jScrollPane1); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jEditorPane1.setContentType(resourceMap.getString("jEditorPane1.contentType")); // NOI18N
        jEditorPane1.setText(resourceMap.getString("jEditorPane1.text")); // NOI18N
        jEditorPane1.setName("jEditorPane1"); // NOI18N
        jScrollPane2.setViewportView(jEditorPane1);

        jTabbedPane1.addTab(resourceMap.getString("jScrollPane2.TabConstraints.tabTitle"), jScrollPane2); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 679, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
        );

        jSplitPane1.setRightComponent(jPanel2);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 679, Short.MAX_VALUE)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 679, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setIcon(resourceMap.getIcon("jMenuItem1.icon")); // NOI18N
        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        fileMenu.add(jMenuItem1);

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setIcon(resourceMap.getIcon("jMenuItem2.icon")); // NOI18N
        jMenuItem2.setText(resourceMap.getString("jMenuItem2.text")); // NOI18N
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        fileMenu.add(jMenuItem2);

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem3.setIcon(resourceMap.getIcon("jMenuItem3.icon")); // NOI18N
        jMenuItem3.setText(resourceMap.getString("jMenuItem3.text")); // NOI18N
        jMenuItem3.setName("jMenuItem3"); // NOI18N
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        fileMenu.add(jMenuItem3);

        jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem4.setIcon(resourceMap.getIcon("jMenuItem4.icon")); // NOI18N
        jMenuItem4.setText(resourceMap.getString("jMenuItem4.text")); // NOI18N
        jMenuItem4.setName("jMenuItem4"); // NOI18N
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        fileMenu.add(jMenuItem4);

        jSeparator5.setName("jSeparator5"); // NOI18N
        fileMenu.add(jSeparator5);

        jMenuItem5.setIcon(resourceMap.getIcon("jMenuItem5.icon")); // NOI18N
        jMenuItem5.setText(resourceMap.getString("jMenuItem5.text")); // NOI18N
        jMenuItem5.setName("jMenuItem5"); // NOI18N
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        fileMenu.add(jMenuItem5);

        jSeparator6.setName("jSeparator6"); // NOI18N
        fileMenu.add(jSeparator6);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setIcon(resourceMap.getIcon("exitMenuItem.icon")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        jMenuItem6.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem6.setIcon(resourceMap.getIcon("jMenuItem6.icon")); // NOI18N
        jMenuItem6.setText(resourceMap.getString("jMenuItem6.text")); // NOI18N
        jMenuItem6.setName("jMenuItem6"); // NOI18N
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem6);

        jMenuItem7.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem7.setIcon(resourceMap.getIcon("jMenuItem7.icon")); // NOI18N
        jMenuItem7.setText(resourceMap.getString("jMenuItem7.text")); // NOI18N
        jMenuItem7.setName("jMenuItem7"); // NOI18N
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem7);

        jSeparator7.setName("jSeparator7"); // NOI18N
        jMenu1.add(jSeparator7);

        jMenuItem8.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem8.setIcon(resourceMap.getIcon("jMenuItem8.icon")); // NOI18N
        jMenuItem8.setText(resourceMap.getString("jMenuItem8.text")); // NOI18N
        jMenuItem8.setName("jMenuItem8"); // NOI18N
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem8);

        jMenuItem9.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem9.setIcon(resourceMap.getIcon("jMenuItem9.icon")); // NOI18N
        jMenuItem9.setText(resourceMap.getString("jMenuItem9.text")); // NOI18N
        jMenuItem9.setName("jMenuItem9"); // NOI18N
        jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem9ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem9);

        jMenuItem10.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem10.setIcon(resourceMap.getIcon("jMenuItem10.icon")); // NOI18N
        jMenuItem10.setText(resourceMap.getString("jMenuItem10.text")); // NOI18N
        jMenuItem10.setName("jMenuItem10"); // NOI18N
        jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem10ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem10);

        jSeparator8.setName("jSeparator8"); // NOI18N
        jMenu1.add(jSeparator8);

        jMenuItem11.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem11.setIcon(resourceMap.getIcon("jMenuItem11.icon")); // NOI18N
        jMenuItem11.setText(resourceMap.getString("jMenuItem11.text")); // NOI18N
        jMenuItem11.setName("jMenuItem11"); // NOI18N
        jMenuItem11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem11ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem11);

        jMenuItem12.setIcon(resourceMap.getIcon("jMenuItem12.icon")); // NOI18N
        jMenuItem12.setText(resourceMap.getString("jMenuItem12.text")); // NOI18N
        jMenuItem12.setName("jMenuItem12"); // NOI18N
        jMenuItem12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem12ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem12);

        jMenuItem13.setIcon(resourceMap.getIcon("jMenuItem13.icon")); // NOI18N
        jMenuItem13.setText(resourceMap.getString("jMenuItem13.text")); // NOI18N
        jMenuItem13.setName("jMenuItem13"); // NOI18N
        jMenuItem13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem13ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem13);

        menuBar.add(jMenu1);

        jMenu2.setText(resourceMap.getString("jMenu2.text")); // NOI18N
        jMenu2.setName("jMenu2"); // NOI18N

        jMenuItem21.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.ALT_MASK));
        jMenuItem21.setIcon(resourceMap.getIcon("jMenuItem21.icon")); // NOI18N
        jMenuItem21.setText(resourceMap.getString("jMenuItem21.text")); // NOI18N
        jMenuItem21.setName("jMenuItem21"); // NOI18N
        jMenuItem21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem21ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem21);

        jMenuItem22.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.ALT_MASK));
        jMenuItem22.setIcon(resourceMap.getIcon("jMenuItem22.icon")); // NOI18N
        jMenuItem22.setText(resourceMap.getString("jMenuItem22.text")); // NOI18N
        jMenuItem22.setName("jMenuItem22"); // NOI18N
        jMenuItem22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem22ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem22);

        menuBar.add(jMenu2);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setIcon(resourceMap.getIcon("aboutMenuItem.icon")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 679, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 497, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        jMenuItem14.setIcon(resourceMap.getIcon("jMenuItem14.icon")); // NOI18N
        jMenuItem14.setText(resourceMap.getString("jMenuItem14.text")); // NOI18N
        jMenuItem14.setName("jMenuItem14"); // NOI18N
        jMenuItem14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem14ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem14);

        jMenuItem15.setIcon(resourceMap.getIcon("jMenuItem15.icon")); // NOI18N
        jMenuItem15.setText(resourceMap.getString("jMenuItem15.text")); // NOI18N
        jMenuItem15.setName("jMenuItem15"); // NOI18N
        jMenuItem15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem15ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem15);

        jSeparator9.setName("jSeparator9"); // NOI18N
        jPopupMenu1.add(jSeparator9);

        jMenuItem16.setIcon(resourceMap.getIcon("jMenuItem16.icon")); // NOI18N
        jMenuItem16.setText(resourceMap.getString("jMenuItem16.text")); // NOI18N
        jMenuItem16.setName("jMenuItem16"); // NOI18N
        jMenuItem16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem16ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem16);

        jMenuItem17.setIcon(resourceMap.getIcon("jMenuItem17.icon")); // NOI18N
        jMenuItem17.setText(resourceMap.getString("jMenuItem17.text")); // NOI18N
        jMenuItem17.setName("jMenuItem17"); // NOI18N
        jMenuItem17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem17ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem17);

        jMenuItem18.setIcon(resourceMap.getIcon("jMenuItem18.icon")); // NOI18N
        jMenuItem18.setText(resourceMap.getString("jMenuItem18.text")); // NOI18N
        jMenuItem18.setName("jMenuItem18"); // NOI18N
        jMenuItem18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem18ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem18);

        jSeparator10.setName("jSeparator10"); // NOI18N
        jPopupMenu1.add(jSeparator10);

        jMenuItem19.setIcon(resourceMap.getIcon("jMenuItem19.icon")); // NOI18N
        jMenuItem19.setText(resourceMap.getString("jMenuItem19.text")); // NOI18N
        jMenuItem19.setName("jMenuItem19"); // NOI18N
        jMenuItem19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem19ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem19);

        jMenuItem20.setIcon(resourceMap.getIcon("jMenuItem20.icon")); // NOI18N
        jMenuItem20.setText(resourceMap.getString("jMenuItem20.text")); // NOI18N
        jMenuItem20.setName("jMenuItem20"); // NOI18N
        jMenuItem20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem20ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem20);

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        if (jButton1.isEnabled()) {
            this.createNew();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        if (jButton2.isEnabled()) {
            this.open();
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed

        if (jButton3.isEnabled()) {
            this.save();
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        if (jButton4.isEnabled()) {
            this.print();
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed

        if (jButton5.isEnabled()) {
            this.undo();
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed

        if (jButton6.isEnabled()) {
            this.redo();
        }
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed

        if (jButton7.isEnabled()) {
            this.cut();
        }
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed

        if (jButton8.isEnabled()) {
            this.copy();
        }
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed

        if (jButton9.isEnabled()) {
            this.paste();
        }
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed

        if (jMenuItem1.isEnabled()) {
            createNew();
        }
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed

        if (jMenuItem2.isEnabled()) {
            open();
        }
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed

        if (jMenuItem3.isEnabled()) {
            save();
        }
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed

        if (jMenuItem4.isEnabled()) {
            saveAs();
        }
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed

        if (jMenuItem6.isEnabled()) {
            undo();
        }
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed

        if (jMenuItem7.isEnabled()) {
            redo();
        }
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed

        if (jMenuItem8.isEnabled()) {
            cut();
        }
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem9ActionPerformed

        if (jMenuItem9.isEnabled()) {
            copy();
        }
    }//GEN-LAST:event_jMenuItem9ActionPerformed

    private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem10ActionPerformed

        if (jMenuItem10.isEnabled()) {
            paste();
        }
    }//GEN-LAST:event_jMenuItem10ActionPerformed

    private void jMenuItem11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem11ActionPerformed

        if (jMenuItem11.isEnabled()) {
            selectAll();
        }
    }//GEN-LAST:event_jMenuItem11ActionPerformed

    private void jMenuItem12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem12ActionPerformed

        if (jMenuItem12.isEnabled()) {
            deleteSelection();
        }
    }//GEN-LAST:event_jMenuItem12ActionPerformed

    private void jMenuItem13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem13ActionPerformed

        if (jMenuItem13.isEnabled()) {
            selectAll();
            deleteSelection();
        }
    }//GEN-LAST:event_jMenuItem13ActionPerformed

    private void jMenuItem14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem14ActionPerformed

        if (jMenuItem14.isEnabled()) {
            undo();
        }
    }//GEN-LAST:event_jMenuItem14ActionPerformed

    private void jMenuItem15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem15ActionPerformed

        if (jMenuItem15.isEnabled()) {
            redo();
        }
    }//GEN-LAST:event_jMenuItem15ActionPerformed

    private void jMenuItem16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem16ActionPerformed

        if (jMenuItem16.isEnabled()) {
            cut();
        }
    }//GEN-LAST:event_jMenuItem16ActionPerformed

    private void jMenuItem17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem17ActionPerformed

        if (jMenuItem17.isEnabled()) {
            copy();
        }
    }//GEN-LAST:event_jMenuItem17ActionPerformed

    private void jMenuItem18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem18ActionPerformed

        if (jMenuItem18.isEnabled()) {
            paste();
        }
    }//GEN-LAST:event_jMenuItem18ActionPerformed

    private void jMenuItem19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem19ActionPerformed

        if (jMenuItem19.isEnabled()) {
            selectAll();
        }
    }//GEN-LAST:event_jMenuItem19ActionPerformed

    private void jMenuItem20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem20ActionPerformed

        if (jMenuItem20.isEnabled()) {
            deleteSelection();
        }
    }//GEN-LAST:event_jMenuItem20ActionPerformed

    private void jMenuItem21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem21ActionPerformed

        if (jMenuItem21.isEnabled()) {
            compile();
        }
    }//GEN-LAST:event_jMenuItem21ActionPerformed

    private void jList1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseClicked

        if ((evt.getClickCount() == 2) && (evt.getButton() == MouseEvent.BUTTON1)) {
            hilightError(jList1.getSelectedIndex());
        }

    }//GEN-LAST:event_jList1MouseClicked

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed

        if (jButton11.isEnabled()) {
            compile();
        }
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed

        if (jMenuItem5.isEnabled()) {
            this.print();
        }
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        this.run();
    }//GEN-LAST:event_jButton12ActionPerformed

    private void jMenuItem22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem22ActionPerformed
        this.run();
    }//GEN-LAST:event_jMenuItem22ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JList jList1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem12;
    private javax.swing.JMenuItem jMenuItem13;
    private javax.swing.JMenuItem jMenuItem14;
    private javax.swing.JMenuItem jMenuItem15;
    private javax.swing.JMenuItem jMenuItem16;
    private javax.swing.JMenuItem jMenuItem17;
    private javax.swing.JMenuItem jMenuItem18;
    private javax.swing.JMenuItem jMenuItem19;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem20;
    private javax.swing.JMenuItem jMenuItem21;
    private javax.swing.JMenuItem jMenuItem22;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JToolBar.Separator jSeparator11;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
}

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
/*
 * EditorView.java
 *
 * Created on 21 Δεκέμβριος 2008, 7:50 μμ
 */
package minijed.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Vector;
import javax.swing.JEditorPane;
import javax.swing.JPopupMenu;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.EditorKit;
import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.SyntaxStyles;
import jsyntaxpane.TokenType;
import jsyntaxpane.lexers.MiniJavaLexer;

/**
 *
 * @author  cyberpython
 */
public class EditorView extends javax.swing.JPanel implements DocumentListener, CaretListener, UndoableEditListener {

    private boolean newFile;
    private boolean modified;
    private String Title;
    private File storage;
    private SyntaxDocument document;
    private EditorViewContainer container;
    private String UNTITLED;
    private JPopupMenu popupMenu;

    /** Creates new form EditorView */
    public EditorView() {
        preInit(null, 0, null);
        initComponents();
        postInit();
    }

    /** Creates new form EditorView */
    public EditorView(EditorViewContainer container) {
        preInit(null, 0, container);
        initComponents();        
        postInit();        
    }

    public EditorView(int documentCount, EditorViewContainer container) {
        preInit(null, documentCount, container);
        initComponents();
        postInit();
    }

    public EditorView(File input, EditorViewContainer container) {
        preInit(input, -1, container);
        initComponents();
        postInit(input);
    }

    private void preInit(File input, int documentCount, EditorViewContainer container) {

        UNTITLED = "Untitled";

        this.container = container;

        this.storage = input;
        if (storage == null) {
            newFile = true;
            Title = UNTITLED + "-" + String.valueOf(documentCount);
        } else {
            newFile = false;
            Title = storage.getName();
        }
        modified = false;
    }

    private void postInit() {
        if (container != null) {
            container.notifyDocumentModified(getTitleWithModificationIndicator(), modified);
        }
    }

    private void postInit(File f) {
        this.storage = f;
        this.Title = storage.getName();
        this.newFile = false;


        if (container != null) {
            container.notifyDocumentModified(getTitleWithModificationIndicator(), modified);
        }
    }

    public void initEditor(JPopupMenu popupmenu, Font f, String[] colors) {
        //jEditorPane1.setTransferHandler(null);
        this.popupMenu = popupmenu;

        jsyntaxpane.DefaultSyntaxKit.initKit();
        jEditorPane1.setContentType("text/minijava");
        jEditorPane1.setFont(f);        

        if (this.isNewFile()) {
            createEmptyFile();
        } else {
            loadFile(this.storage);
        }

        this.setEditorColors(colors);

        jEditorPane1.getDocument().addDocumentListener(this);
        jEditorPane1.addCaretListener(this);
        jEditorPane1.getDocument().addUndoableEditListener(this);
        
        container.notifyCaretChanged(null);    
        
        jEditorPane1.invalidate();

    }

    public void insertText(String text) {
        this.jEditorPane1.replaceSelection(text);
    }

    public Font getEditorFont() {        
        return jEditorPane1.getFont();
    }

    public JEditorPane getEditorPane(){
        return this.jEditorPane1;
    }

    public SyntaxDocument getDocument(){
        return this.document;
    }

    public void requestFocusOnEditor(){
        this.jEditorPane1.requestFocus();
    }


    public void clearHighlights(){
        this.jEditorPane1.getHighlighter().removeAllHighlights();
    }

    public void highlightCurrentLine(Color highlightColor){

        int position = jEditorPane1.getCaretPosition();

        int lineStart = document.getLineStartOffset(position);
        int lineEnd = document.getLineEndOffset(position);

        DefaultHighlightPainter redPainter = new DefaultHighlighter.DefaultHighlightPainter( highlightColor );

		try
		{            
			this.jEditorPane1.getHighlighter().addHighlight( lineStart, lineEnd, redPainter );
		}
		catch(BadLocationException ble) {
        }
    }

    public Object highlight(int start, int end, Color highlightColor){

        DefaultHighlightPainter redPainter = new DefaultHighlighter.DefaultHighlightPainter( highlightColor );

		try
		{            
			return this.jEditorPane1.getHighlighter().addHighlight( start, end, redPainter );
		}
		catch(BadLocationException ble) {
            return null;
        }
    }
    
    public  void print(){
       ((JMiniJavaEditorPane)jEditorPane1).printContents();
        
    }
    
    public Color getKeywordColor(){
    
        try{            
            return SyntaxStyles.getInstance().getStyle(TokenType.KEYWORD).getColor();
        }
        catch(Exception e){
           return Color.BLACK;
        }
        
        
    }



    public Vector<Font> getFixedWidthFonts(){
        
        Vector<Font> result = new Vector<Font>();

        int size = 14;
        int wWidth = -1;
        int iWidth = -1;        

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        BufferedImage tmp  = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics g = tmp.getGraphics();        
        
        String[] fontNames = ge.getAvailableFontFamilyNames();
        Font f =null;
        FontMetrics fm = null;
        
        for(int i=0; i<fontNames.length; i++){

             f = new Font(fontNames[i], Font.PLAIN, size);
             fm = g.getFontMetrics(f);
             wWidth = fm.charWidth('W');
             iWidth = fm.charWidth('i');            

             if((wWidth!=0) && (wWidth == iWidth) ){
                result.add(f);
             }
        }

        return result;
    }



    public Color[] getEditorColors(){

        Color[] colors = new Color[8];

        SyntaxStyles styles = SyntaxStyles.getInstance();
        colors[0] = styles.getStyle(TokenType.KEYWORD).getColor();
        colors[1] = styles.getStyle(TokenType.NUMBER).getColor();
        colors[2] = styles.getStyle(TokenType.STRING).getColor();
        colors[3] = styles.getStyle(TokenType.OPERATOR).getColor();
        colors[4] = styles.getStyle(TokenType.COMMENT).getColor();
        colors[5] = styles.getStyle(TokenType.TYPE).getColor();
        colors[6] = styles.getStyle(TokenType.IDENTIFIER).getColor();
        colors[7] = styles.getStyle(TokenType.DEFAULT).getColor();

        return colors;

    }

    public String[] getEditorColorsAsStrings(){

        String[] colors = new String[8];

        SyntaxStyles styles = SyntaxStyles.getInstance();
        colors[0] = styles.getStyle(TokenType.KEYWORD).getColorString();
        colors[1] = styles.getStyle(TokenType.NUMBER).getColorString();
        colors[2] = styles.getStyle(TokenType.STRING).getColorString();
        colors[3] = styles.getStyle(TokenType.OPERATOR).getColorString();
        colors[4] = styles.getStyle(TokenType.COMMENT).getColorString();
        colors[5] = styles.getStyle(TokenType.TYPE).getColorString();
        colors[6] = styles.getStyle(TokenType.IDENTIFIER).getColorString();
        colors[7] = styles.getStyle(TokenType.DEFAULT).getColorString();

        return colors;

    }


    public void setEditorColors(Color[] colors){
        if(colors.length>=8){
            SyntaxStyles styles = SyntaxStyles.getInstance();
            styles.getStyle(TokenType.KEYWORD).setColorString(colorToHex(colors[0]));
            styles.getStyle(TokenType.NUMBER).setColorString(colorToHex(colors[1]));
            styles.getStyle(TokenType.STRING).setColorString(colorToHex(colors[2]));
            styles.getStyle(TokenType.OPERATOR).setColorString(colorToHex(colors[3]));
            styles.getStyle(TokenType.COMMENT).setColorString(colorToHex(colors[4]));
            styles.getStyle(TokenType.TYPE).setColorString(colorToHex(colors[5]));
            styles.getStyle(TokenType.IDENTIFIER).setColorString(colorToHex(colors[6]));
            styles.getStyle(TokenType.DEFAULT).setColorString(colorToHex(colors[7]));
        }

    }

    public void setEditorColors(String[] colors){
        if(colors.length>=8){
            SyntaxStyles styles = SyntaxStyles.getInstance();
            styles.getStyle(TokenType.KEYWORD).setColorString(colors[0]);
            styles.getStyle(TokenType.NUMBER).setColorString(colors[1]);
            styles.getStyle(TokenType.STRING).setColorString(colors[2]);
            styles.getStyle(TokenType.OPERATOR).setColorString(colors[3]);
            styles.getStyle(TokenType.COMMENT).setColorString(colors[4]);
            styles.getStyle(TokenType.TYPE).setColorString(colors[5]);
            styles.getStyle(TokenType.IDENTIFIER).setColorString(colors[6]);
            styles.getStyle(TokenType.DEFAULT).setColorString(colors[7]);
        }

    }

    private String colorToHex(Color c){
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        String R = Integer.toHexString(r);
        String G = Integer.toHexString(g);
        String B = Integer.toHexString(b);
        if(R.length()==1){R = "0"+R;}
        if(G.length()==1){G = "0"+G;}
        if(B.length()==1){B = "0"+B;}
        return "0x"+R+G+B;
    }

    public void setEditorFont(Font  font){
            jEditorPane1.setFont(font);            
    }
   

    public boolean getModified() {
        return this.modified;
    }

    public boolean isModified() {
        return this.modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
        if (container != null) {
            container.notifyDocumentModified(getTitleWithModificationIndicator(), modified);
        }
    }

    public boolean isNewFile() {
        return this.newFile;
    }

    public File getFile() {
        return this.storage;
    }

    public String getTitle() {
        return this.Title;
    }

    public String getTitleWithModificationIndicator() {
        String s = new String(this.Title);
        if (this.modified) {
            s = s + "*";
        }
        return s;
    }

    public String getSelectedText() {
        return this.jEditorPane1.getSelectedText();
    }
    
    public Point getCaretPosition(){
        int absoluteOffset = jEditorPane1.getCaretPosition();
        int y = document.getLineNumberAt(absoluteOffset);
        int x = absoluteOffset - document.getLineStartOffset(absoluteOffset);
        return new Point(x, y);
    }
    
    public int getSelectionStart(){
        return jEditorPane1.getSelectionStart();
    }

    public void reset(int documentCount) {
        this.jEditorPane1.setText("");
        this.storage = null;
        this.Title = UNTITLED + "-" + documentCount;
        this.newFile = true;
        setModified(false);
    }
    
    
    

    private void createEmptyFile() {
        this.document = new SyntaxDocument(new MiniJavaLexer());
        jEditorPane1.setDocument(this.document);
        jEditorPane1.setCaretPosition(0);

        setModified(false);
        this.document.clearUndos();
    }

    private boolean loadFile(File f) {
        try {
            this.document = new SyntaxDocument(new MiniJavaLexer());
            jEditorPane1.setDocument(this.document);
            EditorKit kit = jEditorPane1.getEditorKit();
            kit.read(new InputStreamReader(new FileInputStream(f), "UTF-8"), this.document, 0);
            jEditorPane1.setCaretPosition(0);

            setModified(false);
            this.document.clearUndos();

            return true;
        } catch (FileNotFoundException fnfe) {
            return false;
        } catch (IOException ioe) {
            return false;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return false;

        }
    }

    
    public boolean saveFile(File f) {

        try {
            OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
            BufferedWriter bw = new BufferedWriter(w);
            bw.write(jEditorPane1.getText());

            bw.flush();
            bw.close();
            w.close();


            this.storage = f;
            this.newFile = false;
            this.Title = storage.getName();
            setModified(false);

            return true;
        } catch (IOException ioe) {
            return false;
        }

    }

    public void undo() {
        this.document.doUndo();
    }

    public void redo() {
        this.document.doRedo();
    }

    public boolean canUndo() {
        return this.document.canUndo();
    }

    public boolean canRedo() {
        return this.document.canRedo();
    }

    public void cut() {
        this.jEditorPane1.cut();
    }

    public void copy() {
        this.jEditorPane1.copy();
    }

    public void paste() {
        this.jEditorPane1.paste();
    }

    public void deleteSelection() {
        this.jEditorPane1.replaceSelection("");
    }

    public void selectAll() {
        this.jEditorPane1.selectAll();
    }
    // <editor-fold defaultstate="expanded" desc="DocumentListener implementation">
    /* IMPLEMENTATION OF THE DOCUMENTLISTENER INTERFACE : */
    public void insertUpdate(DocumentEvent e) {
        setModified(true);
    }

    public void removeUpdate(DocumentEvent e) {
        setModified(true);
    }

    public void changedUpdate(DocumentEvent e) {
        setModified(true);
    }

    /* ----------------------------------------------------- */
    // </editor-fold>

    // <editor-fold defaultstate="expanded" desc="CaretListener implementation">
    public void caretUpdate(CaretEvent e) {
        this.container.notifyCaretChanged(e);
    }

    /* ----------------------------------------------------- */
    // </editor-fold>
    
    
    // <editor-fold defaultstate="expanded" desc="UndoableEditListener implementation">
    public void undoableEditHappened(UndoableEditEvent evt) {
        if (evt.getEdit().isSignificant()) {
                    if(!canUndo()){                        
                        container.notifyFirstUndoableEditHappened(evt);
                    }
                }
    }
     /* ----------------------------------------------------- */
    // </editor-fold>
    
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")     
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane1 = new JMiniJavaEditorPane();

        setName("Form"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(minijed.MiniJedApp.class).getContext().getResourceMap(EditorView.class);
        jEditorPane1.setBackground(resourceMap.getColor("jEditorPane1.background")); // NOI18N
        jEditorPane1.setName("jEditorPane1"); // NOI18N
        jEditorPane1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jEditorPane1MouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jEditorPane1MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jEditorPane1MouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(jEditorPane1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

private void jEditorPane1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jEditorPane1MouseClicked

}//GEN-LAST:event_jEditorPane1MouseClicked

private void jEditorPane1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jEditorPane1MousePressed

    if (evt.isPopupTrigger()) {
        this.popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
    }
}//GEN-LAST:event_jEditorPane1MousePressed

private void jEditorPane1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jEditorPane1MouseReleased

    if (evt.isPopupTrigger()) {
        this.popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
    }
}//GEN-LAST:event_jEditorPane1MouseReleased
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}

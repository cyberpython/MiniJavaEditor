/*
 *  The MIT License
 *
 *  Copyright 2010 Georgios Migdos <cyberpython@gmail.com>.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package minijed;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

/**
 *
 * @author Georgios Migdos <cyberpython@gmail.com>
 */
public class AppRunner extends Thread {

    private JEditorPane output;
    private String cmd;
    private File dir;
    private boolean isActive;

    public AppRunner(JEditorPane output) {
        this.output = output;
        this.cmd = null;
        this.dir = null;
        this.isActive = false;
    }

    public void setParams(String cmd, File dir) {
        this.cmd = cmd;
        this.dir = dir;
    }

    public boolean isIdle() {
        return !this.isActive;
    }

    @Override
    public void run() {

        if (this.cmd != null) {

            this.isActive = true;

            output.setContentType("text/html");
            output.setText("<html>   <head>    </head>   <body style=\"background-color: rgb(255,255,255);\">     <p style=\"margin-top: 0px; margin-left: 10px;\">   </p>   </body> </html>");

            HTMLDocument d = (HTMLDocument) output.getDocument();
            Element mainParagraph = d.getElement(d.getDefaultRootElement(), StyleConstants.NameAttribute, HTML.Tag.P);

            String txt = "";

            try {

                Process p = Runtime.getRuntime().exec(cmd, null, dir);
                String line = null;
                String errorLine = null;
                BufferedReader input =
                        new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader errors =
                        new BufferedReader(new InputStreamReader(p.getErrorStream()));
                while (((line = input.readLine()) != null) || ((errorLine = errors.readLine()) != null)) {
                    if (line != null) {
                        txt = "<span style=\"font-weight: bold;\">" + line + "</span><br>\n";
                        d.insertBeforeEnd(mainParagraph, txt);
                    }
                    if (errorLine != null) {
                        txt = "<span style=\"font-weight: bold; color: rgb(155,0,0);\">" + errorLine + "</span><br>\n";
                        d.insertBeforeEnd(mainParagraph, txt);
                    }
                }
                input.close();

                txt = "\n<br><span style=\"color: rgb(0,155,0);\">----- Execution Finished -----</span><br>\n";
                d.insertBeforeEnd(mainParagraph, txt);

            } catch (BadLocationException ble) {
                ble.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                txt = "\n<br><span style=\"color: rgb(155,0,0);\">----- Execution Terminated Due To I/O Exception -----</span><br>\n";
                try {
                    d.insertBeforeEnd(mainParagraph, txt);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            this.isActive = false;
        }

    }
}

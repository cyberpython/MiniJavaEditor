package jsyntaxpane.syntaxkits;

import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.Lexer;
import jsyntaxpane.lexers.MiniJavaLexer;

/**
 *
 * @author Georgios Migdos
 */
public class MiniJavaSyntaxKit extends DefaultSyntaxKit {

    public MiniJavaSyntaxKit() {
        super(new MiniJavaLexer());
    }

    MiniJavaSyntaxKit(Lexer lexer) {
        super(lexer);
    }
}

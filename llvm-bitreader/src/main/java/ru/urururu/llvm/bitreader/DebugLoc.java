package ru.urururu.llvm.bitreader;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class DebugLoc {
    private final int line;
    private final int col;
    private final DISubprogram scope;
    private final Object ia;

    DebugLoc(int line, int col, DISubprogram scope, Object ia) {
        System.out.println("line = [" + line + "], col = [" + col + "], scope = [" + scope + "], ia = [" + ia + "]");
        this.line = line;
        this.col = col;
        this.scope = scope;
        this.ia = ia;
    }

    public int getLine() {
        return line;
    }

    public int getCol() {
        return col;
    }

    public DISubprogram getScope() {
        return scope;
    }

    public Object getIa() {
        return ia;
    }
}

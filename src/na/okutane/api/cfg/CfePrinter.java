package na.okutane.api.cfg;

import na.okutane.api.Cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
public class CfePrinter {
    public static String print(Cfe cfe) {
        final StringBuilder sb = new StringBuilder();

        cfe.accept(new CfeVisitor() {
            @Override
            public void visit(UnprocessedElement element) {
                sb.append("noop: " + element.getMessage());
            }
        });

        SourceRange sourceRange = cfe.getSourceRange();
        if (sourceRange == null) {
            return sb.toString() + "\n - no source -";
        }
        return sb.toString() + '\n' + sourceRange.toString();
    }

    public static String print(Cfg cfg) {
        StringBuilder sb = new StringBuilder();
        Cfe cfe = cfg.getEntry();

        while (cfe != null) {
            if (sb.length() != 0) {
                sb.append(System.getProperty("line.separator"));
            }
            sb.append(print(cfe));
            cfe = cfe.getNext();
        }

        return sb.toString();
    }
}

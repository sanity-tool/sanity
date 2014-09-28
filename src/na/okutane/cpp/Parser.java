package na.okutane.cpp;

import na.okutane.api.Cfg;
import na.okutane.cpp.llvm.Module;
import na.okutane.cpp.llvm.bitreader;

import java.io.File;

/**
 * @author <a href="mailto:dmitriy.matveev@corp.mail.ru">Dmitriy Matveev</a>
 */
public class Parser {
    static {
        System.loadLibrary("irreader");
    }

    public Cfg[] parse(String filename) {
        try {

            ProcessBuilder pb = new ProcessBuilder();
            pb.directory(new File("/Users/jondoe/IdeaProjects/SA/sanity/tests"));
            //pb.command("clang", filename, "-c", "-S", "-emit-llvm", "-gline-tables-only");

            pb.command("clang", filename, "-c", "-emit-llvm", "-gline-tables-only", "-o", "result.bc");

            pb.inheritIO();

            Process process = pb.start();

            int result = process.waitFor();

            if (result == 0) {
                Module m = bitreader.parse("/Users/jondoe/IdeaProjects/SA/sanity/tests/result.bc");
                m.dump();

                m.delete();
                return null;
            } else {
                System.out.println("result: " + result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

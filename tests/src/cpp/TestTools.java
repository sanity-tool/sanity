package cpp;

import na.okutane.cpp.ClangParametersFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.matveev@corp.mail.ru">Dmitriy Matveev</a>
 */
@Configuration
public class TestTools {
    String llvmAssembler = System.getProperty("sanity.llvm-as", "llvm-as");

    @Bean
    ClangParametersFactory createClangParametersFactory() {
        return (filename, objFile) -> {
            List<String> parameters = new ArrayList<>();

            if (filename.endsWith(".swift")) {
                parameters.add("swiftc");

                parameters.add("-emit-bc");
                parameters.add("-g");
                parameters.add("-o");
                parameters.add(objFile);

                parameters.add(filename);
            } else if (filename.endsWith(".ll")) {
                parameters.add(llvmAssembler);

                parameters.add("-o=" + objFile);
                parameters.add(filename);
            } else {
                parameters.add("clang");

                if (filename.endsWith("hello.m")) {
                    parameters.add("-framework");
                    parameters.add("Foundation");
                }

                parameters.addAll(Arrays.asList(filename, "-c", "-emit-llvm", "-femit-all-decls", "-g", "-o", objFile));
            }

            return parameters.toArray(new String[parameters.size()]);
        };
    }
}

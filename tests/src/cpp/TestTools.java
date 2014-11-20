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
    @Bean
    ClangParametersFactory createClangParametersFactory() {
        return (filename, objFile) -> {
            List<String> parameters = new ArrayList<>();

            parameters.add("clang");

            if (filename.endsWith("hello.m")) {
                parameters.add("-framework");
                parameters.add("Foundation");
            }

            parameters.addAll(Arrays.asList(filename, "-c", "-emit-llvm", "-femit-all-decls", "-g", "-o", objFile));

            return parameters.toArray(new String[parameters.size()]);
        };
    }
}

package ru.urururu.sanity.cpp;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.urururu.sanity.cpp.tools.Tool;
import ru.urururu.sanity.cpp.tools.ToolFactory;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Configuration
public class TestTools {
    @Autowired
    private ToolFactory tools;

    @Bean
    ClangParametersFactory createClangParametersFactory() {
        return (filename, objFile) -> {
            Tool tool = tools.get(FilenameUtils.getExtension(filename));
            return tool.createParameters(filename, objFile);
        };
    }
}

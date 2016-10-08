package ru.urururu.sanity.cpp.tools;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class ToolFactory {
    private final Set<Tool> tools = new HashSet<>();
    private final Map<String, Tool> byExtensions = new HashMap<>();
    private final Map<Language, Tool> byLanguages = new EnumMap<>(Language.class);

    public ToolFactory() throws InterruptedException {
        Tool.tryCreate(System.getProperty("sanity.clang", "clang"), Clang::new).ifPresent(this::addTool);
        Tool.tryCreate(System.getProperty("sanity.swiftc", "swiftc"), Swift::new).ifPresent(this::addTool);

        Tool.tryCreate(System.getProperty("sanity.llvm-as", "llvm-as"), LlvmAs::new).ifPresent(this::addTool);
    }

    private void addTool(Tool tool) {
        if (!tools.add(tool)) {
           throw new IllegalStateException("Duplicate tool: " + tool);
        }

        for (Language language : tool.getLanguages()) {
            byLanguages.merge(language, tool, (t1, t2) -> {
                throw new IllegalStateException("More that one tool for " + language + ": " + t1 + " and " + t2);
            });

            for (String extension : language.getExtensions()) {
                byExtensions.merge(extension, tool, (t1, t2) -> {
                    throw new IllegalStateException("More that one tool for ." + extension + ": " + t1 + " and " + t2);
                });
            }
        }
    }

    public Set<Language> getLanguages() {
        return byLanguages.keySet();
    }

    public Set<Tool> getTools() {
        return Collections.unmodifiableSet(tools);
    }

    public Set<String> getExtensions() {
        return byExtensions.keySet();
    }

    public Tool get(String extension) {
        return byExtensions.get(extension);
    }

    public Tool get(Language language) {
        return byLanguages.get(language);
    }
}

package ru.urururu.sanity.cpp;

import io.swagger.client.model.InstructionDto;
import io.swagger.client.model.ModuleDto;
import io.swagger.client.model.SourceRefDto;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.SourceRangeFactory;
import ru.urururu.sanity.api.cfg.SourceRange;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class RemoteSourceRangeFactory extends SourceRangeFactory<InstructionDto> implements ParserListener {
    private ModuleDto currentModule;

    public SourceRange getSourceRange(InstructionDto instruction) {
        Integer sourceRef = instruction.getSourceRef();

        if (sourceRef != null) {
            SourceRefDto sourceRefDto = currentModule.getSourceRefs().get(sourceRef);
            return getSourceRange(sourceRefDto.getFile().getAbsolutePath(), sourceRefDto.getLine());
        }

        return null;
    }

    @Override
    public void onModuleStarted(ModuleDto module) {
        currentModule = module;
    }

    @Override
    public void onModuleFinished(ModuleDto module) {
        currentModule = null;
    }
}

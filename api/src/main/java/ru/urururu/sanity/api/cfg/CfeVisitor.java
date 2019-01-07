package ru.urururu.sanity.api.cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public interface CfeVisitor {
    void visit(UnprocessedElement element);

    void visit(Allocation allocation);

    void visit(Assignment assignment);

    void visit(Call call);

    void visit(IfCondition ifCondition);

    void visit(Switch switchElement);

    void visit(NoOp noOp);

    void visit(Return returnStatement);
}

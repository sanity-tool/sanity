package com.oracle.truffle.llvm.runtime.types.metadata;

import com.oracle.truffle.llvm.runtime.types.MetadataVisitor;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class MetadataDebugLocation implements MetadataBaseNode {
    private final long line;
    private final long column;
    private final MetadataBaseNode scope;

    public MetadataDebugLocation(long line, long column, MetadataBaseNode scope) {
        this.line = line;
        this.column = column;
        this.scope = scope;
    }

    @Override
    public void accept(MetadataVisitor visitor) {
        visitor.visit(this);
    }

    public long getLine() {
        return line;
    }

    public long getColumn() {
        return column;
    }

    public MetadataBaseNode getScope() {
        return scope;
    }
}

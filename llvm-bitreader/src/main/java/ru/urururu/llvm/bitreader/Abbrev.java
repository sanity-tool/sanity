package ru.urururu.llvm.bitreader;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class Abbrev {
    private final List<RecordReader> readers;

    private Abbrev(List<RecordReader> readers) {
        this.readers = readers;
    }

    public static AbbrevBuilder builder() {
        return new AbbrevBuilder();
    }

    public List<Object> readRecord(BitstreamReader reader) {
        List<Object> fields = new ArrayList<>();

        for (RecordReader recordReader : readers) {
            fields.add(recordReader.read(reader));
        }

        return fields;
    }

    public static class AbbrevBuilder {
        List<RecordReader> readers = new ArrayList<>();

        BuilderState state = new BuilderState() {
            @Override
            public BuilderState onLiteral(int literalValue) {
                readers.add(new RecordReader() {
                    @Override
                    public Object read(BitstreamReader reader) {
                        return literalValue;
                    }

                    @Override
                    public String toString() {
                        return "literal:" + literalValue;
                    }
                });
                return this;
            }

            @Override
            public BuilderState onFixed(int width) {
                if (width == 0 || width > 32) {
                    throw new IllegalArgumentException("width: " + width);
                }
                readers.add(new RecordReader() {
                    @Override
                    public Object read(BitstreamReader reader) {
                        return reader.read(width);
                    }

                    @Override
                    public String toString() {
                        return "fixed" + width + "-reader";
                    }
                });
                return this;
            }

            @Override
            public BuilderState onVbr(int width) {
                if (width == 0 || width > 32) {
                    throw new IllegalArgumentException("width: " + width);
                }
                readers.add(new RecordReader() {
                    @Override
                    public Object read(BitstreamReader reader) {
                        return reader.readVBR(width);
                    }

                    @Override
                    public String toString() {
                        return "vbr" + width + "-reader";
                    }
                });
                return this;
            }

            @Override
            public BuilderState onArray() {
                return new BuilderState() {
                    @Override
                    public BuilderState onFixed(int width) {
                        readers.add(new RecordReader() {
                            @Override
                            public Object read(BitstreamReader reader) {
                                int arrayLength = reader.readVBR(6);

                                List<Integer> integers = new ArrayList<>();

                                for (int i = 0; i < arrayLength; i++) {
                                    int arrayElement = reader.read(width);
                                    integers.add(arrayElement);
                                }

                                return integers;
                            }

                            @Override
                            public String toString() {
                                return "fixed" + width + "-array-reader";
                            }
                        });

                        return BuilderState.FINISHED;
                    }

                    @Override
                    public BuilderState onVbr(int width) {
                        readers.add(new RecordReader() {
                            @Override
                            public Object read(BitstreamReader reader) {
                                int arrayLength = reader.readVBR(6);

                                List<Integer> integers = new ArrayList<>();

                                for (int i = 0; i < arrayLength; i++) {
                                    int arrayElement = reader.readVBR(width);
                                    integers.add(arrayElement);
                                }

                                return integers;
                            }

                            @Override
                            public String toString() {
                                return "vbr" + width + "-array-reader";
                            }
                        });

                        return BuilderState.FINISHED;
                    }

                    @Override
                    public BuilderState onChar6() {
                        readers.add(new RecordReader() {
                            @Override
                            public Object read(BitstreamReader reader) {
                                int arrayLength = reader.readVBR(6);

                                char[] chars = new char[arrayLength];

                                for (int i = 0; i < arrayLength; i++) {
                                    // read char6
                                    int char6code = reader.read(6);
                                    chars[i] = Utils.toChar(char6code);
                                }

                                return new String(chars);
                            }

                            @Override
                            public String toString() {
                                return "char6-array-reader";
                            }
                        });

                        return BuilderState.FINISHED;
                    }

                    @Override
                    public String toString() {
                        return "array";
                    }
                };
            }

            @Override
            public BuilderState onBlob() {
                readers.add(new RecordReader() {
                    @Override
                    public Object read(BitstreamReader reader) {
                        int blobSize = reader.readVBR(6);
                        reader.skipToFourByteBoundary();

                        byte[] blob = new byte[blobSize];

                        for (int i = 0; i < blobSize; i++) {
                            blob[i] = (byte) reader.read(8);
                        }
                        reader.skipToFourByteBoundary();
                        return blob;
                    }
                });
                return FINISHED;
            }

            @Override
            public String toString() {
                return "normal";
            }
        };

        public Abbrev build() {
            return new Abbrev(readers);
        }

        public void onLiteral(int literalValue) {
            state = state.onLiteral(literalValue);
        }

        public void onArray() {
            state = state.onArray();
        }

        public void onChar6() {
            state = state.onChar6();
        }

        public void onVbr(int width) {
            state = state.onVbr(width);
        }

        public void onFixed(int width) {
            state = state.onFixed(width);
        }

        public void onBlob() {
            state = state.onBlob();
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
        }
    }

    private interface BuilderState {
        default BuilderState onLiteral(int literalValue) {
            throw new IllegalStateException();
        }

        default BuilderState onFixed(int width) {
            throw new IllegalStateException();
        }

        default BuilderState onVbr(int width) {
            throw new IllegalStateException();
        }

        default BuilderState onArray() {
            throw new IllegalStateException();
        }

        default BuilderState onChar6() {
            throw new IllegalStateException();
        }

        default BuilderState onBlob() {
            throw new IllegalStateException();
        }

        BuilderState FINISHED = new BuilderState() {
            @Override
            public String toString() {
                return "finished";
            }
        };
    }

    private interface RecordReader {
        Object read(BitstreamReader reader);
    }
}

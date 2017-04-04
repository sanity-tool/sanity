package ru.urururu.llvm.bitreader;

import java.nio.ByteBuffer;

/**
 * Logic for this class is mostly taken as is from \llvm\lib\Bitcode\Reader\BitcodeReader.cpp
 *
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class BitstreamReader {
    private final ByteBuffer buffer;

    private int bitsInCurWord = 0;
    private int curWord;

    public BitstreamReader(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    static final int BITS_IN_WORD = 32;
    static final int MASK = 0x1f;

    public int read(int numBits) {
        if (numBits < 1 || numBits > BITS_IN_WORD) {
            throw new IllegalArgumentException("numBits: " + numBits);
        }

        // If the field is fully contained by curWord, return it quickly.
        if (bitsInCurWord >= numBits) {
            int R = curWord & (~0 >>> (BITS_IN_WORD - numBits));

            // Use a mask to avoid undefined behavior.
            curWord >>>= (numBits & MASK);

            bitsInCurWord -= numBits;
            return R;
        }

        int r = bitsInCurWord > 0 ? curWord : 0;
        int bitsLeft = numBits - bitsInCurWord;

        fillCurWord();

        // If we run out of data, abort.
        if (bitsLeft > bitsInCurWord)
            reportFatalError("Unexpected end of file");

        int r2 = curWord & (~0 >>> (BITS_IN_WORD - bitsLeft));

        // Use a mask to avoid undefined behavior.
        curWord >>>= (bitsLeft & MASK);

        bitsInCurWord -= bitsLeft;

        r |= r2 << (numBits - bitsLeft);

        return r;
    }

    public int readVBR(int numBits) {
        int piece = read(numBits);
        if ((piece & (1 << (numBits - 1))) == 0)
            return piece;

        int result = 0;
        int nextBit = 0;
        while (true) {
            result |= (piece & ((1 << (numBits - 1)) - 1)) << nextBit;

            if ((piece & (1 << (numBits - 1))) == 0)
                return result;

            nextBit += numBits - 1;
            piece = read(numBits);
        }
    }

    @Deprecated
    public int getBitsInCurWord() {
        return bitsInCurWord;
    }

    public void skipToFourByteBoundary() {
        bitsInCurWord = 0;
    }

    private void fillCurWord() {
        curWord = buffer.getInt();

        //System.out.println("buffer.position() = " + buffer.position());

        //System.out.println("curWord = " + curWord);
        bitsInCurWord = 32;
    }

    boolean hasMore() {
        return buffer.position() < buffer.limit() || bitsInCurWord > 0;
    }

    int position() {
        return buffer.position();
    }

    private void reportFatalError(String s) {
        throw new IllegalStateException(s);
    }

    public void position(int position) {
        buffer.position(position);
        bitsInCurWord = 0;
    }
}

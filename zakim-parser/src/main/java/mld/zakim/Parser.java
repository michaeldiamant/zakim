package mld.zakim;

public class Parser {

    private static int mapBit(byte[] a, int i, int bitIndex, char c) {
        int x = ((a[i] ^ c) == 0) ? 1 : 0;
        return (x << bitIndex);
    }

    private static int mapByte(byte[] a, int i, char c) {
        int r1 = mapBit(a, 8 * i, 0, c);
        int r2 = mapBit(a, 8 * i + 1, 1, c);
        int r3 = mapBit(a, 8 * i + 2, 2, c);
        int r4 = mapBit(a, 8 * i + 3, 3, c);
        int r5 = mapBit(a, 8 * i + 4, 4, c);
        int r6 = mapBit(a, 8 * i + 5, 5, c);
        int r7 = mapBit(a, 8 * i + 6, 6, c);
        int r8 = mapBit(a, 8 * i + 7, 7, c);
        return r1 | r2 | r3 | r4 | r5 | r6 | r7 | r8;
    }

    private static final int WORD_SIZE = 8;

    private static byte[] newArray(int length) {
        return new byte[(length + WORD_SIZE - 1) / WORD_SIZE];
    }

    public static int parse(byte[] a) {
        byte[] colons = newArray(a.length);
        byte[] quotes = newArray(a.length);
        byte[] backslashes = newArray(a.length);
        byte[] leftBraces = newArray(a.length);
        byte[] rightBraces = newArray(a.length);

        int r = 0;
        int delta = a.length - colons.length;
        for (int i = 0; i < colons.length - 1 - delta; i++) {
            colons[i] |= mapByte(a, i, ':');
        }
        for (int i = 0; i < colons.length - 1 - delta; i++) {
            quotes[i] |= mapByte(a, i, '\"');
        }
        for (int i = 0; i < colons.length - 1 - delta; i++) {
            backslashes[i] |= mapByte(a, i, '\\');
        }
        for (int i = 0; i < colons.length - 1 - delta; i++) {
            leftBraces[i] |= mapByte(a, i, '{');
        }
        for (int i = 0; i < colons.length - 1 - delta; i++) {
            rightBraces[i] |= mapByte(a, i, '}');
        }

        return r;
    }
}

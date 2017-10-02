package mld.zakim;

public class Parser {
    private static long createMask(char c) {
        long l = 0;
        l |= c << 8;
        l |= c << 16;
        l |= c << 24;
        l |= c << 32;
        l |= c << 40;
        l |= c << 48;
        l |= c << 56;
        l |= c << 64;
        return l;
    }

    private static long COLON = createMask(':');

    private static byte isPositive(int n) {
        return (byte) (((~n & (~n + 1)) >> 31) & 1);
    }

    private static int shift(long map, int s) {
        return 1 - ((int) (map >> s) & 0xFF);
    }

    private static void extractByte(byte[] r, int offset, long[] bb, int j) {
        for (int x = 0; x < 8; x++) {
            bb[j] |= r[offset + x] << ((7 - x) * 8);
        }
    }

    private static long buildStructuralBitmap(long x, long y) {
        long map = x ^ y;
        long b = 0;

        int s1 = shift(map, 56);
        int s2 = shift(map, 48);
        int s3 = shift(map, 40);
        int s4 = shift(map, 32);
        int s5 = shift(map, 24);
        int s6 = shift(map, 16);
        int s7 = shift(map, 8);
        int s8 = shift(map, 0);

        byte x1 = isPositive(s1);
        byte x2 = isPositive(s2);
        byte x3 = isPositive(s3);
        byte x4 = isPositive(s4);
        byte x5 = isPositive(s5);
        byte x6 = isPositive(s6);
        byte x7 = isPositive(s7);
        byte x8 = isPositive(s8);

        b |= x1;
        b |= x2 << 1;
        b |= x3 << 2;
        b |= x4 << 3;
        b |= x5 << 4;
        b |= x6 << 5;
        b |= x7 << 6;
        b |= x8 << 7;
        return b;
    }

    public static long parse(byte[] r) {
        long[] bitmap = new long[r.length / 64];
        long sum = 0;
        long[] bb = new long[8];
        for (int i = 0; i < r.length - 64; i += 64) {
            long b = 0;
            for (int j = 0; j < bb.length; j++) {
                bb[j] = 0;
                extractByte(r, i, bb, j);
            }

            // TODO Apply masks to bb
//            long r1 = buildStructuralBitmap(bb[0], COLON);
//            long r2 = buildStructuralBitmap(bb[1], COLON) << 8;
//            long r3 = buildStructuralBitmap(bb[2], COLON) << 16;
//            long r4 = buildStructuralBitmap(bb[3], COLON) << 24;
//            long r5 = buildStructuralBitmap(bb[4], COLON) << 32;
//            long r6 = buildStructuralBitmap(b6, COLON) << 40;
//            long r7 = buildStructuralBitmap(b7, COLON) << 48;
//            long r8 = buildStructuralBitmap(b8, COLON) << 56;

//            b = r1 | r2 | r3; //| r2 | r3 | r4 | r5 | r6 | r7 | r8;
            for (int ii = 0; ii < 8; ii++) {
                b |= bb[ii];
            }

            sum += b;
        }
        return sum;
    }
}

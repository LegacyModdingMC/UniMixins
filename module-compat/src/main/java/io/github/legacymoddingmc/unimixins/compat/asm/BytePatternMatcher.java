package io.github.legacymoddingmc.unimixins.compat.asm;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Was taken from <a href="https://github.com/GTNewHorizons/RetroFuturaBootstrap/pull/20">RFB</a>
 */
public class BytePatternMatcher {
    final Mode mode;
    // first byte -> matched patterns
    final byte[][][] byFirst = new byte[256][][];
    int minPatternLen = Integer.MAX_VALUE;

    public enum Mode {
        /** Checks if the constant pool entry contains a pattern */
        Contains,
        /** Checks if the whole constant pool entry equals a pattern */
        Equals,
        /** Checks if the constant pool entry starts with a pattern */
        StartsWith
    }

    public BytePatternMatcher(String strPattern, Mode mode) {
        this(new String[] {strPattern}, mode);
    }

    public BytePatternMatcher(String[] strPatterns, Mode mode) {
        this.mode = mode;

        final byte[][] patterns = new byte[strPatterns.length][];
        final int[] bucketSizes = new int[256];
        final int[] bucketIndices = new int[Math.min(strPatterns.length, 256)];
        int bucketsCount = 0;

        for (int i = 0; i < strPatterns.length; i++) {
            final byte[] pattern = strPatterns[i].getBytes(StandardCharsets.UTF_8);
            patterns[i] = pattern;

            if (pattern.length < minPatternLen) {
                minPatternLen = pattern.length;
            }

            final int bucketIndex = pattern[0] & 0xFF;
            if (bucketSizes[bucketIndex]++ == 0) {
                bucketIndices[bucketsCount++] = bucketIndex;
            }
        }

        // Ascending sorting by length
        Arrays.sort(patterns, (a, b) -> Integer.compare(a.length, b.length));

        for (int i = 0; i < bucketsCount; i++) {
            final int bucketIndex = bucketIndices[i];
            byFirst[bucketIndex] = new byte[bucketSizes[bucketIndex]][];
            bucketSizes[bucketIndex] = 0; // reuse as write index
        }

        for (final byte[] pattern : patterns) {
            final int bucketIndex = pattern[0] & 0xFF;
            byFirst[bucketIndex][bucketSizes[bucketIndex]++] = pattern;
        }
    }

    public boolean matches(byte[] bytes, int start, int len) {
        if (len < minPatternLen) {
            return false;
        }

        switch (mode) {
            case Contains:
                return contains(bytes, start, len);
            case Equals:
                return equals(bytes, start, len);
            case StartsWith:
                return startsWith(bytes, start, len);
        }

        return false;
    }

    private boolean contains(byte[] bytes, int start, int len) {
        final int end = start + len;

        for (int pos = start; pos <= end - minPatternLen; pos++) {
            final byte[][] patterns = byFirst[bytes[pos] & 0xFF];
            if (patterns == null) {
                continue;
            }

            for (final byte[] pattern : patterns) {
                if (pattern.length > end - pos) {
                    break;
                }

                int k = pattern.length - 1;
                while (k > 0 && bytes[pos + k] == pattern[k]) k--;
                if (k == 0) return true;
            }
        }

        return false;
    }

    private boolean equals(byte[] bytes, int start, int len) {
        final byte[][] patterns = byFirst[bytes[start] & 0xFF];
        if (patterns == null) {
            return false;
        }

        for (final byte[] pattern : patterns) {
            if (pattern.length < len) {
                continue;
            }
            if (pattern.length > len) {
                break;
            }

            int k = pattern.length - 1;
            while (k > 0 && bytes[start + k] == pattern[k]) k--;
            if (k == 0) return true;
        }

        return false;
    }

    private boolean startsWith(byte[] bytes, int start, int len) {
        final byte[][] patterns = byFirst[bytes[start] & 0xFF];
        if (patterns == null) {
            return false;
        }

        for (final byte[] pattern : patterns) {
            if (pattern.length > len) {
                break;
            }

            int k = pattern.length - 1;
            while (k > 0 && bytes[start + k] == pattern[k]) k--;
            if (k == 0) return true;
        }

        return false;
    }
}

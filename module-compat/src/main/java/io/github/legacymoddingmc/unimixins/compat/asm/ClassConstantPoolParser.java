/***
 * This Class is derived from the ASM ClassReader
 * <p>
 * ASM: a very small and fast Java bytecode manipulation framework Copyright (c) 2000-2011 INRIA, France Telecom All
 * rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or other materials provided with the
 * distribution. 3. Neither the name of the copyright holders nor the names of its contributors may be used to endorse
 * or promote products derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.github.legacymoddingmc.unimixins.compat.asm;

import java.nio.charset.StandardCharsets;

/**
 * Using this class to search for a (single) String reference is > 40 times faster than parsing a class with a
 * ClassReader + ClassNode while using way less RAM
 */
public final class ClassConstantPoolParser {

    enum ConstantTypes {

        INVALID, // 0 unused, if that ever changes shift this to the next unused index
        UTF8,
        // 2 unused
        INT,
        FLOAT,
        LONG,
        DOUBLE,
        CLASS_REF,
        STR_REF,
        FIELD,
        METH_REF,
        IMETH_REF,
        NAME_TYPE,
        // 13,14 unused
        METH_HANDLE,
        METH_TYPE,
        DYNAMIC,
        INVOKE_DYNAMIC,
        MODULE,
        PACKAGE;

        // Indices in this table directly map to the values of these constants
        static final ConstantTypes[] MAP = {      INVALID,
            UTF8,    INVALID,        INT,         FLOAT,
            LONG,    DOUBLE,         CLASS_REF,   STR_REF,
            FIELD,   METH_REF,       IMETH_REF,   NAME_TYPE,
            INVALID, INVALID,        METH_HANDLE, METH_TYPE,
            DYNAMIC, INVOKE_DYNAMIC, MODULE,      PACKAGE };

        static ConstantTypes toType(byte code) {
            ConstantTypes ret = MAP[Byte.toUnsignedInt(code)];
            if (ret == INVALID) throw new RuntimeException("Invalid constant type: " + code);
            return ret;
        }
    }

    private byte[][] BYTES_TO_SEARCH;

    public ClassConstantPoolParser(String... strings) {
        BYTES_TO_SEARCH = new byte[strings.length][];
        for (int i = 0; i < BYTES_TO_SEARCH.length; i++) {
            BYTES_TO_SEARCH[i] = strings[i].getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * Returns true if the constant pool of the class represented by this byte array contains one of the Strings we are
     * looking for
     */
    public boolean find(byte[] basicClass) {
        return find(basicClass, false);
    }

    /**
     * Returns true if the constant pool of the class represented by this byte array contains one of the Strings we are
     * looking for.
     *
     * @param prefixes If true, it is enough for a constant pool entry to <i>start</i> with one of our Strings to count
     *                 as a match - otherwise, the entire String has to match.
     */
    public boolean find(byte[] basicClass, boolean prefixes) {
        if (basicClass == null || basicClass.length == 0) {
            return false;
        }

        // checks the class version
        final int maxSupported = 69; // Java 25
        short major = readShort(6, basicClass);
        if (major > maxSupported || (major == maxSupported && readShort(4, basicClass) > 0)) {
            return false;
        }

        // Loop through each entry in the constant pool, getting the size and content before jumping to the next.
        // Strings get scanned for the searched constants, and if found result in an early exit.
        final int numConstants = readUnsignedShort(8, basicClass);
        int index = 10;
        for (int i = 1; i < numConstants; ++i) {
            int size = -1;

            switch (ConstantTypes.toType(basicClass[index])) {
                case UTF8: {
                    final int strLen = readUnsignedShort(index + 1, basicClass);
                    size = 3 + strLen;

                    for (byte[] bytes : BYTES_TO_SEARCH) {
                        if (prefixes ? strLen < bytes.length : strLen != bytes.length) {
                            continue;
                        }

                        boolean found = true;
                        for (int j = index + 3; j < index + 3 + bytes.length; j++) {
                            if (basicClass[j] != bytes[j - (index + 3)]) {
                                found = false;
                                break;
                            }
                        }

                        if (found) {
                            return true;
                        }
                    }
                    break;
                }

                case INT:
                case FLOAT:
                case FIELD:
                case METH_REF:
                case IMETH_REF:
                case NAME_TYPE:
                case DYNAMIC:
                case INVOKE_DYNAMIC:
                    size = 5;
                    break;

                case LONG:
                case DOUBLE:
                    size = 9;
                    ++i;
                    break;

                case METH_HANDLE:
                    size = 4;
                    break;

                case CLASS_REF:
                case STR_REF:
                case METH_TYPE:
                case MODULE:
                case PACKAGE:
                    size = 3;
                    break;
            }

            if (size < 0) throw new RuntimeException("Error parsing constant pool!");
            index += size;
        }

        return false;
    }

    private static short readShort(final int index, byte[] basicClass) {
        return (short) readUnsignedShort(index, basicClass);
    }

    private static int readUnsignedShort(final int index, byte[] basicClass) {
        return ((basicClass[index] & 0xFF) << 8) | (basicClass[index + 1] & 0xFF);
    }

}

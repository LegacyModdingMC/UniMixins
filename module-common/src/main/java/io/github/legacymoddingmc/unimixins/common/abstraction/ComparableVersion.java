package io.github.legacymoddingmc.unimixins.common.abstraction;

public class ComparableVersion {
    private final IComparableVersionImpl impl;

    public ComparableVersion(String version) {
        if(ComparableVersion.class.getResource("/cpw/mods/fml/common/versioning/ComparableVersion.class") != null) {
            impl = new ComparableVersionOld(version);
        } else {
            impl = new ComparableVersionNew(version);
        }
    }

    public int compareTo(ComparableVersion other) {
        return impl.compareTo(other.impl);
    }

    public interface IComparableVersionImpl {
        int compareTo(IComparableVersionImpl other);
    }

    public static class ComparableVersionOld implements IComparableVersionImpl {
        private final cpw.mods.fml.common.versioning.ComparableVersion internal;

        public ComparableVersionOld(String version) {
            this.internal = new cpw.mods.fml.common.versioning.ComparableVersion(version);
        }

        public int compareTo(IComparableVersionImpl other) {
            if(other instanceof ComparableVersionOld) {
                return internal.compareTo(((ComparableVersionOld) other).internal);
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    public static class ComparableVersionNew implements IComparableVersionImpl {
        private final net.minecraftforge.fml.common.versioning.ComparableVersion internal;

        public ComparableVersionNew(String version) {
            this.internal = new net.minecraftforge.fml.common.versioning.ComparableVersion(version);
        }

        public int compareTo(IComparableVersionImpl other) {
            if(other instanceof ComparableVersionNew) {
                return internal.compareTo(((ComparableVersionNew) other).internal);
            } else {
                throw new IllegalArgumentException();
            }
        }
    }
}

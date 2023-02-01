/**
 * Copyright 2020 TimeConqueror
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, 
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject 
 * to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ru.timeconqueror.spongemixins;

import com.gtnewhorizon.gtnhmixins.GTNHMixins;
import cpw.mods.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This is here for SpongeMixins backwards compat
 * @deprecated use {@link GTNHMixins} instead
 */
@SuppressWarnings("unused")
@Deprecated
@Mod(modid = SpongeMixins.MODID, version = "2.0.1", name = SpongeMixins.NAME, acceptableRemoteVersions = "*")
public class SpongeMixins {
    
    /**
     * @deprecated use {@link GTNHMixins#NAME} instead
     */
    @Deprecated
    public static final String NAME = "SpongeMixins Loader";
    
    /**
     * @deprecated use {@link GTNHMixins#MODID} instead
     */
    @Deprecated
    public static final String MODID = "spongemixins";
    
    /**
     * @deprecated use {@link GTNHMixins#LOGGER} instead
     */
    @Deprecated
    public static final Logger LOGGER = GTNHMixins.LOGGER;

}
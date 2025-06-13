import com.github.jengelman.gradle.plugins.shadow.transformers.Transformer
import com.github.jengelman.gradle.plugins.shadow.transformers.TransformerContext
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import org.apache.tools.ant.filters.ReplaceTokens
import org.apache.tools.zip.ZipEntry
import org.apache.tools.zip.ZipOutputStream
import org.gradle.kotlin.dsl.filter
import java.io.InputStreamReader

plugins {
    id("com.gradleup.shadow") version "8.3.6"
}

dependencies {
    implementation(project(":mixin", "shadowArtifact"))
    implementation(project(":compat", "shadowArtifact"))
    implementation(project(":mixingasm", "shadowArtifact"))
    implementation(project(":spongemixins", "shadowArtifact"))
    implementation(project(":mixinbooterlegacy", "shadowArtifact"))
    implementation(project(":gasstation", "shadowArtifact"))
    implementation(project(":gtnhmixins", "shadowArtifact"))
    implementation(project(":mixinextras", "shadowArtifact"))
}

val moduleList =
    "\\nMixin," +
    "\\nCompat," +
    "\\nMixingasm," +
    "\\nSpongeMixins," +
    "\\nMixinBooterLegacy," +
    "\\nGasStation," +
    "\\nGTNHMixins, and" +
    "\\nMixinExtras."

/**
 * Adapted from https://github.com/LogicFan/shadow-json-transformer.
 * @author Logic Fan
 * @author ah-OOG-ah
 */
class McmodInfoMerger(private val parentModid: String) : Transformer {
    private val GSON = GsonBuilder().setPrettyPrinting().create()
    private val LOGGER = Logging.getLogger(McmodInfoMerger::class.java)
    private var json: JsonElement? = null
    private val targetFile = "mcmod.info"
    // GSON adds the extra quotes when parsing, so we add them to the comparison string
    private val comparisonModid = "\"$parentModid\""

    override fun canTransformResource(element: FileTreeElement?): Boolean {
        return targetFile.equals(element?.relativePath.toString(), ignoreCase = true)
    }

    override fun transform(context: TransformerContext?) {
        val j: JsonElement?
        try {
            val inputStream = context?.`is`
            j = if (inputStream == null)
                null
            else
                JsonParser.parseReader(InputStreamReader(inputStream, "UTF-8"))
        } catch (e: Exception) {
            throw RuntimeException("error on processing json", e)
        }

        val modObj = if (j is JsonArray && !j.isEmpty) j.get(0) as JsonObject
        else throw RuntimeException("Mod objects should be in an array!")

        if (modObj.get("modid").toString() != comparisonModid) {
            modObj.add("parent", JsonPrimitive(parentModid))
        }

        json = if (json == null) j else mergeJson(json, j)
    }

    override fun hasTransformedResource(): Boolean {
        return json != null
    }

    override fun modifyOutputStream(os: ZipOutputStream?, preserveFileTimestamps: Boolean) {
        val entry = ZipEntry(targetFile)
        entry.time = TransformerContext.getEntryTimestamp(preserveFileTimestamps, entry.time)
        os!!.putNextEntry(entry)
        os.write(GSON.toJson(json).toByteArray())

        json = null
    }

    override fun getName(): String {
        return "McmodInfoMerger"
    }

    /**
     * @param lhs a JsonElement
     * @param rhs a JsonElement
     * @param id used for logging purpose only
     * @return the merged JsonElement
     */
    fun mergeJson(lhs: JsonElement?, rhs: JsonElement?, id: String = ""): JsonElement? {
        if (rhs == null || rhs is JsonNull) {
            return lhs
        } else if (lhs == null || lhs is JsonNull) {
            return rhs
        } else if (lhs is JsonArray && rhs is JsonArray) {
            return mergeJsonArray(lhs, rhs)
        } else if (lhs is JsonObject && rhs is JsonObject) {
            return mergeJsonObject(lhs, rhs, id)
        } else if (lhs is JsonPrimitive && rhs is JsonPrimitive) {
            return mergeJsonPrimitive(lhs, rhs, id)
        } else {
            LOGGER.warn("conflicts for property {} detected, {} & {}",
                id, lhs.toString(), rhs.toString())
            return lhs
        }
    }

    fun mergeJsonArray(lhs: JsonArray, rhs: JsonArray): JsonArray {
        val array = JsonArray()
        array.addAll(lhs)
        array.addAll(rhs)
        return array
    }

    fun mergeJsonObject(lhs: JsonObject, rhs: JsonObject, id: String): JsonObject {
        val obj = JsonObject()

        val properties = HashSet<String>()
        properties.addAll(lhs.keySet())
        properties.addAll(rhs.keySet())
        for (property in properties) {
            obj.add(
                property,
                mergeJson(lhs.get(property),
                rhs.get(property),
                "$id:$property"))
        }

        return obj
    }

    fun mergeJsonPrimitive(lhs: JsonPrimitive, rhs: JsonPrimitive, id: String): JsonPrimitive {
        if (lhs != rhs) {
            LOGGER.warn("conflicts for property {} detected, {} & {}", id, lhs.toString(), rhs.toString())
        }

        return lhs
    }
}

tasks.processResources {
    files("mcmod.info") {
        filter<ReplaceTokens>("tokens" to mapOf(
            "moduleList" to moduleList
        ))
    }
}

tasks.shadowJar {
    archiveClassifier = ""

    // Exclude errant license files
    exclude("LICENSE*")

    // Merge mcmod.info files
    transform(McmodInfoMerger("unimixins"))
}

tasks.jar {
    dependsOn(tasks.shadowJar)
    enabled = false
}

unimined.minecraft {

}
package hazem.nurmontage.videoquran.Utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import hazem.nurmontage.videoquran.model.EntityQuranTemplate
import java.lang.reflect.Type

class MItemAdabterJson : JsonSerializer<EntityQuranTemplate>, JsonDeserializer<EntityQuranTemplate> {

    override fun deserialize(
        json: JsonElement, typeOfT: Type, context: JsonDeserializationContext
    ): EntityQuranTemplate? {
        return try {
            val jsonObject = json.asJsonObject
            val className = jsonObject.get("type").asString
            val properties = jsonObject.get("properties")
            context.deserialize(properties, Class.forName("com.hazem.s_din12_24.model.$className"))
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    override fun serialize(
        src: EntityQuranTemplate, typeOfSrc: Type, context: JsonSerializationContext
    ): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.add("type", JsonPrimitive(src::class.simpleName))
        jsonObject.add("properties", context.serialize(src, src::class.java))
        return jsonObject
    }
}

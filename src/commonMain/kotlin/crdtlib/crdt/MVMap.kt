/*
* Copyright © 2020, Concordant and contributors.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
* associated documentation files (the "Software"), to deal in the Software without restriction,
* including without limitation the rights to use, copy, modify, merge, publish, distribute,
* sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all copies or
* substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
* NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package crdtlib.crdt

import crdtlib.utils.Json
import crdtlib.utils.Name
import crdtlib.utils.Timestamp
import crdtlib.utils.UnexpectedTypeException
import crdtlib.utils.VersionVector
import kotlinx.serialization.*
import kotlinx.serialization.json.*

/**
* This class is a delta-based CRDT map implementing last writer wins (MV) to resolve conflicts.
* It is serializable to JSON and respect the following schema:
* {
    "_type": "MVMap",
    "_metadata": {
        "entries": {
            // $key is a string
            (( "$key": [ ( Timestamp.toJson() )*( , Timestamp.toJson() )? ] )*( , "$key": [ ( Timestamp.toJson() )*( , Timestamp.toJson() )? ] ))?
        },
        "causalContext": VersionVector.toJson()
    }
    // $key is a string and $value can be Boolean, double, integer or string
    ( , "$key": [
            (( T.toJson(), )*( T.toJson() ))?
    ] )*
* }
*/
@Serializable
class MVMap : DeltaCRDT<MVMap> {

    /**
    * A mutable map storing metadata relative to each key.
    */
    private val entries: MutableMap<String, MutableSet<Pair<String?, Timestamp>>> = mutableMapOf()

    /**
    * A causal context summarizing executed operations.
    */
    private var causalContext: VersionVector = VersionVector()

    /**
    * Default constructor.
    */
    constructor() {
    }

    /**
    * Constructor initializing the causal context.
    */
    constructor(cc: VersionVector) {
        this.causalContext = cc
    }

    /**
    * Gets the set of Boolean values corresponding to a given key.
    * @param key the key that should be looked for.
    * @return the set of Boolean values associated to the key, or null if the key is not present in
    * the map or last operation is a delete.
    */
    @Name("getBoolean")
    fun getBoolean(key: String): Set<Boolean?>? {
        val setOfValues = this.entries.get(key + MVMap.BOOLEAN)?.map { it.first?.toBoolean() }?.toSet()
        if (setOfValues == mutableSetOf(null)) return null
        return setOfValues
    }

    /**
    * Gets the set of double values corresponding to a given key.
    * @param key the key that should be looked for.
    * @return the set of double values associated to the key, or null if the key is not present in
    * the map or last operation is a delete.
    */
    @Name("getDouble")
    fun getDouble(key: String): Set<Double?>? {
        val setOfValues = this.entries.get(key + MVMap.DOUBLE)?.map { it.first?.toDouble() }?.toSet()
        if (setOfValues == mutableSetOf(null)) return null
        return setOfValues
    }

    /**
    * Gets the set of integer values corresponding to a given key.
    * @param key the key that should be looked for.
    * @return the set of integer values associated to the key, or null if the key is not present in
    * the map or last operation is a delete.
    */
    @Name("getInt")
    fun getInt(key: String): Set<Int?>? {
        val setOfValues = this.entries.get(key + MVMap.INTEGER)?.map { it.first?.toInt() }?.toSet()
        if (setOfValues == mutableSetOf(null)) return null
        return setOfValues
    }

    /**
    * Gets the set of string of values corresponding to a given key.
    * @param key the key that should be looked for.
    * @return the set of string values associated to the key, or null if the key is not present in
    * the map or last operation is a delete.
    */
    @Name("getString")
    fun getString(key: String): Set<String?>? {
        val setOfValues = this.entries.get(key + MVMap.STRING)?.map { it.first }?.toSet()
        if (setOfValues == mutableSetOf(null)) return null
        return setOfValues
    }

    /**
    * Puts a key / Boolean value pair into the map.
    * @param key the key that is targeted.
    * @param value the Boolean value that should be assigned to the key.
    * @param ts the timestamp of this operation.
    * @return the delta corresponding to this operation.
    */
    @Name("setBoolean")
    fun put(key: String, value: Boolean?, ts: Timestamp): MVMap {
        val op = MVMap()
        if (!this.causalContext.contains(ts)) {
            var meta = this.entries.get(key + MVMap.BOOLEAN)
            if (meta == null) meta = mutableSetOf()
            else meta.clear()
            meta.add(Pair(value?.toString(), ts))

            this.entries.put(key + MVMap.BOOLEAN, meta)
            op.entries.put(key + MVMap.BOOLEAN, meta.toMutableSet())
            this.causalContext.update(ts)
            op.causalContext.update(ts)
        }
        return op
    }


    /**
    * Puts a key / double value pair into the map.
    * @param key the key that is targeted.
    * @param value the double value that should be assigned to the key.
    * @param ts the timestamp of this operation.
    * @return the delta corresponding to this operation.
    */
    @Name("setDouble")
    fun put(key: String, value: Double?, ts: Timestamp): MVMap {
        val op = MVMap()
        if (!this.causalContext.contains(ts)) {
            var meta = this.entries.get(key + MVMap.DOUBLE)
            if (meta == null) meta = mutableSetOf()
            else meta.clear()
            meta.add(Pair(value?.toString(), ts))

            this.entries.put(key + MVMap.DOUBLE, meta)
            op.entries.put(key + MVMap.DOUBLE, meta.toMutableSet())
            this.causalContext.update(ts)
            op.causalContext.update(ts)
        }
        return op
    }


    /**
    * Puts a key / integer value pair into the map.
    * @param key the key that is targeted.
    * @param value the integer value that should be assigned to the key.
    * @param ts the timestamp of this operation.
    * @return the delta corresponding to this operation.
    */
    @Name("setInt")
    fun put(key: String, value: Int?, ts: Timestamp): MVMap {
        val op = MVMap()
        if (!this.causalContext.contains(ts)) {
            var meta = this.entries.get(key + MVMap.INTEGER)
            if (meta == null) meta = mutableSetOf()
            else meta.clear()
            meta.add(Pair(value?.toString(), ts))

            this.entries.put(key + MVMap.INTEGER, meta)
            op.entries.put(key + MVMap.INTEGER, meta.toMutableSet())
            this.causalContext.update(ts)
            op.causalContext.update(ts)
        }
        return op
    }


    /**
    * Puts a key / string value pair into the map.
    * @param key the key that is targeted.
    * @param value the string value that should be assigned to the key.
    * @param ts the timestamp of this operation.
    * @return the delta corresponding to this operation.
    */
    @Name("setString")
    fun put(key: String, value: String?, ts: Timestamp): MVMap {
        val op = MVMap()
        if (!this.causalContext.contains(ts)) {
            var meta = this.entries.get(key + MVMap.STRING)
            if (meta == null) meta = mutableSetOf()
            else meta.clear()
            meta.add(Pair(value, ts))

            this.entries.put(key + MVMap.STRING, meta)
            op.entries.put(key + MVMap.STRING, meta.toMutableSet())
            this.causalContext.update(ts)
            op.causalContext.update(ts)
        }
        return op
    }

    /**
    * Deletes a given key / Boolean value pair if it is present in the map and has not yet been
    * deleted.
    * @param key the key that should be deleted.
    * @param ts the timestamp linked to this operation.
    * @return the delta corresponding to this operation.
    */
    @Name("deleteBoolean")
    fun deleteBoolean(key: String, ts: Timestamp): MVMap {
        return put(key, null as Boolean?, ts)
    }

    /**
    * Deletes a given key / double value pair if it is present in the map and has not yet been
    * deleted.
    * @param key the key that should be deleted.
    * @param ts the timestamp linked to this operation.
    * @return the delta corresponding to this operation.
    */
    @Name("deleteDouble")
    fun deleteDouble(key: String, ts: Timestamp): MVMap {
        return put(key, null as Double?, ts)
    }

    /**
    * Deletes a given key / integer value pair if it is present in the map and has not yet been
    * deleted.
    * @param key the key that should be deleted.
    * @param ts the timestamp linked to this operation.
    * @return the delta corresponding to this operation.
    */
    @Name("deleteInt")
    fun deleteInt(key: String, ts: Timestamp): MVMap {
        return put(key, null as Int?, ts)
    }

    /**
    * Deletes a given key / string value pair if it is present in the map and has not yet been
    * deleted.
    * @param key the key that should be deleted.
    * @param ts the timestamp linked to this operation.
    * @return the delta corresponding to this operation.
    */
    @Name("deleteString")
    fun deleteString(key: String, ts: Timestamp): MVMap {
        return put(key, null as String?, ts)
    }

    /**
    * Generates a delta of operations recorded and not already present in a given context.
    * @param vv the context used as starting point to generate the delta.
    * @return the corresponding delta of operations.
    */
    override fun generateDeltaProtected(vv: VersionVector):  DeltaCRDT<MVMap>{
        var delta = MVMap()
        for ((key, meta) in this.entries) {
            if (meta.any { !vv.contains(it.second) }) {
                delta.entries.put(key, meta.toMutableSet())
            }
        }
        delta.causalContext.update(this.causalContext)
        return delta
    }

    /**
    * Merges information contained in a given delta into the local replica, the merge is unilateral
    * and only the local replica is modified.
    * A foreign (local) value is kept iff it is contained in the local (foreign) replica or its
    * associated timestamp is not included in the local (foreign) causal context.
    * @param delta the delta that should be merged with the local replica.
    */
    override fun mergeProtected(delta: DeltaCRDT<MVMap>) {
        if (delta !is MVMap)
            throw UnexpectedTypeException("MVMap does not support merging with type: " + delta::class)

        for ((key, foreignEntries) in delta.entries) {

            val keptEntries = mutableSetOf<Pair<String?, Timestamp>>()
            val localEntries = this.entries.get(key)
            if (localEntries != null) {
                for ((value, ts) in localEntries) {
                    if (!delta.causalContext.contains(ts) || foreignEntries.any { it.second == ts }) {
                        keptEntries.add(Pair(value, ts))
                    }
                }
                for ((value, ts) in foreignEntries) {
                    if (!this.causalContext.contains(ts)) {
                        keptEntries.add(Pair(value, ts))
                    }
                }
            } else {
                for ((value, ts) in foreignEntries) {
                    keptEntries.add(Pair(value, ts))
                }
            }
            this.entries.put(key, keptEntries)
        }
        this.causalContext.update(delta.causalContext)
    }

    /**
    * Serializes this crdt map to a json string.
    * @return the resulted json string.
    */
    @Name("toJson")
    fun toJson(): String {
        val jsonSerializer = JsonMVMapSerializer(MVMap.serializer())
        return Json.stringify<MVMap>(jsonSerializer, this)
    }

    companion object {
        /**
        * Constant value for key fields' separator.
        */
        const val SEPARATOR = "%"

        /**
        * Constant suffix value for key associated to a value of type Boolean.
        */
        const val BOOLEAN = MVMap.SEPARATOR + "BOOLEAN"

        /**
        * Constant suffix value for key associated to a value of type double.
        */
        const val DOUBLE = MVMap.SEPARATOR + "DOUBLE"

        /**
        * Constant suffix value for key associated to a value of type integer.
        */
        const val INTEGER = MVMap.SEPARATOR + "INTEGER"

        /**
        * Constant suffix value for key associated to a value of type string.
        */
        const val STRING = MVMap.SEPARATOR + "STRING"

        /**
        * Deserializes a given json string in a crdt map.
        * @param json the given json string.
        * @return the resulted crdt map.
        */
        @Name("fromJson")
        fun fromJson(json: String): MVMap {
            val jsonSerializer = JsonMVMapSerializer(MVMap.serializer())
            return Json.parse(jsonSerializer, json)
        }
    }
}

/**
* This class is a json transformer for MVMap, it allows the separation between data and metadata.
*/
class JsonMVMapSerializer(private val serializer: KSerializer<MVMap>) :
        JsonTransformingSerializer<MVMap>(serializer, "JsonMVMapSerializer") {

    override fun writeTransform(element: JsonElement): JsonElement {
        val values = mutableMapOf<String, JsonElement>()
        val entries = mutableMapOf<String, JsonElement>()
        val causalContext = element.jsonObject.getObject("causalContext")
        for ((key, entry) in element.jsonObject.getObject("entries")) {
            val value = mutableListOf<JsonElement>()
            val meta = mutableListOf<JsonElement>()
            for (tmpPair in entry.jsonArray) {
                if (key.endsWith(MVMap.BOOLEAN)) {
                    value.add(JsonPrimitive(tmpPair.jsonObject.getPrimitive("first").booleanOrNull) as JsonElement)
                } else if (key.endsWith(MVMap.DOUBLE)) {
                    value.add(JsonPrimitive(tmpPair.jsonObject.getPrimitive("first").doubleOrNull) as JsonElement)
                } else if (key.endsWith(MVMap.INTEGER)) {
                    value.add(JsonPrimitive(tmpPair.jsonObject.getPrimitive("first").intOrNull) as JsonElement)
                } else {
                  value.add(tmpPair.jsonObject.get("first") as JsonElement)
                }
                meta.add(tmpPair.jsonObject.getObject("second"))
            }
            values.put(key, JsonArray(value))
            entries.put(key, JsonArray(meta))
        }
        val metadata = JsonObject(mapOf("entries" to JsonObject(entries.toMap()), "causalContext" to causalContext))
        return JsonObject(mapOf("_type" to JsonPrimitive("MVMap"), "_metadata" to metadata).plus(values))
    }

    override fun readTransform(element: JsonElement): JsonElement {
        val metadata = element.jsonObject.getObject("_metadata")
        val causalContext = metadata.getObject("causalContext")
        val entries = mutableMapOf<String, JsonElement>()
        for ((key, meta) in metadata.getObject("entries")) {
            val values = element.jsonObject.getArray(key)
            val tmpEntries = mutableListOf<JsonElement>()
            var idx = 0
            for (ts in meta.jsonArray) {
                var value = values[idx]
                if (value !is JsonNull && !key.endsWith(MVMap.STRING)) {
                    value = JsonPrimitive(value.toString())
                }
                val tmpEntry = JsonObject(mapOf("first" to value, "second" to ts))
                tmpEntries.add(tmpEntry)
                idx++
            }
            entries.put(key, JsonArray(tmpEntries))
        }
        return JsonObject(mapOf("entries" to JsonObject(entries), "causalContext" to causalContext))
    }
}
package com.example.hipermarketsallingapplication.utils

import org.json.JSONArray
import org.json.JSONObject

object JsonHelper {

    // Python-like json.dumps helper for serializing lists/maps/objects to JSON strings
    fun dumps(obj: Any?): String {
        return when (obj) {
            is List<*> -> {
                val jsonArray = JSONArray()
                for (item in obj) {
                    when (item) {
                        is List<*> -> jsonArray.put(JSONArray(item))
                        is Map<*, *> -> {
                            val jsonObj = JSONObject()
                            for ((k, v) in item) {
                                jsonObj.put(k.toString(), v)
                            }
                            jsonArray.put(jsonObj)
                        }
                        else -> jsonArray.put(item)
                    }
                }
                jsonArray.toString()
            }
            is Map<*, *> -> {
                val jsonObject = JSONObject()
                for ((k, v) in obj) {
                    jsonObject.put(k.toString(), v)
                }
                jsonObject.toString()
            }
            else -> obj?.toString() ?: "[]"
        }
    }

    // Python-like json.loads helper for deserializing JSON strings to lists/maps
    fun loads(jsonStr: String?): Any {
        if (jsonStr.isNullOrBlank()) return emptyList<Any>()
        val trimmed = jsonStr.trim()
        return try {
            if (trimmed.startsWith("[")) {
                val jsonArray = JSONArray(trimmed)
                val list = mutableListOf<Any>()
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.opt(i)
                    if (item is JSONArray) {
                        val subList = mutableListOf<Any>()
                        for (j in 0 until item.length()) {
                            subList.add(item.opt(j))
                        }
                        list.add(subList)
                    } else if (item is JSONObject) {
                        val map = mutableMapOf<String, Any>()
                        val keys = item.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            map[key] = item.opt(key) ?: ""
                        }
                        list.add(map)
                    } else {
                        if (item != null) list.add(item)
                    }
                }
                list
            } else if (trimmed.startsWith("{")) {
                val jsonObject = JSONObject(trimmed)
                val map = mutableMapOf<String, Any>()
                val keys = jsonObject.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    map[key] = jsonObject.opt(key) ?: ""
                }
                map
            } else {
                trimmed
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList<Any>()
        }
    }
}

package com.seanshubin.json.console.domain
import com.seanshubin.json.console.domain.Direction.*
import com.seanshubin.json.console.domain.Type.*

import com.fasterxml.jackson.module.kotlin.readValue
import java.lang.ClassCastException
import java.lang.Exception
import java.nio.file.Path
import java.nio.file.Paths

class Runner(
    private val args: Array<String>,
    private val files: FilesContract,
    private val emit:(Any?)->Unit
) : Runnable {
    override fun run() {
        try {
            runThatThrows()
        }catch(ex:Exception){
            syntax(ex)
        }
    }

    private fun runThatThrows() {
        val file = Paths.get(args[0])
        val direction = Direction.valueOf(args[1].uppercase())
        val type = Type.valueOf(args[2].uppercase())
        val value = args[3]
        val typedValue = type.fromString(value)
        val path = args.drop(4)
        when (direction) {
            GET -> emit(loadObject(file, typedValue, path))
            SET -> storeObject(file, typedValue, path)
        }
    }

    private fun syntax(ex:Exception){
        ex.printStackTrace()
        println("syntax:")
        println()
        println("java -jar json-console-app.jar file direction type value path...")
        println()
        println("file     : the json file")
        println("direction: GET or SET")
        println("type     : STRING or INT, the data type")
        println("value    : default value if the direction is GET, new value if the direction is SET")
        println("path     : list of keys for the nested json object")
    }

    private fun loadObject(filePath: Path, default: Any, path: List<String>): Any? {
        if (!files.exists(filePath)) {
            storeObject(filePath, default, path)
        }
        val text = files.readString(filePath)
        val model: Any? = JsonMappers.parser.readValue(text)
        val value = getObject(model, default, path)
        storeObject(filePath, value, path)
        return value
    }

    private fun storeObject(filePath:Path, value: Any?, path: List<String>) {
        if (!files.exists(filePath)) {
            if (filePath.parent != null && !files.exists(filePath.parent)) {
                files.createDirectories(filePath.parent)
            }
            files.writeString(filePath, "{}")
        }
        val oldText = files.readString(filePath)
        val oldModel: Any? = JsonMappers.parser.readValue(oldText)
        val newModel = putObject(oldModel, value, path)
        val newText = JsonMappers.pretty.writeValueAsString(newModel)
        files.writeString(filePath, newText)
    }

    private fun putObject(untypedDestination: Any?, value: Any?, path: List<String>): Map<String, Any?> {
        val destination = castToJsonObject(untypedDestination, path)
        val head = path[0]
        val tail = path.drop(1)
        return if (path.size == 1) {
            destination + (head to value)
        } else {
            val existing = destination[head]
            if (existing == null) {
                destination + (head to putObject(emptyMap<String, Any?>(), value, tail))
            } else {
                destination + (head to putObject(existing, value, tail))
            }
        }
    }

    private fun getObject(untypedSource: Any?, default: Any?, path: List<String>): Any? {
        val source = castToJsonObject(untypedSource, path)
        val head = path[0]
        val tail = path.drop(1)
        return if (path.size == 1) {
            if (source.containsKey(head)) {
                source[head]
            } else {
                default
            }
        } else {
            val existing = source[head]
            if (existing == null) {
                default
            } else {
                getObject(existing, default, tail)
            }
        }
    }

    private fun castToString(value: Any?, path: List<String>): String =
        when (value) {
            null -> throwCastError(null, "null", "String", path)
            is String -> value
            else -> throwCastError(value, value.javaClass.simpleName, "String", path)
        }

    private fun castToInt(value: Any?, path: List<String>): Int =
        when (value) {
            null -> throwCastError(null, "null", "Int", path)
            is Int -> value
            else -> throwCastError(value, value.javaClass.simpleName, "Int", path)
        }

    @Suppress("UNCHECKED_CAST")
    private fun castToJsonObject(value: Any?, path: List<String>): Map<String, Any?> =
        when (value) {
            null -> throwCastError(null, "null", "Map<String, Object>", path)
            is Map<*, *> -> value as Map<String, Any?>
            else -> throwCastError(value, value.javaClass.simpleName, "Map<String, Object>", path)
        }

    private fun throwCastError(
        value: Any?,
        sourceTypeName: String,
        destinationTypeName: String,
        path: List<String>
    ): Nothing {
        val joinedPath = path.joinToString(".")
        val message = "Unable to cast value $value of type $sourceTypeName to $destinationTypeName at path $joinedPath"
        throw ClassCastException(message)
    }
}
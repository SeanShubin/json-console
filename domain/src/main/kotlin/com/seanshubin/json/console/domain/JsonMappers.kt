package com.seanshubin.json.console.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue

object JsonMappers {
    val pretty: ObjectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
        .enable(SerializationFeature.INDENT_OUTPUT)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    val compact: ObjectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    val parser: ObjectMapper = compact
    inline fun <reified T> parse(json: String): T = parser.readValue(json)
}

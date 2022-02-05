package com.seanshubin.json.console.app

object EntryPoint {
    @JvmStatic
    fun main(args: Array<String>) {
        Dependencies(args).runner.run()
    }
}
package com.seanshubin.json.console.app

import com.seanshubin.json.console.domain.FilesContract
import com.seanshubin.json.console.domain.FilesDelegate
import com.seanshubin.json.console.domain.Runner

class Dependencies(args:Array<String>) {
    val files:FilesContract = FilesDelegate
    val emit:(Any?)->Unit = ::println
    val runner:Runnable = Runner(args, files, emit)
}

package de.florianm.klox

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess


fun main(
    args: Array<String>
) {
    if (1 < args.size) {
        println("klox [script]")
        exitProcess(64)
    } else if (1 == args.size) {
        KLox().runFile(args[0])
    } else {
        KLox().runPrompt()
    }
}

class KLox {


    fun runFile(
        path: String
    ) {
        val src = Files.readString(Paths.get(path))
        run(src)

        if (hasErrors) exitProcess(65)
    }

    fun runPrompt() {
        val stdIn = InputStreamReader(System.`in`)
        val reader = BufferedReader(stdIn)

        while (true) {
            print("> ")
            val line = reader.readLine() ?: return
            if (line.isBlank()) return

            run(line)
            hasErrors = false
        }
    }

    fun run(
        src: String
    ) {
        val scanner = Scanner(src)
        val tokens = scanner.scanTokens()
        tokens.forEach(::println)
    }

    companion object {
        private var hasErrors = false

        fun error(
            line: Int,
            message: String
        ) {
            report(line, "", message)
        }

        private fun report(
            line: Int,
            where: String,
            message: String
        ) {
            System.err.println("[Line $line] Error${where}: $message")
            hasErrors = true
        }
    }
}
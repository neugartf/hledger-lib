package com.neugartf.hledger

import com.neugartf.antlr.parser.GrammarLexer
import com.neugartf.hledger.antlr.parser.GrammarListenerImpl
import com.neugartf.antlr.parser.GrammarParser
import com.neugartf.hledger.model.Transaction
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import org.antlr.v4.kotlinruntime.ConsoleErrorListener
import org.antlr.v4.kotlinruntime.tree.ParseTreeWalker
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString

@Suppress("unused")
object HledgerParser {
    fun parse(string: String): List<Transaction> {
        val lexer = GrammarLexer(CharStreams.fromString(string))
        val tokenStream = CommonTokenStream(lexer)
        val grammarParser = GrammarParser(tokenStream)
        val grammarListenerImpl = GrammarListenerImpl()
        grammarParser.addErrorListener(ConsoleErrorListener())
        val walker = ParseTreeWalker()
        walker.walk(grammarListenerImpl, grammarParser.file_())
        return grammarListenerImpl.entries
    }

    fun parse(file: Path): List<Transaction> = parse(file.readText())

    @OptIn(ExperimentalStdlibApi::class)
    private fun Path.readText(): String = SystemFileSystem
        .source(this)
        .buffered()
        .use(Source::readString)
}
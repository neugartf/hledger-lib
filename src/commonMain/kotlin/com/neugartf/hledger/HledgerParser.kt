package com.neugartf.hledger

import com.neugartf.antlr.parser.GrammarLexer
import com.neugartf.antlr.parser.GrammarListenerImpl
import com.neugartf.antlr.parser.GrammarParser
import com.neugartf.hledger.model.Transaction
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import org.antlr.v4.kotlinruntime.ConsoleErrorListener
import org.antlr.v4.kotlinruntime.tree.ParseTreeWalker

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
}
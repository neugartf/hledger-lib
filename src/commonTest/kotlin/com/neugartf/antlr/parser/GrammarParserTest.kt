package com.neugartf.antlr.parser

import com.strumenta.kotlinmultiplatform.BitSet
import kotlinx.datetime.LocalDate
import org.antlr.v4.kotlinruntime.ANTLRErrorListener
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import org.antlr.v4.kotlinruntime.Parser
import org.antlr.v4.kotlinruntime.RecognitionException
import org.antlr.v4.kotlinruntime.Recognizer
import org.antlr.v4.kotlinruntime.atn.ATNConfigSet
import org.antlr.v4.kotlinruntime.dfa.DFA
import org.antlr.v4.kotlinruntime.tree.ParseTreeWalker
import kotlin.test.Test
import kotlin.test.assertEquals

class GrammarParserTest {

    @Test
    fun testParser() {
        val charStream = CharStreams.fromString(
            """
            2023-02-01 GOODWORKS CORP
            assets:bank:checking       ${'$'}1000
            income:salary 
        """.trimIndent()
        )
        val lexer = GrammarLexer(charStream)
        val tokenStream = CommonTokenStream(lexer)
        val p = GrammarParser(tokenStream)
        val listenerImpl = GrammarListenerImpl()
        p.addErrorListener(object : ANTLRErrorListener {
            override fun reportAmbiguity(
                recognizer: Parser,
                dfa: DFA,
                startIndex: Int,
                stopIndex: Int,
                exact: Boolean,
                ambigAlts: BitSet,
                configs: ATNConfigSet
            ) {
                println()
            }

            override fun reportAttemptingFullContext(
                recognizer: Parser,
                dfa: DFA,
                startIndex: Int,
                stopIndex: Int,
                conflictingAlts: BitSet,
                configs: ATNConfigSet
            ) {
                println()

            }

            override fun reportContextSensitivity(
                recognizer: Parser,
                dfa: DFA,
                startIndex: Int,
                stopIndex: Int,
                prediction: Int,
                configs: ATNConfigSet
            ) {
                TODO("Not yet implemented")
            }

            override fun syntaxError(
                recognizer: Recognizer<*, *>,
                offendingSymbol: Any?,
                line: Int,
                charPositionInLine: Int,
                msg: String,
                e: RecognitionException?
            ) {
                println(e)
                println(offendingSymbol)
            }

        })
        val walker = ParseTreeWalker()
        walker.walk(listenerImpl, p.file_())
        assertEquals(
            listOf(
                Transaction(
                    LocalDate.parse("2023-02-01"),
                    "GOODWORKS CORP",
                    mutableListOf(
                        Posting.DefaultPosting("assets:bank:checking", Currency.Dollar, 1000f),
                        Posting.DefaultPosting("income:salary")
                    )
                )
            ), listenerImpl.entries
        )
    }


    @Test
    fun testParserNegative() {
        val charStream = CharStreams.fromString(
            """2023-01-01 opening balances           
    assets:bank:checking        ${'$'}1000  
    assets:bank:savings         ${'$'}2000 
    assets:cash                  ${'$'}100
    liabilities:credit card      ${'$'}-50
    equity:opening/closing     ${'$'}-3050
               
        """.trimIndent()
        )
        val lexer = GrammarLexer(charStream)
        val tokenStream = CommonTokenStream(lexer)
        val p = GrammarParser(tokenStream)
        val listenerImpl = GrammarListenerImpl()
        p.addErrorListener(object : ANTLRErrorListener {
            override fun reportAmbiguity(
                recognizer: Parser,
                dfa: DFA,
                startIndex: Int,
                stopIndex: Int,
                exact: Boolean,
                ambigAlts: BitSet,
                configs: ATNConfigSet
            ) {
                println()
            }

            override fun reportAttemptingFullContext(
                recognizer: Parser,
                dfa: DFA,
                startIndex: Int,
                stopIndex: Int,
                conflictingAlts: BitSet,
                configs: ATNConfigSet
            ) {

            }

            override fun reportContextSensitivity(
                recognizer: Parser,
                dfa: DFA,
                startIndex: Int,
                stopIndex: Int,
                prediction: Int,
                configs: ATNConfigSet
            ) {

            }

            override fun syntaxError(
                recognizer: Recognizer<*, *>,
                offendingSymbol: Any?,
                line: Int,
                charPositionInLine: Int,
                msg: String,
                e: RecognitionException?
            ) {
                println(e)
                println(offendingSymbol)
            }

        })
        val walker = ParseTreeWalker()
        walker.walk(listenerImpl, p.file_())
        listenerImpl.entries.map { println(it) }
    }

    @Test
    fun testParserCommodity() {
        val charStream = CharStreams.fromString(
            """2022-01-01 Cost in another commodity can
assets:investments           2.0 AAAA @ $1.0
assets:investments           3.0 AAAA @ $4
assets:checking            $-7.00

""".trimIndent()
        )
        val lexer = GrammarLexer(charStream)
        val tokenStream = CommonTokenStream(lexer)
        val p = GrammarParser(tokenStream)
        val listenerImpl = GrammarListenerImpl()
        p.addErrorListener(object : ANTLRErrorListener {
            override fun reportAmbiguity(
                recognizer: Parser,
                dfa: DFA,
                startIndex: Int,
                stopIndex: Int,
                exact: Boolean,
                ambigAlts: BitSet,
                configs: ATNConfigSet
            ) {

            }

            override fun reportAttemptingFullContext(
                recognizer: Parser,
                dfa: DFA,
                startIndex: Int,
                stopIndex: Int,
                conflictingAlts: BitSet,
                configs: ATNConfigSet
            ) {

            }

            override fun reportContextSensitivity(
                recognizer: Parser,
                dfa: DFA,
                startIndex: Int,
                stopIndex: Int,
                prediction: Int,
                configs: ATNConfigSet
            ) {
                TODO("Not yet implemented")
            }

            override fun syntaxError(
                recognizer: Recognizer<*, *>,
                offendingSymbol: Any?,
                line: Int,
                charPositionInLine: Int,
                msg: String,
                e: RecognitionException?
            ) {
                println(e)
                println(offendingSymbol)
            }

        })
        val walker = ParseTreeWalker()
        walker.walk(listenerImpl, p.file_())
        listenerImpl.entries.map { println(it) }
    }

}
package com.neugartf.antlr.parser

import com.strumenta.kotlinmultiplatform.BitSet
import kotlinx.datetime.LocalDate
import org.antlr.v4.kotlinruntime.ANTLRErrorListener
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import org.antlr.v4.kotlinruntime.ConsoleErrorListener
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
            2022-01-01 opening balances
                assets:bank:checking        $1000  
                assets:bank:savings         $2000 
                assets:cash:wallet           $100
                liabilities:credit card     $-200
                equity
        """.trimIndent()
        )
        val lexer = GrammarLexer(charStream)
        val tokenStream = CommonTokenStream(lexer)
        val p = GrammarParser(tokenStream)
        val listenerImpl = GrammarListenerImpl()
        p.addErrorListener(ConsoleErrorListener())
        val walker = ParseTreeWalker()
        walker.walk(listenerImpl, p.file_())
        assertEquals(
            listOf(Transaction(
                date = LocalDate.parse("2022-01-01"),
                description = "opening balances",
                postings = mutableListOf(
                    Posting.DefaultPosting("assets:bank:checking", Currency.Dollar, 1000f),
                    Posting.DefaultPosting("assets:bank:savings", Currency.Dollar, 2000f),
                    Posting.DefaultPosting("assets:cash:wallet", Currency.Dollar, 100f),
                    Posting.DefaultPosting("liabilities:credit card", Currency.Dollar, -200f),
                    Posting.DefaultPosting("equity"),
                )
            )),
            listenerImpl.entries
        )

    }

    @Test
    fun testParserCommodity() {
        val charStream = CharStreams.fromString(
            """
            2022-01-01 Cost in another commodity can
                assets:investments           2.0 AAAA @ $1.0
                assets:investments           3.0 AAAA @ $4
                assets:checking            $-7.00

            """.trimIndent()
        )
        val lexer = GrammarLexer(charStream)
        val tokenStream = CommonTokenStream(lexer)
        val p = GrammarParser(tokenStream)
        val listenerImpl = GrammarListenerImpl()
        p.addErrorListener(ConsoleErrorListener())
        val walker = ParseTreeWalker()
        walker.walk(listenerImpl, p.file_())
        assertEquals(
            listOf(Transaction(
                date = LocalDate.parse("2022-01-01"),
                description = "Cost in another commodity can",
                postings = mutableListOf(
                    Posting.CommodityPosting("assets:investments", "AAAA", 2.0f, Currency.Dollar, 1f),
                    Posting.CommodityPosting("assets:investments", "AAAA", 3.0f, Currency.Dollar, 4f),
                    Posting.DefaultPosting("assets:checking", Currency.Dollar, -7.00f),
                )
            )),
            listenerImpl.entries
        )
    }

}
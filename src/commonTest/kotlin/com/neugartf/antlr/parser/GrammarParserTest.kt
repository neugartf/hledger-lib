package com.neugartf.antlr.parser

import com.neugartf.hledger.model.Posting
import com.neugartf.hledger.model.Currency
import com.neugartf.hledger.model.Transaction
import kotlinx.datetime.LocalDate
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import org.antlr.v4.kotlinruntime.ConsoleErrorListener
import org.antlr.v4.kotlinruntime.tree.ParseTreeWalker
import kotlin.test.Test
import kotlin.test.assertEquals

class GrammarParserTest {

    @Test
    fun testParser() {
        // ASSIGN
        val charStream = CharStreams.fromString(
            """
            2022-01-01 opening balances
                assets:bank:checking        $1000
                assets:bank:savings         $2000 
                assets:cash:wallet           $100
                liabilities:credit card     $-200
                equity
                
            2022-01-01
                assets:bank:checking        $1000
                equity
        """.trimIndent()
        )
        val lexer = GrammarLexer(charStream)
        val tokenStream = CommonTokenStream(lexer)
        val parser = GrammarParser(tokenStream)
        val listenerImpl = GrammarListenerImpl()
        parser.addErrorListener(ConsoleErrorListener())

        // ACT
        val walker = ParseTreeWalker()
        walker.walk(listenerImpl, parser.file_())
        val result = listenerImpl.entries

        // ASSERT
        assertEquals(
            listOf(
                Transaction(
                date = LocalDate.parse("2022-01-01"),
                description = "opening balances",
                postings = mutableListOf(
                    Posting.DefaultPosting("assets:bank:checking", Currency.Dollar, 1000f),
                    Posting.DefaultPosting("assets:bank:savings", Currency.Dollar, 2000f),
                    Posting.DefaultPosting("assets:cash:wallet", Currency.Dollar, 100f),
                    Posting.DefaultPosting("liabilities:credit card", Currency.Dollar, -200f),
                    Posting.DefaultPosting("equity"),
                )
            ),
                Transaction(
                    date = LocalDate.parse("2022-01-01"),
                    description = null,
                    postings = mutableListOf(
                        Posting.DefaultPosting("assets:bank:checking", Currency.Dollar, 1000f),
                        Posting.DefaultPosting("equity"),
                    )
                )
            ),
            result
        )

    }

    @Test
    fun testParserCommodity() {
        // ASSIGN
        val charStream = CharStreams.fromString(
            """
            2022-01-01 Cost in another commodity can
                assets:investments           2.0 AAAA @ $1.0
                assets:investments           3.0 "A1AA" @ $4
                assets:checking            $-7.00

            """.trimIndent()
        )
        val lexer = GrammarLexer(charStream)
        val tokenStream = CommonTokenStream(lexer)
        val parser = GrammarParser(tokenStream)
        val listener = GrammarListenerImpl()
        parser.addErrorListener(ConsoleErrorListener())

        // ACT
        val walker = ParseTreeWalker()
        walker.walk(listener, parser.file_())

        // ASSERT
        assertEquals(
            listOf(
                Transaction(
                date = LocalDate.parse("2022-01-01"),
                description = "Cost in another commodity can",
                postings = mutableListOf(
                    Posting.CommodityPosting("assets:investments", "AAAA", 2.0f, Currency.Dollar, 1f),
                    Posting.CommodityPosting("assets:investments", "A1AA", 3.0f, Currency.Dollar, 4f),
                    Posting.DefaultPosting("assets:checking", Currency.Dollar, -7.00f),
                )
            )
            ),
            listener.entries
        )
    }

}
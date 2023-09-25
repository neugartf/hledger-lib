package com.neugartf.antlr.parser

import kotlinx.datetime.LocalDate

enum class Currency(val c: Char) {
    Euro('€'), Dollar('$');

    companion object {
        fun fromChar(char: Char): Currency? = Currency.entries.find { it.c == char }
    }

}

sealed class Posting(open var account: String? = null) {
    data class DefaultPosting(override var account: String? =null, var currency: Currency? = null, var value: Float? = null): Posting(account)

}
data class Transaction(var date: LocalDate? = null, var description: String? = null,
                       val postings: MutableList<Posting> = mutableListOf())

class GrammarListenerImpl: GrammarBaseListener() {

    val entries: MutableList<Transaction> = mutableListOf()
    private var current: Transaction? = null
    private var currentPosting: Posting? = null

    override fun enterDescription(ctx: GrammarParser.DescriptionContext) {
        super.enterDescription(ctx)

        current?.description = ctx.text
    }
    override fun enterDate(ctx: GrammarParser.DateContext) {
        super.enterDate(ctx)
        current?.date = try {
            LocalDate.parse(ctx.text)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    override fun enterDefault_posting(ctx: GrammarParser.Default_postingContext) {
        super.enterDefault_posting(ctx)
        val posting = Posting.DefaultPosting()
        current?.postings?.add(posting)
        currentPosting = posting
    }

    override fun enterCurrency(ctx: GrammarParser.CurrencyContext) {
        super.enterCurrency(ctx)

        (currentPosting as? Posting.DefaultPosting)?.currency = Currency.fromChar(ctx.text[0])
    }
    override fun enterAccount(ctx: GrammarParser.AccountContext) {
        super.enterAccount(ctx)
        currentPosting?.account = ctx.text
    }


    override fun enterTransaction(ctx: GrammarParser.TransactionContext) {
        super.enterTransaction(ctx)
        current = Transaction()
    }

    override fun enterAmount(ctx: GrammarParser.AmountContext) {
        super.enterAmount(ctx)
        println(ctx.text)
        (currentPosting as? Posting.DefaultPosting)?.value = ctx.text.toFloatOrNull()

    }


    override fun exitTransaction(ctx: GrammarParser.TransactionContext) {
        super.exitTransaction(ctx)

        current?.let {
            entries.add(it)
            current = null
        }
    }
}
package com.neugartf.antlr.parser

import kotlinx.datetime.LocalDate

enum class Currency(val c: Char) {
    Euro('€'), Dollar('$');

    companion object {
        fun fromChar(char: Char): Currency? = Currency.entries.find { it.c == char }
    }

}

sealed class Posting(open var account: String? = null) {
    data class DefaultPosting(override var account: String? = null, var currency: Currency? = null, var value: Float? = null): Posting(account)
    data class CommodityPosting(override var account: String? = null, var name: String? = null, var quantity: Float?= null, var currency: Currency? = null, var unitPrice: Float? = null ):Posting(account)
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

    override fun enterCommodity_posting(ctx: GrammarParser.Commodity_postingContext) {
        super.enterCommodity_posting(ctx)
        val posting = Posting.CommodityPosting()
        current?.postings?.add(posting)
        currentPosting = posting
    }

    override fun enterCommodity_name(ctx: GrammarParser.Commodity_nameContext) {
        super.enterCommodity_name(ctx)
        (currentPosting as? Posting.CommodityPosting)?.name = ctx.text
    }

    override fun enterCurrency(ctx: GrammarParser.CurrencyContext) {
        super.enterCurrency(ctx)

        when (currentPosting) {
            is Posting.CommodityPosting -> (currentPosting as Posting.CommodityPosting).currency = Currency.fromChar(ctx.text[0])
            is Posting.DefaultPosting -> (currentPosting  as Posting.DefaultPosting).currency  = Currency.fromChar(ctx.text[0])
            null -> throw IllegalArgumentException()
        }

    }


    override fun enterQuantity(ctx: GrammarParser.QuantityContext) {
        super.enterQuantity(ctx)
        (currentPosting as? Posting.CommodityPosting)?.quantity = ctx.text.toFloatOrNull()
    }


    override fun enterAccount(ctx: GrammarParser.AccountContext) {
        super.enterAccount(ctx)
        currentPosting?.account = ctx.text.trim()
    }


    override fun enterTransaction(ctx: GrammarParser.TransactionContext) {
        super.enterTransaction(ctx)
        current = Transaction()
    }

    override fun enterAmount(ctx: GrammarParser.AmountContext) {
        super.enterAmount(ctx)
        when(currentPosting) {
            is Posting.CommodityPosting ->  (currentPosting as? Posting.CommodityPosting)?.unitPrice = ctx.text.toFloatOrNull()
            is Posting.DefaultPosting ->  (currentPosting as? Posting.DefaultPosting)?.value = ctx.text.toFloatOrNull()
            null -> throw IllegalArgumentException()
        }

    }


    override fun exitTransaction(ctx: GrammarParser.TransactionContext) {
        super.exitTransaction(ctx)

        current?.let {
            entries.add(it)
            current = null
        }
    }
}
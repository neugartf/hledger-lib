package com.neugartf.hledger.model

sealed class Posting(open var account: String? = null) {
    data class DefaultPosting(
        override var account: String? = null,
        var currency: Currency? = null,
        var value: Float? = null
    ) : Posting(account)

    data class CommodityPosting(
        override var account: String? = null,
        var name: String? = null,
        var quantity: Float? = null,
        var currency: Currency? = null,
        var unitPrice: Float? = null
    ) :
        Posting(account)
}
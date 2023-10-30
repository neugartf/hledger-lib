package com.neugartf.hledger.model

import kotlinx.datetime.LocalDate

data class Transaction(
    var date: LocalDate? = null, var description: String? = null,
    val postings: MutableList<Posting> = mutableListOf()
)
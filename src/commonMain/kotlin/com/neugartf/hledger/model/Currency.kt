package com.neugartf.hledger.model

enum class Currency(val c: Char) {
    Euro('€'), Dollar('$');

    companion object {
        fun fromChar(char: Char): Currency? = Currency.entries.find { it.c == char }
    }

}
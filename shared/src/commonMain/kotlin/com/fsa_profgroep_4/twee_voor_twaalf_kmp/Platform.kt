package com.fsa_profgroep_4.twee_voor_twaalf_kmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
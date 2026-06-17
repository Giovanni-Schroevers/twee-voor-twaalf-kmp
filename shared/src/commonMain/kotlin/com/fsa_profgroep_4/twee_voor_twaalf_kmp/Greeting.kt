package com.fsa_profgroep_4.twee_voor_twaalf_kmp

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return sayHello(platform.name)
    }
}
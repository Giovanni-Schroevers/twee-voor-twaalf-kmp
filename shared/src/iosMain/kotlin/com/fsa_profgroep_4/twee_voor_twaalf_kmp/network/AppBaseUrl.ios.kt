package com.fsa_profgroep_4.twee_voor_twaalf_kmp.network

/** The iOS simulator shares the host's network, so `localhost` works there. */
internal actual fun localDevBaseUrl(): String = "http://localhost:8080/"

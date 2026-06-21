package com.fsa_profgroep_4.twee_voor_twaalf_kmp.network

/** Desktop runs on the host, so `localhost` reaches the backend directly. */
internal actual fun localDevBaseUrl(): String = "http://localhost:8080/"

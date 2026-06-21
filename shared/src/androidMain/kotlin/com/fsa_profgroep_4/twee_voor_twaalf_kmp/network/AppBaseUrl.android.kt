package com.fsa_profgroep_4.twee_voor_twaalf_kmp.network

/** The Android emulator reaches the host machine at `10.0.2.2`, not `localhost`. */
internal actual fun localDevBaseUrl(): String = "http://10.0.2.2:8080/"

package teksturepako.pakku.io

import platform.posix.*
import kotlinx.cinterop.*

@OptIn(ExperimentalForeignApi::class)
fun getEnv(env: String): String? = getenv(env)?.toKString()
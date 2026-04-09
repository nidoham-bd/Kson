package com.nidoham.kson.logging

import kotlin.reflect.KClass

interface KsonLogger {
    fun debug(message: String)
    fun info(message: String)
    fun warn(message: String)
    fun error(message: String)
    fun error(message: String, throwable: Throwable)

    companion object {
        private var loggerFactory: (KClass<*>) -> KsonLogger = { DefaultKsonLogger(it.simpleName ?: "Kson") }

        fun setLoggerFactory(factory: (KClass<*>) -> KsonLogger) { loggerFactory = factory }
        fun getLogger(kClass: KClass<*>): KsonLogger = loggerFactory(kClass)
        fun getLogger(name: String): KsonLogger = loggerFactory(object : KClass<Nothing> {
            override val simpleName: String? = name; override val qualifiedName: String? = name
        })
        fun disableLogging() { loggerFactory = { NoOpLogger } }
        fun enableDebugLogging() { loggerFactory = { DefaultKsonLogger(it.simpleName ?: "Kson", true) } }
    }
}

class DefaultKsonLogger(private val tag: String, private val debugEnabled: Boolean = false) : KsonLogger {
    private var enabled = true
    fun setEnabled(enabled: Boolean) { this.enabled = enabled }

    override fun debug(message: String) { if (enabled && debugEnabled) println("[Kson DEBUG] $tag: $message") }
    override fun info(message: String) { if (enabled) println("[Kson INFO] $tag: $message") }
    override fun warn(message: String) { if (enabled) println("[Kson WARN] $tag: $message") }
    override fun error(message: String) { println("[Kson ERROR] $tag: $message") }
    override fun error(message: String, throwable: Throwable) { println("[Kson ERROR] $tag: $message - ${throwable.message}"); throwable.printStackTrace() }
}

object NoOpLogger : KsonLogger {
    override fun debug(message: String) {}
    override fun info(message: String) {}
    override fun warn(message: String) {}
    override fun error(message: String) {}
    override fun error(message: String, throwable: Throwable) {}
}
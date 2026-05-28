package io.github.androidpoet.pocketbase.client

import kotlinx.serialization.json.JsonObject

public class AuthStore {
    public var token: String = ""
        private set
    public var record: JsonObject? = null
        private set

    public val isValid: Boolean get() = token.isNotBlank()

    public fun save(token: String, record: JsonObject?) {
        this.token = token
        this.record = record
    }

    public fun clear() {
        token = ""
        record = null
    }
}

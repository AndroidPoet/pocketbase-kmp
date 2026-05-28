package io.github.androidpoet.pocketbase.auth

import io.github.androidpoet.pocketbase.client.PocketBaseClient

public fun createAuthClient(client: PocketBaseClient): AuthClient = AuthClientImpl(client)

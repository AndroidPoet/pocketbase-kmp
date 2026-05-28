package io.github.androidpoet.pocketbase.realtime

import io.github.androidpoet.pocketbase.client.PocketBaseClient

public fun createRealtimeClient(client: PocketBaseClient): RealtimeClient = RealtimeClientImpl(client)

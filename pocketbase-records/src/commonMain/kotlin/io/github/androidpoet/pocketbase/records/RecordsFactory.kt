package io.github.androidpoet.pocketbase.records

import io.github.androidpoet.pocketbase.client.PocketBaseClient

public fun createRecordsClient(client: PocketBaseClient): RecordsClient = RecordsClientImpl(client)

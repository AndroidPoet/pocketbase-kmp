package io.github.androidpoet.pocketbase.files

import io.github.androidpoet.pocketbase.client.PocketBaseClient

public fun createFilesClient(client: PocketBaseClient): FilesClient = FilesClientImpl(client)

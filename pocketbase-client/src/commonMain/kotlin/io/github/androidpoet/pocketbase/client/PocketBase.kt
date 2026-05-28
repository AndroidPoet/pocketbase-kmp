package io.github.androidpoet.pocketbase.client

public object PocketBase {
    public fun create(baseUrl: String, lang: String? = null): PocketBaseApi {
        val client = PocketBaseClientImpl(config = PocketBaseConfig(baseUrl = baseUrl, lang = lang))
        return PocketBaseApi(
            client = client,
            health = HealthService(client),
            collections = CollectionsService(client),
            batch = BatchService(client),
            backups = BackupsService(client),
            logs = LogsService(client),
            settings = SettingsService(client),
            crons = CronsService(client),
        )
    }
}

public data class PocketBaseApi(
    val client: PocketBaseClient,
    val health: HealthService,
    val collections: CollectionsService,
    val batch: BatchService,
    val backups: BackupsService,
    val logs: LogsService,
    val settings: SettingsService,
    val crons: CronsService,
)

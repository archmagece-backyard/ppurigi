import com.typesafe.config.ConfigFactory
import org.flywaydb.core.Flyway

object DBMigration {
    fun migrate() {
        val flyway = Flyway()
        val dbType = ConfigFactory.load().getString("db_type")
        val config = ConfigFactory.load().getConfig(dbType)
//        val config = ConfigFactory.load().let {
//            it.getConfig(it.getString("db_type"))
//        }
        flyway.setDataSource(
            config.getString("dataSource.url"),
            config.getString("dataSource.user"),
            config.getString("dataSource.password"),
        )
        flyway.setSchemas("t_scatter", "t_treasure", "t_gather")
        flyway.setLocations("db/migration/$dbType")
        flyway.migrate()
    }
}
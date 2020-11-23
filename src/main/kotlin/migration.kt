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
            config.getString("jdbcUrl"),
            config.getString("username"),
            config.getString("password"),
        )
        flyway.setSchemas("t_ppoo_event", "t_ppoo_prize", "t_ppoo_prizewinner")
        flyway.setLocations("db/migration/$dbType")
        flyway.migrate()
    }
}
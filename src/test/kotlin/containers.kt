import org.testcontainers.containers.MariaDBContainer

class MariadbContainerAdapter(dockerImageName: String): MariaDBContainer<MariadbContainerAdapter>(dockerImageName)


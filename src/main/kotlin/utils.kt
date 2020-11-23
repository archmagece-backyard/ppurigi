import org.apache.commons.lang3.RandomStringUtils

//fun generateToken() = RandomStringUtils.randomAscii(3)!!
//fun generateToken() = RandomStringUtils.random(3)!!
fun generateToken() = RandomStringUtils.random(3, true, true)!!

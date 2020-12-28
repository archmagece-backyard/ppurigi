import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime

fun main(){
    println(DateTime.now().zone)
    println(DateTime.now().millis)
    println(DateTime.now(DateTimeZone.UTC).zone)
    println(DateTime.now(DateTimeZone.UTC).millis)
}
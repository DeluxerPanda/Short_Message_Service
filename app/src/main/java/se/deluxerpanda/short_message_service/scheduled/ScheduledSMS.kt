package se.deluxerpanda.short_message_service.scheduled

import se.deluxerpanda.short_message_service.smssender.MainActivity
import java.text.SimpleDateFormat

class ScheduledSMS {
fun ScheduledSMS(phonenumber: String,message: String, DateStart: String, Clock_Time: String, repeatSmS: String){
    var phonenumber = phonenumber.replace("[/N.,'*;#]".toRegex(), "")
    val dateTimeString: String = DateStart + " " + Clock_Time
    val sdf = SimpleDateFormat("yyyy-MM-dd H:m")

    val date = sdf.parse(dateTimeString)
    val triggerTime = date?.time

}
}
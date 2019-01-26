fun sendMessageToClient(
        client: Client?, message: String?, mailer: Mailer
){
    if (mailer != null) mailer.sendMessage(mailer, message?)
}

class Client (val personalInfo: PersonalInfo?)
class PersonalInfo (val email: String?)
interface Mailer {
    fun sendMessage(email: String, message: String)
}

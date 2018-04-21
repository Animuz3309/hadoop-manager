/**
 * Just use in idea to create an encrypt msg. for example "hm.auth.admin-passowrd"
 */
import ch.qos.logback.classic.Level
import edu.scut.cs.hm.admin.config.JasyptConfiguration
import org.jasypt.encryption.StringEncryptor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.PropertySource

// set logger level
logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
logger.setLevel(Level.INFO)

def cipher

if (args.length < 2) {
    println 'Usage: groovy encrypt.groovy <pwd> <msg>'
    return
}
pwd = args[1]
msg = args[0]

System.setProperty('jasypt.encryptor.password', pwd)
ctx = new AnnotationConfigApplicationContext(GroovyJasyptConfiguration.class)

encryptor = ctx.getBean(StringEncryptor.class)

cipher = encryptor.encrypt(msg)
logger.info('Get cipher: ${}', cipher)

@Configuration
@Import(JasyptConfiguration.class)
@PropertySource(name="EncryptedProperties", value = "classpath:encrypted.properties")
class GroovyJasyptConfiguration {

}





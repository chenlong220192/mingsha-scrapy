package site.mingsha.scrapy.test.example.component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class MailComponent {

    private static final Logger logger = LoggerFactory.getLogger(MailComponent.class);

    @Value("${mail.from}")
    private String              MAIL_FROM;
    @Value("#{'${mail.to}'.split(',')}")
    private List<String>        MAIL_TO;

    /* ----------------------------------------------------------------------- */

    @Resource
    private JavaMailSender      emailSender;

    /* ----------------------------------------------------------------------- */

    /**
     *
     * @param subject
     * @param text
     */
    public void sendMail(String subject, String text) {
        if (MAIL_TO.isEmpty()) {
            return;
        }
        MAIL_TO.forEach(to -> {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(MAIL_FROM);
                message.setTo(to);
                message.setSubject(subject);
                message.setText(text);
                emailSender.send(message);
            } catch (Exception e) {
                logger.error(String.format("Moudle:[%s]%n%s", "sendMail4Mime", e.getMessage()));
            }
        });

    }

    /**
     *
     * @param subject
     * @param text
     * @param path
     */
    public void sendMail4Mime(String subject, String text, String path) {
        if (MAIL_TO.isEmpty()) {
            return;
        }
        MAIL_TO.forEach(to -> {
            try {
                MimeMessage mimeMessage = emailSender.createMimeMessage();

                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                helper.setFrom(MAIL_FROM);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(text);

                // 添加附件
                Path _path = Paths.get(path);
                String fileName = _path.getFileName().toString();
                byte[] attachmentBytes = Files.readAllBytes(_path);
                InputStreamSource inputStreamSource = new ByteArrayResource(attachmentBytes);
                helper.addAttachment(fileName, inputStreamSource);

                emailSender.send(mimeMessage);
            } catch (Exception e) {
                logger.error(String.format("Moudle:[%s]%n%s", "sendMail4Mime", e.getMessage()));
            }
        });
    }

}

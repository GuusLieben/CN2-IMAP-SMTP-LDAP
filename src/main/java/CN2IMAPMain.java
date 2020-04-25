import com.sun.mail.smtp.SMTPTransport;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class CN2IMAPMain {

    private Session imapSession;
    private Session smtpSession;
    private SMTPTransport smtpTransport;
    private Folder imapFolder;

    public CN2IMAPMain() {
    }

    private static class MailComparator implements Comparator<Message> {

        @Override
        public int compare(Message o1, Message o2) {
            try {
                if (o1.getReceivedDate().before(o2.getReceivedDate())) return 1;
                else return -1;
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        String user = args[0];
        String password = args[1];
        String host = args[2];

        CN2IMAPMain main = new CN2IMAPMain();
        main.login(host, user, password);

        // Before sending mail
        Arrays.stream(main.getMessages()).sorted(new MailComparator()).limit(10).forEach(CN2IMAPMain::messageOut);
        main.sendMessage(user, user, "CN2 Sample Mail", "This is a sample mail");
        // After sending mail
        Arrays.stream(main.getMessages()).sorted(new MailComparator()).limit(10).forEach(CN2IMAPMain::messageOut);

        main.close();
    }

    private static void messageOut(Message mail) {
        try {
            System.out.printf("From : %s%nSubject : %s%n%n", Arrays.toString(mail.getFrom()), mail.getSubject());
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void login(String host, String username, String password)
            throws Exception {
        URLName url = new URLName("imaps", host, 993, "INBOX", username, password);
        Properties props = System.getProperties();

        if (imapSession == null) imapSession = Session.getInstance(props, null);
        Store store = imapSession.getStore(url);
        store.connect();
        imapFolder = store.getFolder(url);

        imapFolder.open(Folder.READ_WRITE);

        props.put("mail.smtp.host", host);
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.port", "465");
        smtpSession = Session.getInstance(props, null);
        smtpTransport = (SMTPTransport) smtpSession.getTransport("smtp");
        smtpTransport.connect(host, username, password);
    }

    public void sendMessage(String recipient, String from, String subject, String content) throws MessagingException {
        Message msg = new MimeMessage(smtpSession);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        msg.setSubject(subject);
        msg.setText(content);
        msg.setSentDate(new Date());
        smtpTransport.sendMessage(msg, msg.getAllRecipients());
        smtpTransport.close();
    }

    public void close() throws MessagingException {
        smtpTransport.close();
        imapFolder.close(true);
    }

    public Message[] getMessages() throws MessagingException {
        return imapFolder.getMessages();
    }

}

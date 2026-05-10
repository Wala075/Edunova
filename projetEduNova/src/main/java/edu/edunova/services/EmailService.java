package edu.edunova.services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.util.ByteArrayDataSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;


public class EmailService {

    private final Properties config;

    public EmailService() {
        this.config = chargerConfig();
    }

    private Properties chargerConfig() {
        Properties props = new Properties();

        // 1. Essayer de charger depuis le fichier .env à la racine du projet
        File envFile = new File(System.getProperty("user.dir"), ".env");
        if (envFile.exists()) {
            try (InputStream is = Files.newInputStream(envFile.toPath())) {
                Properties envProps = new Properties();
                envProps.load(is);
                // Mapper les clés .env vers les clés attendues
                mapEnvToProps(envProps, props);
                return props;
            } catch (IOException e) {
                System.err.println("Avertissement: impossible de lire .env, fallback sur email.properties");
            }
        }

        // 2. Fallback sur email.properties dans resources
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("email.properties")) {
            if (is == null) {
                throw new RuntimeException("Ni .env ni email.properties trouvé. Créez un fichier .env à la racine du projet.");
            }
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lecture email.properties: " + e.getMessage(), e);
        }
        return props;
    }

    private void mapEnvToProps(Properties env, Properties props) {
        props.setProperty("smtp.host",       env.getProperty("SMTP_HOST", "smtp.gmail.com"));
        props.setProperty("smtp.port",       env.getProperty("SMTP_PORT", "587"));
        props.setProperty("smtp.username",   env.getProperty("SMTP_USERNAME", ""));
        props.setProperty("smtp.password",   env.getProperty("SMTP_PASSWORD", ""));
        props.setProperty("smtp.from.name",  env.getProperty("SMTP_FROM_NAME", "EduNova"));
        props.setProperty("smtp.from.email", env.getProperty("SMTP_FROM_EMAIL", env.getProperty("SMTP_USERNAME", "")));
    }

    public void envoyer(String destinataire, String sujet, String corpsHtml) throws Exception {
        envoyerInterne(destinataire, sujet, corpsHtml, null);
    }

    public void envoyerAvecPDF(String destinataire, String sujet, String corpsHtml, File pdfFile) throws Exception {
        if (pdfFile == null || !pdfFile.exists()) {
            throw new IllegalArgumentException("Fichier PDF introuvable : " + pdfFile);
        }
        envoyerInterne(destinataire, sujet, corpsHtml, pdfFile);
    }

    private void envoyerInterne(String destinataire, String sujet, String corpsHtml, File pdf) throws Exception {
        String host      = config.getProperty("smtp.host", "smtp.gmail.com");
        String port      = config.getProperty("smtp.port", "587");
        String username  = config.getProperty("smtp.username");
        String password  = config.getProperty("smtp.password");
        String fromEmail = config.getProperty("smtp.from.email", username);
        String fromName  = config.getProperty("smtp.from.name", "EduNova");

        if (password == null || password.isEmpty() || password.contains("PASTE_YOUR_APP_PASSWORD")) {
            throw new RuntimeException("Mot de passe SMTP manquant. Edite resources/email.properties");
        }

        // Configuration SMTP
        Properties smtpProps = new Properties();
        smtpProps.put("mail.smtp.auth", "true");
        smtpProps.put("mail.smtp.starttls.enable", "true");
        smtpProps.put("mail.smtp.host", host);
        smtpProps.put("mail.smtp.port", port);
        smtpProps.put("mail.smtp.ssl.trust", host);

        Session session = Session.getInstance(smtpProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        // Construction du message
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail, fromName, "UTF-8"));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(destinataire));
        message.setSubject(sujet, "UTF-8");

        if (pdf == null) {
            // Email simple HTML
            message.setContent(corpsHtml, "text/html; charset=UTF-8");
        } else {
            // Email multipart avec pièce jointe PDF
            MimeMultipart multipart = new MimeMultipart();

            // Partie HTML
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(corpsHtml, "text/html; charset=UTF-8");
            multipart.addBodyPart(htmlPart);

            // Partie PDF
            MimeBodyPart pdfPart = new MimeBodyPart();
            byte[] pdfBytes = Files.readAllBytes(pdf.toPath());
            DataSource ds = new ByteArrayDataSource(pdfBytes, "application/pdf");
            pdfPart.setDataHandler(new DataHandler(ds));
            pdfPart.setFileName(pdf.getName());
            multipart.addBodyPart(pdfPart);

            message.setContent(multipart);
        }

        Transport.send(message);
    }
}

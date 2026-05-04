package edu.edunova.notifications;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.edunova.utils.ConfigLoader;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Client minimaliste pour l'API transactionnelle Brevo (ex-Sendinblue).
 * https://developers.brevo.com/reference/sendtransacemail
 *
 * Configuration .env :
 *   BREVO_API_KEY=xkeysib-...
 *   BREVO_SENDER_EMAIL=no-reply@tondomaine.com
 *   BREVO_SENDER_NAME=EduNova
 */
public class BrevoService {

    private static final String ENDPOINT = "https://api.brevo.com/v3/smtp/email";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient http = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private final Gson gson = new Gson();

    /**
     * Résultat d'un envoi d'email.
     */
    public static class SendResult {
        public final boolean success;
        public final String messageId;     // null si échec
        public final String errorMessage;  // null si succès

        private SendResult(boolean s, String id, String err) {
            this.success = s; this.messageId = id; this.errorMessage = err;
        }

        public static SendResult ok(String id)    { return new SendResult(true, id, null); }
        public static SendResult ko(String err)   { return new SendResult(false, null, err); }
    }

    /**
     * Envoie un email transactionnel HTML.
     *
     * @param toEmail   destinataire
     * @param toName    nom (peut être null)
     * @param subject   sujet
     * @param htmlBody  corps HTML
     * @return SendResult avec success=true/false et messageId ou errorMessage
     */
    public SendResult sendEmail(String toEmail, String toName,
                                String subject, String htmlBody) {
        String apiKey = ConfigLoader.get("BREVO_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            return SendResult.ko("BREVO_API_KEY non configurée dans le .env");
        }
        String senderEmail = ConfigLoader.get("BREVO_SENDER_EMAIL");
        if (senderEmail == null || senderEmail.isBlank()) {
            return SendResult.ko("BREVO_SENDER_EMAIL non configuré dans le .env");
        }
        String senderName = ConfigLoader.get("BREVO_SENDER_NAME", "EduNova");

        if (toEmail == null || toEmail.isBlank()) {
            return SendResult.ko("Destinataire vide");
        }

        // ---- Body JSON ----
        JsonObject sender = new JsonObject();
        sender.addProperty("name", senderName);
        sender.addProperty("email", senderEmail);

        JsonObject toObj = new JsonObject();
        toObj.addProperty("email", toEmail);
        if (toName != null && !toName.isBlank()) toObj.addProperty("name", toName);

        JsonArray toArr = new JsonArray();
        toArr.add(toObj);

        JsonObject body = new JsonObject();
        body.add("sender", sender);
        body.add("to", toArr);
        body.addProperty("subject", subject);
        body.addProperty("htmlContent", htmlBody);

        Request req = new Request.Builder()
                .url(ENDPOINT)
                .header("api-key", apiKey)
                .header("accept", "application/json")
                .post(RequestBody.create(gson.toJson(body), JSON))
                .build();

        try (Response resp = http.newCall(req).execute()) {
            String raw = resp.body() == null ? "" : resp.body().string();
            if (!resp.isSuccessful()) {
                return SendResult.ko("HTTP " + resp.code() + " : " + extractError(raw));
            }
            // Réponse contient { "messageId": "<...>" }
            String msgId = "";
            try {
                JsonObject json = JsonParser.parseString(raw).getAsJsonObject();
                if (json.has("messageId")) msgId = json.get("messageId").getAsString();
            } catch (Exception ignore) {}
            return SendResult.ok(msgId);
        } catch (IOException e) {
            return SendResult.ko("IO : " + e.getMessage());
        }
    }

    private String extractError(String raw) {
        try {
            JsonObject obj = JsonParser.parseString(raw).getAsJsonObject();
            if (obj.has("message")) return obj.get("message").getAsString();
            if (obj.has("error"))   return obj.get("error").getAsString();
        } catch (Exception ignore) {}
        return raw.length() > 300 ? raw.substring(0, 300) + "..." : raw;
    }
}

package edunova.connexion.tools;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class HCaptchaServer {

    private static final int PORT = 7654;
    private HttpServer server;
    private final CountDownLatch latch =
            new CountDownLatch(1);
    private final AtomicReference<String> tokenRef =
            new AtomicReference<>(null);

    // ── Démarrer le serveur et attendre le token ──────────────────
    public String attendreToken(int timeoutSec)
            throws Exception {

        server = HttpServer.create(
                new InetSocketAddress("localhost", PORT), 0);

        // Route : /callback?token=XXX
        server.createContext("/callback", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String token = null;
            if (query != null &&
                    query.startsWith("token=")) {
                token = java.net.URLDecoder.decode(
                        query.substring(6),
                        StandardCharsets.UTF_8);
            }

            // Réponse HTML de confirmation
            String html =
                    "<!DOCTYPE html><html><head>" +
                            "<meta charset='UTF-8'/>" +
                            "<style>" +
                            "body{font-family:system-ui;display:flex;" +
                            "align-items:center;justify-content:center;" +
                            "min-height:100vh;background:#f0fdf4;margin:0;}" +
                            ".box{background:white;border-radius:16px;" +
                            "padding:40px;text-align:center;" +
                            "box-shadow:0 4px 24px rgba(0,0,0,0.1);}" +
                            ".icon{font-size:64px;margin-bottom:16px;}" +
                            "h2{color:#16a34a;font-size:22px;}" +
                            "p{color:#64748b;font-size:14px;}" +
                            "</style></head><body>" +
                            "<div class='box'>" +
                            "<div class='icon'>✅</div>" +
                            "<h2>Vérification réussie !</h2>" +
                            "<p>Vous pouvez fermer cette fenêtre<br/>" +
                            "et retourner à EduNova.</p>" +
                            "</div></body></html>";

            byte[] bytes = html.getBytes(
                    StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add(
                    "Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200,
                    bytes.length);
            try (OutputStream os =
                         exchange.getResponseBody()) {
                os.write(bytes);
            }

            if (token != null && !token.isEmpty()) {
                tokenRef.set(token);
                latch.countDown();
            }
        });

        // Route : /captcha → page HTML hCaptcha
        server.createContext("/captcha", exchange -> {
            String html = getCaptchaHtml();
            byte[] bytes = html.getBytes(
                    StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add(
                    "Content-Type",
                    "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200,
                    bytes.length);
            try (OutputStream os =
                         exchange.getResponseBody()) {
                os.write(bytes);
            }
        });

        server.start();

        // Attendre le token (timeout)
        boolean recu = latch.await(
                timeoutSec, TimeUnit.SECONDS);
        arreter();

        return recu ? tokenRef.get() : null;
    }

    public void arreter() {
        if (server != null) {
            server.stop(0);
        }
    }

    // ── Page hCaptcha servie localement ──────────────────────────
    private String getCaptchaHtml() {
        return "<!DOCTYPE html>" +
                "<html lang='fr'><head>" +
                "<meta charset='UTF-8'/>" +
                "<title>EduNova — Vérification</title>" +
                "<script src='https://js.hcaptcha.com/1/api.js'" +
                " async defer></script>" +
                "<style>" +
                "*{margin:0;padding:0;box-sizing:border-box;}" +
                "body{background:#f1f5f9;display:flex;" +
                "align-items:center;justify-content:center;" +
                "min-height:100vh;" +
                "font-family:system-ui,-apple-system,sans-serif;}" +
                ".card{background:white;border-radius:20px;" +
                "padding:40px 36px;text-align:center;" +
                "box-shadow:0 8px 32px rgba(0,0,0,0.12);" +
                "max-width:400px;width:92%;}" +
                ".logo{width:72px;height:72px;" +
                "background:linear-gradient(135deg,#7c3aed,#4f46e5);" +
                "border-radius:50%;display:flex;" +
                "align-items:center;justify-content:center;" +
                "margin:0 auto 20px;font-size:36px;}" +
                "h2{font-size:22px;color:#1e293b;" +
                "margin-bottom:8px;font-weight:700;}" +
                ".sub{font-size:13px;color:#64748b;" +
                "margin-bottom:28px;line-height:1.6;}" +
                ".hcap{display:flex;justify-content:center;" +
                "margin-bottom:24px;}" +
                ".btn{width:100%;padding:14px;" +
                "background:linear-gradient(135deg,#7c3aed,#4f46e5);" +
                "color:white;border:none;border-radius:12px;" +
                "font-size:15px;font-weight:700;cursor:pointer;" +
                "transition:opacity .2s,transform .1s;}" +
                ".btn:hover:not(:disabled){opacity:.9;" +
                "transform:translateY(-1px);}" +
                ".btn:disabled{background:#c4b5fd;cursor:not-allowed;}" +
                ".status{margin-top:14px;font-size:13px;" +
                "min-height:20px;font-weight:500;}" +
                ".ok{color:#16a34a;}.err{color:#dc2626;}" +
                ".sep{height:1px;background:#e2e8f0;margin:20px 0;}" +
                ".footer{font-size:11px;color:#94a3b8;" +
                "display:flex;align-items:center;" +
                "justify-content:center;gap:5px;}" +
                "</style></head><body>" +
                "<div class='card'>" +
                "<div class='logo'>🛡️</div>" +
                "<h2>Vérification de sécurité</h2>" +
                "<p class='sub'>Confirmez que vous êtes humain<br/>" +
                "pour accéder à votre espace EduNova</p>" +
                "<div class='hcap'>" +
                "<div class='h-captcha'" +
                " data-sitekey='e5dc64c4-b178-446a-9116-e1fc0cb40780'" +
                " data-callback='onSuccess'" +
                " data-expired-callback='onExpired'" +
                " data-error-callback='onError'" +
                " data-theme='light'>" +
                "</div></div>" +
                "<div class='sep'></div>" +
                "<button class='btn' id='btn'" +
                " onclick='valider()' disabled>" +
                "✓ &nbsp;Valider et continuer</button>" +
                "<div class='status' id='st'></div>" +
                "<div class='sep'></div>" +
                "<div class='footer'>" +
                "<span>🔒</span>" +
                "<span>Protégé par hCaptcha</span>" +
                "</div></div>" +
                "<script>" +
                "var tok=null;" +
                "function onSuccess(t){" +
                "tok=t;" +
                "document.getElementById('btn')" +
                ".removeAttribute('disabled');" +
                "var s=document.getElementById('st');" +
                "s.className='status ok';" +
                "s.textContent='✅ Vérification réussie !';}" +
                "function onExpired(){" +
                "tok=null;" +
                "document.getElementById('btn')" +
                ".setAttribute('disabled','true');" +
                "var s=document.getElementById('st');" +
                "s.className='status err';" +
                "s.textContent='⚠ Expirée, réessayez.';}" +
                "function onError(){" +
                "var s=document.getElementById('st');" +
                "s.className='status err';" +
                "s.textContent='❌ Erreur réseau.';}" +
                "function valider(){" +
                "if(!tok)return;" +
                "var s=document.getElementById('st');" +
                "s.className='status ok';" +
                "s.textContent='⏳ Vérification...';" +
                "window.location.href=" +
                "'http://localhost:7654/callback?token='" +
                "+encodeURIComponent(tok);}" +
                "</script></body></html>";
    }
}

# Google OAuth2 Testing Guide
**Date**: May 9, 2026  
**Status**: ✅ READY FOR TESTING

---

## 🧪 Testing the Google OAuth2 Fix

### Prerequisites:
- Application built successfully
- Google OAuth2 credentials configured
- Internet connection available
- Ports 8888-8891 available (or dynamic port)

---

## 📋 Test Scenario 1: Normal Port (8888)

### Setup:
1. Ensure port 8888 is available
2. Start the application
3. Navigate to login page

### Steps:
1. Click "Continuer avec Google" (Continue with Google)
2. Verify OAuth2 window opens
3. Check console logs for:
   ```
   GoogleOAuth2WindowController: Initialisation
   GoogleOAuth2WindowController: Serveur HTTP créé sur le port 8888
   GoogleOAuth2WindowController: Serveur HTTP démarré sur le port 8888
   GoogleOAuth2Service: Port de callback défini à 8888
   GoogleOAuth2WindowController: Chargement URL: https://accounts.google.com/...
   ```
4. Log in with Google account
5. Verify redirect to dashboard
6. Check console logs for:
   ```
   GoogleOAuth2WindowController: Callback reçu sur le port 8888
   GoogleOAuth2WindowController: Code extrait
   GoogleOAuth2WindowController: Code d'autorisation reçu
   GoogleOAuth2Service: Échange du code...
   GoogleOAuth2Service: Code de réponse token: 200
   GoogleOAuth2Service: Token obtenu
   GoogleOAuth2Service: Code de réponse userinfo: 200
   GoogleOAuth2Service: Infos utilisateur obtenues - Email: [your-email]
   ```

### Expected Result:
- ✅ OAuth2 window opens
- ✅ Google login page displays
- ✅ User can log in
- ✅ Redirected to dashboard
- ✅ User email displayed in dashboard

---

## 📋 Test Scenario 2: Dynamic Port (8889-8891)

### Setup:
1. Occupy port 8888 (e.g., run another application on port 8888)
2. Ensure ports 8889-8891 are available
3. Start the application

### Steps:
1. Click "Continuer avec Google" (Continue with Google)
2. Verify OAuth2 window opens
3. Check console logs for:
   ```
   GoogleOAuth2WindowController: Port 8888 occupé, tentative 1
   GoogleOAuth2WindowController: Port 8889 occupé, tentative 2
   GoogleOAuth2WindowController: Port 8890 occupé, tentative 3
   GoogleOAuth2WindowController: Port 8891 occupé, tentative 4
   GoogleOAuth2WindowController: Tous les ports occupés, utilisation d'un port dynamique
   GoogleOAuth2WindowController: Port dynamique assigné: [port]
   GoogleOAuth2Service: Port de callback défini à [port]
   ```
4. Log in with Google account
5. Verify redirect to dashboard
6. Check console logs for successful token exchange

### Expected Result:
- ✅ Application uses dynamic port
- ✅ OAuth2 window opens
- ✅ Google login page displays
- ✅ User can log in
- ✅ Redirected to dashboard
- ✅ Token exchange succeeds with dynamic port

---

## 📋 Test Scenario 3: All Ports Occupied

### Setup:
1. Occupy ports 8888-8891 (e.g., run multiple applications)
2. Start the application

### Steps:
1. Click "Continuer avec Google" (Continue with Google)
2. Verify OAuth2 window opens
3. Check console logs for:
   ```
   GoogleOAuth2WindowController: Tous les ports occupés, utilisation d'un port dynamique
   GoogleOAuth2WindowController: Port dynamique assigné: [random-port]
   GoogleOAuth2Service: Port de callback défini à [random-port]
   ```
4. Log in with Google account
5. Verify redirect to dashboard

### Expected Result:
- ✅ Application uses OS-assigned dynamic port
- ✅ OAuth2 window opens
- ✅ Google login page displays
- ✅ User can log in
- ✅ Redirected to dashboard
- ✅ Token exchange succeeds with dynamic port

---

## 📋 Test Scenario 4: Multiple Logins

### Setup:
1. Start the application
2. Complete first Google login

### Steps:
1. Log out from dashboard
2. Click "Continuer avec Google" again
3. Log in with same or different Google account
4. Verify redirect to dashboard

### Expected Result:
- ✅ First login succeeds
- ✅ Second login succeeds
- ✅ No port conflicts
- ✅ Both users logged in correctly

---

## 📋 Test Scenario 5: Error Handling

### Setup:
1. Start the application
2. Click "Continuer avec Google"

### Steps:
1. Close the OAuth2 window without logging in
2. Verify application returns to login page
3. Try again and complete login

### Expected Result:
- ✅ Closing window doesn't crash application
- ✅ Can retry login
- ✅ Second attempt succeeds

---

## 🔍 Console Log Verification

### Expected Log Sequence:

```
GoogleOAuth2WindowController: Initialisation
GoogleOAuth2WindowController: Port 8888 occupé, tentative 1
GoogleOAuth2WindowController: Port 8889 occupé, tentative 2
GoogleOAuth2WindowController: Port 8890 occupé, tentative 3
GoogleOAuth2WindowController: Port 8891 occupé, tentative 4
GoogleOAuth2WindowController: Tous les ports occupés, utilisation d'un port dynamique
GoogleOAuth2WindowController: Port dynamique assigné: 59062
GoogleOAuth2WindowController: Serveur HTTP démarré sur le port 59062
GoogleOAuth2Service: Port de callback défini à 59062
GoogleOAuth2WindowController: Chargement URL: https://accounts.google.com/o/oauth2/v2/auth?client_id=...&redirect_uri=http%3A%2F%2Flocalhost%3A59062%2FCallback&...
[User logs in with Google]
GoogleOAuth2WindowController: Callback reçu sur le port 59062
GoogleOAuth2WindowController: Code extrait
GoogleOAuth2WindowController: Code d'autorisation reçu
GoogleOAuth2Service: Échange du code...
GoogleOAuth2Service: Code de réponse token: 200
GoogleOAuth2Service: Token obtenu
GoogleOAuth2Service: Code de réponse userinfo: 200
GoogleOAuth2Service: Infos utilisateur obtenues - Email: user@example.com
GoogleOAuth2WindowController: Serveur HTTP arrêté
LoginController: Traitement du code Google OAuth2
[User redirected to dashboard]
```

### Key Indicators:
- ✅ Port assignment (8888, 8889, 8890, 8891, or dynamic)
- ✅ Service port notification: `Port de callback défini à [port]`
- ✅ Redirect URI includes correct port
- ✅ Token response code: 200 (not 400)
- ✅ User info retrieved successfully
- ✅ No "redirect_uri_mismatch" error

---

## ❌ Troubleshooting

### Issue: "redirect_uri_mismatch" Error

**Symptoms**:
```
GoogleOAuth2Service: Code de réponse token: 400
GoogleOAuth2Service: Réponse token: {
  "error": "redirect_uri_mismatch",
  "error_description": "Bad Request"
}
```

**Causes**:
1. Port not passed to service
2. Service using wrong port
3. Google credentials not updated

**Solutions**:
1. Verify `GoogleOAuth2Service.setCallbackPort()` is called
2. Check console logs for port assignment
3. Verify Google OAuth2 credentials are correct
4. Restart application

---

### Issue: OAuth2 Window Doesn't Open

**Symptoms**:
- Click "Continuer avec Google" but nothing happens
- No OAuth2 window appears

**Causes**:
1. Port binding failed
2. Server didn't start
3. WebView issue

**Solutions**:
1. Check console logs for errors
2. Verify ports are available
3. Restart application
4. Check internet connection

---

### Issue: Redirect to Dashboard Doesn't Happen

**Symptoms**:
- OAuth2 window shows success message
- But not redirected to dashboard
- Still on login page

**Causes**:
1. Callback not received
2. Code extraction failed
3. Token exchange failed

**Solutions**:
1. Check console logs for errors
2. Verify callback is received
3. Check token exchange response
4. Verify user info retrieval

---

## ✅ Success Criteria

### All Tests Pass When:
- ✅ OAuth2 window opens
- ✅ Google login page displays
- ✅ User can log in
- ✅ Callback is received on correct port
- ✅ Token exchange succeeds (code 200)
- ✅ User info is retrieved
- ✅ User is redirected to dashboard
- ✅ User email is displayed
- ✅ No "redirect_uri_mismatch" error
- ✅ Works with fixed and dynamic ports

---

## 📊 Test Results Template

```
Test Date: [Date]
Tester: [Name]
Application Version: [Version]

Test Scenario 1 (Port 8888): [PASS/FAIL]
Test Scenario 2 (Dynamic Port): [PASS/FAIL]
Test Scenario 3 (All Ports Occupied): [PASS/FAIL]
Test Scenario 4 (Multiple Logins): [PASS/FAIL]
Test Scenario 5 (Error Handling): [PASS/FAIL]

Overall Result: [PASS/FAIL]

Notes:
[Any issues or observations]
```

---

## 🎯 Next Steps

1. **Run all test scenarios**
2. **Document results**
3. **Report any issues**
4. **Deploy when all tests pass**

---

**Last Updated**: May 9, 2026  
**Status**: ✅ READY FOR TESTING

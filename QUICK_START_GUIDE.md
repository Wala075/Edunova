# EduNova Application - Quick Start Guide

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- JavaFX 21 runtime
- MySQL database
- Google OAuth2 credentials (optional, for OAuth2 login)

### Running the Application

1. **Build the application**:
   ```bash
   mvn clean package -DskipTests
   ```

2. **Run the application**:
   ```bash
   java -jar target/Login-1.0-SNAPSHOT.jar
   ```

3. **Application will open in light mode** ✅

---

## 🎨 Theme Management

### Default Theme
- **Light Mode** (default on startup)
- Clean, bright interface
- Easy on the eyes during daytime

### Switching Themes
1. Click the **theme toggle button** in the dashboard sidebar
2. Application switches between light and dark modes
3. Theme preference is maintained during session

### Theme Colors

**Light Mode**:
- Background: Light gray (`#f1f5f9`)
- Cards: White (`#ffffff`)
- Text: Dark gray (`#1e293b`)
- Borders: Light gray (`#e2e8f0`)

**Dark Mode**:
- Background: Very dark (`#0f0f1a`)
- Cards: Dark gray (`#1a1a2e`)
- Text: Light gray (`#e2e8f0`)
- Borders: Dark gray (`#2d2d4e`)

---

## 🔐 Login & Authentication

### Login Form
1. Enter your email address
2. Enter your password
3. Complete the CAPTCHA verification
4. Click "Se connecter" (Connect)

### Registration Form
1. Click "S'inscrire" (Sign up) on the login page
2. Fill in your details:
   - Name and first name
   - Email address
   - Phone number (with country code)
   - Password (minimum 6 characters)
   - Confirm password
3. Accept the terms and conditions
4. Click "Créer mon compte" (Create my account)

### Google OAuth2 Login
1. Click "Continuer avec Google" (Continue with Google)
2. A new window will open with Google login
3. Sign in with your Google account
4. You will be automatically redirected to the dashboard

**Note**: The application uses WebView for Google login (no external browser needed).

---

## 🔑 Password Management

### Forgot Password
1. Click "Mot de passe oublié ?" (Forgot password?) on the login page
2. Enter your email address
3. Click "Vérifier mon compte" (Verify my account)
4. If account is found, enter a new password
5. Confirm the new password
6. Click "Réinitialiser le mot de passe" (Reset password)

### Password Requirements
- Minimum 6 characters
- Should contain a mix of letters and numbers
- Avoid using common passwords

---

## 👥 User Management

### Dashboard Features
- **View Statistics**: See total users, admins, teachers, students
- **Recent Users**: View recently added users
- **Active Users**: See active and inactive users
- **Manage Users**: Add, edit, or delete users

### User Roles
- **Admin**: Full access to all features
- **Teacher**: Access to class and student management
- **Student**: Access to personal information and grades

---

## 📊 Risk Analysis

### What is Tracked
- Login attempts (successful and failed)
- User activities
- Security events
- Access patterns

### Viewing Risk Analysis
1. Go to the dashboard
2. Look for the "Risk Analysis" section
3. View statistics and trends
4. Generate reports if needed

---

## 🛠️ Troubleshooting

### Application Won't Start
**Problem**: Application crashes on startup  
**Solution**:
1. Verify Java 17+ is installed: `java -version`
2. Verify JavaFX 21 is available
3. Check that MySQL is running
4. Check application logs for errors

### Google OAuth2 Not Working
**Problem**: Google login fails or doesn't redirect  
**Solution**:
1. Verify Google OAuth2 credentials are configured
2. Check that ports 8888-8891 are available
3. Verify internet connection
4. Check firewall settings

### Database Connection Failed
**Problem**: Cannot connect to database  
**Solution**:
1. Verify MySQL is running
2. Check database credentials in configuration
3. Verify database exists and has required tables
4. Check network connectivity

### Theme Not Changing
**Problem**: Theme toggle doesn't work  
**Solution**:
1. Restart the application
2. Clear application cache
3. Check that theme toggle button is visible
4. Try clicking the button again

---

## 📱 Features Overview

### Login Page
- ✅ Email/password login
- ✅ Google OAuth2 login
- ✅ Registration form
- ✅ Forgot password link
- ✅ CAPTCHA verification
- ✅ Light mode by default

### Dashboard
- ✅ User statistics
- ✅ Recent users list
- ✅ Active/inactive users
- ✅ Theme toggle
- ✅ User management
- ✅ Risk analysis

### User Management
- ✅ Add new users
- ✅ Edit user information
- ✅ Delete users
- ✅ View user details
- ✅ Filter and search users

### Security
- ✅ Password encryption
- ✅ Session management
- ✅ Role-based access control
- ✅ Risk analysis tracking
- ✅ Secure OAuth2 integration

---

## 🔧 Configuration

### Database Configuration
Edit the database connection settings in the application configuration file:
```
Database Host: localhost
Database Port: 3306
Database Name: edunova
Username: root
Password: [your password]
```

### Google OAuth2 Configuration
Set up your Google OAuth2 credentials:
1. Go to Google Cloud Console
2. Create a new project
3. Enable OAuth2 API
4. Create OAuth2 credentials
5. Add redirect URI: `http://localhost:8888/Callback`
6. Copy Client ID and Client Secret
7. Configure in application

---

## 📞 Support

### Common Issues

**Q: Application opens in dark mode instead of light mode**  
A: This shouldn't happen. If it does, restart the application. Light mode is the default.

**Q: Password field doesn't show/hide password**  
A: The password field is intentionally simple for security. You cannot see the password as you type.

**Q: Google login opens in external browser**  
A: The application uses WebView for Google login. If it opens in external browser, check your browser settings.

**Q: Theme toggle button is not visible**  
A: The theme toggle button is in the dashboard sidebar. Make sure you're logged in and on the dashboard.

---

## 📚 Additional Resources

- **Application Status Report**: See `APPLICATION_STATUS_REPORT.md`
- **Verification Checklist**: See `VERIFICATION_CHECKLIST.md`
- **Context Summary**: See `CONTEXT_CONTINUATION_SUMMARY.md`

---

## ✅ Checklist Before Deployment

- [ ] Java 17+ installed
- [ ] JavaFX 21 runtime available
- [ ] MySQL database configured
- [ ] Google OAuth2 credentials configured (if using OAuth2)
- [ ] Application builds successfully
- [ ] Application starts without errors
- [ ] Light mode loads correctly
- [ ] Theme toggle works
- [ ] Login form works
- [ ] Registration form works
- [ ] Forgot password works
- [ ] Google OAuth2 works (if configured)
- [ ] User management works
- [ ] Risk analysis works

---

**Last Updated**: May 9, 2026  
**Status**: ✅ READY FOR USE

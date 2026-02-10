# 📧 Custom Email Template untuk Firebase

## Template Password Reset (Bahasa Indonesia)

### Subject:
```
[Luca] Reset Password Akun Anda
```

### Body:
```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f5f5f5;">
    <table width="100%" cellpadding="0" cellspacing="0" style="background-color: #f5f5f5; padding: 40px 0;">
        <tr>
            <td align="center">
                <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.08);">
                    
                    <!-- Header -->
                    <tr>
                        <td style="background: linear-gradient(135deg, #FDBF50 0%, #FDD835 100%); padding: 40px 30px; text-align: center;">
                            <h1 style="margin: 0; color: #000000; font-size: 32px; font-weight: 600;">Luca</h1>
                            <p style="margin: 8px 0 0 0; color: #000000; font-size: 14px; opacity: 0.8;">Shared Expense Tracker</p>
                        </td>
                    </tr>
                    
                    <!-- Content -->
                    <tr>
                        <td style="padding: 40px 30px;">
                            <h2 style="margin: 0 0 16px 0; color: #000000; font-size: 24px; font-weight: 600;">Reset Password Anda</h2>
                            
                            <p style="margin: 0 0 24px 0; color: #333333; font-size: 16px; line-height: 1.6;">
                                Halo,
                            </p>
                            
                            <p style="margin: 0 0 24px 0; color: #333333; font-size: 16px; line-height: 1.6;">
                                Kami menerima permintaan untuk reset password akun Luca Anda yang terdaftar dengan email <strong>%EMAIL%</strong>.
                            </p>
                            
                            <p style="margin: 0 0 32px 0; color: #333333; font-size: 16px; line-height: 1.6;">
                                Klik tombol di bawah ini untuk membuat password baru:
                            </p>
                            
                            <!-- CTA Button -->
                            <table width="100%" cellpadding="0" cellspacing="0">
                                <tr>
                                    <td align="center">
                                        <a href="%LINK%" style="display: inline-block; padding: 16px 48px; background-color: #FDBF50; color: #000000; text-decoration: none; border-radius: 25px; font-size: 16px; font-weight: 600; box-shadow: 0 4px 12px rgba(253,191,80,0.3);">
                                            Reset Password
                                        </a>
                                    </td>
                                </tr>
                            </table>
                            
                            <p style="margin: 32px 0 24px 0; color: #666666; font-size: 14px; line-height: 1.6;">
                                Atau copy dan paste link berikut ke browser Anda:
                            </p>
                            
                            <div style="background-color: #f8f8f8; padding: 16px; border-radius: 8px; word-break: break-all;">
                                <a href="%LINK%" style="color: #FDBF50; text-decoration: none; font-size: 14px;">%LINK%</a>
                            </div>
                            
                            <!-- Warning Box -->
                            <table width="100%" cellpadding="0" cellspacing="0" style="margin-top: 32px;">
                                <tr>
                                    <td style="background-color: #FFF9E6; border-left: 4px solid #FDBF50; padding: 16px; border-radius: 4px;">
                                        <p style="margin: 0; color: #666666; font-size: 14px; line-height: 1.6;">
                                            ⚠️ <strong>Penting:</strong> Link ini akan kadaluarsa dalam <strong>1 jam</strong>. Jika Anda tidak meminta reset password, abaikan email ini. Password Anda akan tetap aman.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    
                    <!-- Footer -->
                    <tr>
                        <td style="background-color: #f8f8f8; padding: 30px; text-align: center; border-top: 1px solid #e0e0e0;">
                            <p style="margin: 0 0 16px 0; color: #333333; font-size: 16px; font-weight: 600;">
                                Salam hangat,<br>Tim Luca
                            </p>
                            
                            <p style="margin: 0 0 24px 0; color: #666666; font-size: 14px;">
                                Butuh bantuan? Hubungi kami di <a href="mailto:support@luca.app" style="color: #FDBF50; text-decoration: none;">support@luca.app</a>
                            </p>
                            
                            <div style="border-top: 1px solid #e0e0e0; padding-top: 24px;">
                                <p style="margin: 0; color: #999999; font-size: 12px; line-height: 1.6;">
                                    © 2026 Luca - Shared Expense Tracker<br>
                                    Email ini dikirim secara otomatis. Mohon tidak membalas email ini.
                                </p>
                            </div>
                        </td>
                    </tr>
                    
                </table>
            </td>
        </tr>
    </table>
</body>
</html>
```

---

## Cara Apply Template ke Firebase:

### OPTION 1: Firebase Console (Simple - Text Only)

1. Firebase Console → **Authentication** → **Templates**
2. Klik **"Password reset"**
3. Klik icon **pensil (edit)**
4. Copy paste template berikut:

**Subject:**
```
[Luca] Reset Password Akun Anda
```

**Body (Text Version):**
```
Halo,

Kami menerima permintaan untuk reset password akun Luca Anda yang terdaftar dengan email %EMAIL%.

Klik link di bawah ini untuk membuat password baru:

%LINK%

⚠️ PENTING:
- Link ini akan kadaluarsa dalam 1 jam
- Jika Anda tidak meminta reset password, abaikan email ini
- Password Anda akan tetap aman

Salam hangat,
Tim Luca - Shared Expense Tracker

---
Butuh bantuan? Hubungi kami di support@luca.app

© 2026 Luca. Email ini dikirim secara otomatis.
Mohon tidak membalas email ini.
```

5. **Save**

### OPTION 2: Custom SMTP (Advanced - HTML Email)

Requires Blaze Plan + Custom SMTP setup.

**Benefits:**
- ✅ HTML formatting dengan branding Luca
- ✅ Styled button
- ✅ Professional appearance
- ✅ Higher deliverability

**Setup:**
1. Upgrade to Blaze Plan
2. Authentication → Templates → "Customize email sender"
3. Setup SMTP credentials
4. Upload HTML template
5. Configure DNS (SPF, DKIM)

---

## Template Variables:

Firebase akan otomatis replace:
- `%EMAIL%` → Email user yang request reset
- `%LINK%` → Password reset link dengan oobCode
- `%APP_NAME%` → Nama app dari public-facing name

---

## Testing Template:

1. Apply template di Firebase Console
2. Send test email: Authentication → Users → Send password reset email
3. Check email:
   - Subject sesuai?
   - Body readable?
   - Link berfungsi?
   - Branding jelas?

---

## Tips Agar Tidak Masuk Spam:

### ✅ DO:
- Use clear subject line dengan nama app
- Include unsubscribe link (untuk production)
- Personalize dengan %EMAIL%
- Add company/app info di footer
- Use professional language
- Include contact info
- Explain why user got this email

### ❌ DON'T:
- All caps subject (RESET PASSWORD)
- Too many exclamation marks!!!
- Misleading subject
- No unsubscribe option (production)
- Broken links
- Grammar errors
- Too many images
- Suspicious words (FREE, URGENT, WINNER)

---

## Expected Result:

### Before Custom Template:
```
Subject: Reset your password for luca-f40d7
Body: [Generic Firebase text in English]
Spam Score: Medium-High
```

### After Custom Template:
```
Subject: [Luca] Reset Password Akun Anda
Body: [Branded, professional, Indonesian]
Spam Score: Low
Deliverability: Higher ✅
```

---

## Template Bahasa Indonesia Benefits:

1. ✅ **User Familiar** - Target market Indonesia
2. ✅ **Professional** - Serious app appearance
3. ✅ **Clear Instructions** - Easy to understand
4. ✅ **Branded** - Luca identity strong
5. ✅ **Trust Building** - Detailed explanation
6. ✅ **Legal Compliance** - Contact info, disclaimer

---

## Next Steps:

1. ✅ Apply text template (5 menit)
2. ✅ Test send email
3. ✅ Verify deliverability
4. ✅ Collect user feedback
5. 🔜 Consider HTML template (production)
6. 🔜 Setup custom domain (long term)

---

## 📊 Monitoring:

Track email performance:
- Open rate
- Click rate (reset link)
- Spam complaints
- Bounce rate

Firebase Console → Authentication → Usage

---

**Template ini akan significantly improve email deliverability dan user experience!** 📧✨

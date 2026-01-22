# Ghost User Fix - Dokumentasi

## Masalah
User yang datanya sudah dihapus dari Firestore (database) tetapi masih ada di Firebase Authentication tidak bisa sign up lagi menggunakan email yang sama. Error yang muncul adalah "email already in use".

## Solusi
Implementasi handling untuk "Ghost User" - yaitu user yang ada di Firebase Authentication tetapi tidak ada datanya di Firestore.

## Perubahan yang Dilakukan

### 1. AuthRepository.kt - Fungsi signUpManual()

**Sebelum:**
```kotlin
suspend fun signUpManual(email: String, pass: String): Boolean {
    return try {
        auth.createUserWithEmailAndPassword(email, pass).await()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
```

**Sesudah:**
```kotlin
suspend fun signUpManual(email: String, pass: String): Boolean {
    return try {
        auth.createUserWithEmailAndPassword(email, pass).await()
        true
    } catch (e: Exception) {
        // Jika email sudah terdaftar, cek apakah user hantu
        if (e.message?.contains("already in use", true) == true ||
            e.message?.contains("email-already-in-use", true) == true) {
            try {
                // Coba sign in dengan kredensial yang sama
                val result = auth.signInWithEmailAndPassword(email, pass).await()
                val user = result.user
                
                if (user != null) {
                    // Cek apakah data ada di Firestore
                    val doc = db.collection("users").document(user.uid).get().await()
                    if (!doc.exists()) {
                        // User ada di Auth tapi tidak ada di DB (User Hantu)
                        // Biarkan user lanjut ke Fill Profile
                        return true
                    } else {
                        // User sudah lengkap, tidak bisa sign up lagi
                        auth.signOut()
                        return false
                    }
                } else {
                    return false
                }
            } catch (signInError: Exception) {
                // Jika sign in gagal (password salah), tetap return false
                signInError.printStackTrace()
                return false
            }
        }
        
        e.printStackTrace()
        false
    }
}
```

**Penjelasan:**
- Ketika `createUserWithEmailAndPassword` gagal karena email sudah ada, system akan mencoba sign in
- Jika sign in berhasil, cek apakah data user ada di Firestore
- Jika tidak ada data di Firestore (Ghost User), return `true` sehingga user bisa lanjut ke Fill Profile Screen
- Jika ada data lengkap, return `false` karena user sudah terdaftar

### 2. SignUpScreen.kt - Error Handling

**Update Toast Message:**
- Memberikan pesan yang lebih jelas untuk user jika sign up gagal
- Membedakan antara error teknis dan user yang sudah terdaftar lengkap

## Alur Aplikasi Setelah Fix

### Skenario 1: User Baru (Normal)
1. User mengisi email & password di Sign Up Screen
2. `signUpManual()` membuat akun baru di Firebase Auth
3. Return `true` → Navigasi ke Fill Profile Screen
4. User mengisi username & pilih avatar
5. Data disimpan ke Firestore via `updateProfile()`
6. Navigasi ke Home Screen

### Skenario 2: Ghost User (Data Terhapus dari Firestore)
1. User mengisi email & password di Sign Up Screen
2. `signUpManual()` mencoba create account → Gagal (email already exists)
3. System otomatis sign in dengan kredensial yang sama
4. Cek Firestore → Data tidak ada
5. Return `true` → Navigasi ke Fill Profile Screen
6. User mengisi ulang username & pilih avatar
7. Data disimpan ke Firestore via `updateProfile()` dengan `SetOptions.merge()`
8. Navigasi ke Home Screen

### Skenario 3: User Sudah Terdaftar Lengkap
1. User mengisi email & password di Sign Up Screen
2. `signUpManual()` mencoba create account → Gagal (email already exists)
3. System otomatis sign in dengan kredensial yang sama
4. Cek Firestore → Data ada dan lengkap
5. Sign out otomatis
6. Return `false` → Toast muncul: "Email sudah terdaftar..."
7. User diminta untuk login instead

### Skenario 4: Password Salah
1. User mengisi email yang sudah ada & password yang salah
2. `signUpManual()` mencoba create account → Gagal (email already exists)
3. System mencoba sign in → Gagal (password salah)
4. Return `false` → Toast muncul: "Email sudah terdaftar atau password salah..."
5. User diminta untuk login dengan password yang benar

## Testing

### Test Case 1: Ghost User Bisa Sign Up Lagi
**Pre-condition:**
- User dengan email `test@example.com` ada di Firebase Authentication
- User `test@example.com` TIDAK ada di Firestore collection `users`

**Steps:**
1. Buka aplikasi
2. Tap "Sign Up"
3. Masukkan email: `test@example.com`
4. Masukkan password yang sesuai dengan akun di Firebase Auth
5. Confirm password
6. Tap "Continue"

**Expected Result:**
- Toast muncul: "Akun berhasil dibuat!"
- Navigasi ke Fill Profile Screen
- Setelah isi profile, data tersimpan di Firestore
- Navigasi ke Home Screen

### Test Case 2: User Lengkap Tidak Bisa Sign Up
**Pre-condition:**
- User dengan email `existing@example.com` ada di Firebase Authentication
- User `existing@example.com` ADA di Firestore collection `users` dengan data lengkap

**Steps:**
1. Buka aplikasi
2. Tap "Sign Up"
3. Masukkan email: `existing@example.com`
4. Masukkan password yang sesuai
5. Confirm password
6. Tap "Continue"

**Expected Result:**
- Toast muncul: "Email sudah terdaftar atau password salah. Silakan gunakan Login jika sudah memiliki akun."
- Tetap di Sign Up Screen

## Catatan Penting

1. **SetOptions.merge()**: Fungsi `updateProfile()` menggunakan `SetOptions.merge()` yang akan membuat document baru jika tidak ada, atau update jika sudah ada.

2. **Security**: Pastikan Firestore Security Rules mengizinkan create/update document untuk authenticated users:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

3. **Cleanup**: Jika ingin benar-benar menghapus user, pastikan untuk menghapus dari:
   - Firebase Authentication (menggunakan Firebase Console atau Admin SDK)
   - Firestore collection `users`

## Troubleshooting

**Masalah:** Toast tetap menunjukkan "Email sudah terdaftar..." padahal data sudah dihapus dari Firestore

**Solusi:** 
- Pastikan data benar-benar terhapus dari Firestore collection `users`
- Check di Firebase Console → Firestore Database → users collection
- Pastikan document dengan UID yang sama tidak ada

**Masalah:** Error "User offline" saat updateProfile

**Solusi:**
- Pastikan user berhasil sign in/sign up sebelum masuk ke Fill Profile Screen
- Check `auth.currentUser` tidak null

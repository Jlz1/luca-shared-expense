# 📱 SearchBarModify Component - Panduan Penggunaan (UPDATED)

## ✨ Perubahan Utama

Komponen `SearchBarModify` sekarang memiliki **STATE INTERNAL** yang otomatis menghandle input keyboard.

**TIDAK PERLU** lagi mendeklarasikan state `searchQuery` di parent screen!

---

## 🔧 Parameter Yang Berubah

### ❌ SEBELUMNYA:
```kotlin
searchQuery: String = ""  // Harus pass state dari parent
```

### ✅ SEKARANG:
```kotlin
initialQuery: String = ""  // Hanya untuk nilai awal (optional)
```

---

## 📖 Cara Pemanggilan

### 1️⃣ Mode Read-Only (Hanya Clickable)
Digunakan untuk navigate ke halaman search saat diklik.

```kotlin
SearchBarModify(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .height(50.dp),
    placeholder = "Search",
    onSearchClick = { 
        // Navigate ke search screen
        navController.navigate("search_screen")
    },
    readOnly = true  // Default value
)
```

---

### 2️⃣ Mode Editable - Tanpa State di Parent (BARU!)
Langsung bisa menerima input keyboard tanpa deklarasi state!

```kotlin
// ✅ TIDAK PERLU: var searchQuery by remember { mutableStateOf("") }

SearchBarModify(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp)
        .height(50.dp),
    placeholder = "Search",
    onSearchQueryChange = { query ->
        // Callback ini dipanggil setiap user ketik
        println("User typing: $query")
        
        // Lakukan filtering atau search logic di sini
        filteredList = items.filter { it.contains(query, ignoreCase = true) }
    },
    readOnly = false  // PENTING: Set false agar bisa edit
)
```

---

### 3️⃣ Mode Editable - Dengan Initial Value
Jika ingin search bar memiliki nilai awal.

```kotlin
SearchBarModify(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .height(50.dp),
    placeholder = "Search items...",
    initialQuery = "Breakfast",  // Nilai awal
    onSearchQueryChange = { query ->
        // Handle search
        performSearch(query)
    },
    readOnly = false
)
```

---

### 4️⃣ Mode Editable - Dengan Database Label
Menampilkan label database yang sedang digunakan.

```kotlin
SearchBarModify(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .height(60.dp),  // Tinggi lebih besar untuk label
    placeholder = "Search products...",
    databaseLabel = "Database: Products",
    onSearchQueryChange = { query ->
        // Search from database
        searchFromDatabase(query)
    },
    readOnly = false
)
```

---

## 🔄 Migration Guide

### File: DetailedEventScreen.kt

#### ❌ SEBELUMNYA:
```kotlin
var searchQuery by remember { mutableStateOf("") }

SearchBarModify(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp)
        .height(50.dp),
    placeholder = "Search",
    searchQuery = searchQuery,  // Pass state
    onSearchQueryChange = { query ->
        searchQuery = query  // Update state
        println("Search: $query")
    },
    readOnly = false
)
```

#### ✅ SEKARANG (LEBIH SEDERHANA):
```kotlin
// Hapus deklarasi state di atas

SearchBarModify(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp)
        .height(50.dp),
    placeholder = "Search",
    onSearchQueryChange = { query ->
        // Langsung filter/search
        println("Search: $query")
    },
    readOnly = false
)
```

---

### File: NewEventScreen.kt

#### ❌ SEBELUMNYA:
```kotlin
SearchBarModify(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp),
    placeholder = "Search",
    searchQuery = "",  // Tidak ada state management
    onSearchQueryChange = { newQuery -> 
        searchQuery = newQuery  // Error: searchQuery tidak didefinisikan
    },
    readOnly = false
)
```

#### ✅ SEKARANG (OTOMATIS KERJA):
```kotlin
SearchBarModify(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp)
        .height(50.dp),
    placeholder = "Search",
    onSearchQueryChange = { query ->
        // Langsung bisa digunakan untuk filtering
        println("Search: $query")
    },
    readOnly = false
)
```

---

## 🎯 Keuntungan Perubahan Ini

1. ✅ **Lebih Sederhana**: Tidak perlu deklarasi state di setiap screen
2. ✅ **Keyboard Otomatis Muncul**: State internal menghandle input secara otomatis
3. ✅ **Konsisten**: Semua screen menggunakan cara yang sama
4. ✅ **Backward Compatible**: Screen lama masih bisa menggunakan cara lama (dengan initial value)

---

## 🐛 Troubleshooting

### Problem: Keyboard tidak muncul
**Solution**: Pastikan parameter `readOnly = false`

### Problem: Input tidak terlihat
**Solution**: Pastikan parent Modifier memiliki `.height()` yang cukup (min 50.dp)

### Problem: Callback tidak terpanggil
**Solution**: Cek apakah `onSearchQueryChange` sudah didefinisikan dengan benar

---

## 📝 Notes

- State internal menggunakan `remember { mutableStateOf(initialQuery) }`
- Setiap perubahan input akan memanggil callback `onSearchQueryChange`
- Jika perlu akses ke search query dari luar komponen, gunakan callback untuk menyimpannya di parent state (jika diperlukan)

---

**Updated**: January 2026
**Component**: `SearchBarModify` in `Components_Steven.kt`


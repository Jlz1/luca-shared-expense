# 🎯 Contoh Implementasi SearchBarModify - CARA BARU

## ✅ File: DetailedEventScreen.kt (SUDAH DIUPDATE)

```kotlin
@Composable
fun DetailedEventScreen() {
    // ❌ TIDAK PERLU LAGI: var searchQuery by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = { HeaderSection() },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 100.dp)
            ) {
                EventCard()
                Spacer(modifier = Modifier.height(16.dp))
                
                // ✅ CARA BARU: Langsung bisa ketik tanpa state di parent
                SearchBarModify(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .height(50.dp),
                    placeholder = "Search",
                    onSearchQueryChange = { query ->
                        // Filter atau search logic langsung di sini
                        println("Search: $query")
                    },
                    readOnly = false
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                ActivitySection()
            }
            
            // Bottom buttons...
            BottomActionArea(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            )
        }
    }
}
```

---

## ✅ File: NewEventScreen.kt (SUDAH DIUPDATE)

```kotlin
@Composable
fun NewEventScreen() {
    // ❌ TIDAK PERLU LAGI: var searchQuery by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = { HeaderSection() },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 100.dp)
            ) {
                EventCard()
                Spacer(modifier = Modifier.height(16.dp))

                // ✅ CARA BARU: State internal otomatis handle keyboard
                SearchBarModify(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .height(50.dp),
                    placeholder = "Search",
                    onSearchQueryChange = { query ->
                        println("Search: $query")
                    },
                    readOnly = false
                )

                Spacer(modifier = Modifier.height(32.dp))
                EmptyStateMessage()
            }

            BottomActionAreaNew()
        }
    }
}
```

---

## 📋 Contoh Kasus Penggunaan Lainnya

### 1. Search dengan Filtering Data Lokal

```kotlin
@Composable
fun MyScreen() {
    val allItems = remember { listOf("Apple", "Banana", "Cherry", "Date", "Elderberry") }
    var filteredItems by remember { mutableStateOf(allItems) }
    
    Column {
        // Search bar dengan filtering otomatis
        SearchBarModify(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(50.dp),
            placeholder = "Search fruits...",
            onSearchQueryChange = { query ->
                filteredItems = if (query.isEmpty()) {
                    allItems
                } else {
                    allItems.filter { it.contains(query, ignoreCase = true) }
                }
            },
            readOnly = false
        )
        
        // Display filtered items
        LazyColumn {
            items(filteredItems) { item ->
                Text(item, modifier = Modifier.padding(16.dp))
            }
        }
    }
}
```

---

### 2. Search dengan Debounce (API Call)

```kotlin
@Composable
fun SearchProductScreen(viewModel: ProductViewModel) {
    SearchBarModify(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(50.dp),
        placeholder = "Search products...",
        onSearchQueryChange = { query ->
            // ViewModel akan handle debounce dan API call
            viewModel.searchProducts(query)
        },
        readOnly = false
    )
    
    // Display search results from ViewModel
    LazyColumn {
        items(viewModel.searchResults.value) { product ->
            ProductCard(product)
        }
    }
}
```

---

### 3. Search dengan Initial Value

```kotlin
@Composable
fun FilteredListScreen(initialSearchTerm: String = "") {
    SearchBarModify(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(50.dp),
        placeholder = "Search...",
        initialQuery = initialSearchTerm, // Pre-filled dengan nilai awal
        onSearchQueryChange = { query ->
            // Filter list berdasarkan query
            performSearch(query)
        },
        readOnly = false
    )
}

// Usage:
FilteredListScreen(initialSearchTerm = "Breakfast")
```

---

### 4. Search dengan Database Label

```kotlin
@Composable
fun DatabaseSearchScreen() {
    var selectedDatabase by remember { mutableStateOf("Products") }
    
    Column {
        // Dropdown untuk pilih database
        DatabaseSelector(
            selectedDb = selectedDatabase,
            onDbSelected = { selectedDatabase = it }
        )
        
        // Search bar dengan label database
        SearchBarModify(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(60.dp), // Lebih tinggi untuk accommodate label
            placeholder = "Search in database...",
            databaseLabel = "Database: $selectedDatabase",
            onSearchQueryChange = { query ->
                searchInDatabase(selectedDatabase, query)
            },
            readOnly = false
        )
        
        // Display results...
    }
}
```

---

### 5. Read-Only Mode (Navigate to Search Screen)

```kotlin
@Composable
fun HomeScreen(navController: NavController) {
    SearchBarModify(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(50.dp),
        placeholder = "Search",
        onSearchClick = {
            // Navigate ke dedicated search screen
            navController.navigate("search_screen")
        },
        readOnly = true  // Default value, tidak perlu keyboard
    )
}
```

---

## 🎨 Styling Tips

### Custom Height untuk Berbagai Use Case

```kotlin
// Compact size
SearchBarModify(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .height(40.dp), // Kecil dan compact
    placeholder = "Quick search...",
    onSearchQueryChange = { /* ... */ },
    readOnly = false
)

// Standard size
SearchBarModify(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .height(50.dp), // Size standar
    placeholder = "Search...",
    onSearchQueryChange = { /* ... */ },
    readOnly = false
)

// Large size dengan label
SearchBarModify(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .height(70.dp), // Tinggi untuk label + input
    placeholder = "Search...",
    databaseLabel = "Database: Users",
    onSearchQueryChange = { /* ... */ },
    readOnly = false
)
```

---

## ⚠️ Common Mistakes

### ❌ JANGAN:
```kotlin
// JANGAN pass searchQuery parameter lagi!
var searchQuery by remember { mutableStateOf("") }
SearchBarModify(
    searchQuery = searchQuery, // ❌ Parameter ini sudah tidak ada!
    onSearchQueryChange = { searchQuery = it },
    readOnly = false
)
```

### ✅ LAKUKAN:
```kotlin
// Langsung gunakan, state otomatis handled di dalam komponen
SearchBarModify(
    onSearchQueryChange = { query ->
        // Gunakan query di sini
        println(query)
    },
    readOnly = false
)
```

---

## 🔍 How It Works Internally

Komponen `SearchBarModify` sekarang memiliki:

```kotlin
// State internal yang otomatis handle input
var internalSearchQuery by remember { mutableStateOf(initialQuery) }

// TextField yang update state internal
TextField(
    value = internalSearchQuery,
    onValueChange = { newQuery ->
        internalSearchQuery = newQuery  // Update internal state
        onSearchQueryChange(newQuery)   // Callback ke parent (optional)
    },
    // ... other parameters
)
```

Jadi keyboard otomatis muncul karena:
1. State `internalSearchQuery` di-manage oleh `remember { mutableStateOf() }`
2. Setiap ketikan user mengupdate state internal
3. Callback `onSearchQueryChange` dipanggil untuk parent bisa react (optional)

---

**Updated**: January 2026  
**Status**: ✅ Production Ready  
**Files Updated**: 
- `Components_Steven.kt`
- `DetailedEventScreen.kt`
- `NewEventScreen.kt`


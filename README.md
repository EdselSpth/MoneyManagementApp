# ⭐ Shiota Wallet (스마트 가계부)

**Shiota Wallet** adalah aplikasi manajemen keuangan pribadi modern (*Personal Finance, Expense & Multi-Wallet Tracker, and Smart Budgeting*) dengan estetika minimalis **Shadcn UI**, floating navigation bar bergaya Apple, dan sistem multi-tema (termasuk palet resmi **Aether Genshin Impact**). 

Aplikasi ini berjalan **100% Offline** menggunakan **SQLite lokal**, dioptimalkan untuk mata uang **Korean Won (KRW - ₩)**, serta mendukung platform **Android (Jetpack Compose)** dan **Desktop (Compose Multiplatform)**.

---

## ✨ Fitur Utama (Key Features)

### 1. 💳 Multi-Card & Digital Wallet Management
- **Interactive Card Carousel**: Tampilan horizontal kartu & dompet digital (T-Money, Toss, KakaoPay, Shinhan, Cash Wallet, dll.) dengan nomor kartu terenkripsi/masking, saldo real-time, dan status kartu utama (*Primary*).
- **Kelola Kartu & Dompet Lengkap**: Tambah, edit saldo awal, ubah nama/nomor kartu, hapus, dan atur kartu utama.
- **Custom Bank & Card Issuers**: Bebas menambah dan mengelola daftar penerbit bank atau e-wallet sendiri langsung dari menu Pengaturan (*Settings*).
- **Multi-Account Linking**: Setiap transaksi pemasukan atau pengeluaran langsung terhubung dan memotong/menambah saldo kartu atau dompet yang dipilih.

### 2. 🇰🇷 Korean Won Currency Engine (KRW - ₩)
- Seluruh kalkulasi nominal dan format tampilan dioptimalkan untuk mata uang Won Korea tanpa pecahan desimal (contoh: `₩1,250,000`, `+₩3,500,000`, `-₩45,000`).
- **Quick-Add Buttons**: Tombol instan penambahan nominal Won (`+₩10,000`, `+₩50,000`, `+₩100,000`, `+₩500,000`, `+₩1,000,000`) untuk input cepat tanpa repot mengetik banyak angka nol.

### 3. 💰 Tracking Pemasukan & Pengeluaran (Income & Expense Tracking)
- Pencatatan transaksi lengkap: Kategori pengeluaran/pemasukan, Nominal (₩), Tanggal, Catatan/Deskripsi (dengan teks *auto-truncate* rapi), dan Sumber Kartu/Dompet.
- Filter transaksi live berdasarkan tipe (Semua / Pemasukan / Pengeluaran), Kategori, dan Pencarian teks langsung.
- Ringkasan statistik otomatis: Total Saldo Semua Akun, Total Pemasukan Bulanan, dan Total Pengeluaran Bulanan.

### 4. 📊 Smart Budgeting (Aturan 50/30/20 & Category Caps)
- **Simulasi 50/30/20 Rule**: Membagi otomatis total pemasukan bulanan ke dalam 3 pilar finansial:
  - **Needs (50%)**: Kebutuhan pokok (Sewa kamar/월세, makanan, transportasi, tagihan).
  - **Wants (30%)**: Keinginan gaya hidup (Ngopi/카페, belanja/쇼핑, hobi, hiburan).
  - **Savings & Investments (20%)**: Tabungan masa depan & dana darurat.
  - *Rasio dapat disesuaikan secara dinamis sesuai kebutuhan.*
- **Category Monthly Limits**: Pengaturan batas maksimal pengeluaran per kategori dengan indikator visual progres (*Safe*, *Warning*, dan *Exceeded*).

### 5. 📈 Visual Analytics & Insights
- Diagram distribusi pengeluaran per kategori.
- Visualisasi perbandingan rasio Cashflow (Pemasukan vs Pengeluaran).
- Metrik **Savings Rate %** (*Tingkat Tabungan*).
- Peringkat pengeluaran tertinggi (*Top Expense Categories*).

### 6. 🎯 Savings Goals (Target Celengan Digital)
- Buat target tabungan spesifik (misal: *"Tiket Liburan ke Jeju"*, *"Dana Darurat 3 Bulan"*).
- Fitur setor tabungan (*Deposit Funds*) langsung memotong dari dompet/rekening aktif.
- Visual progress bar pencapaian target dan kalkulasi sisa nominal yang perlu dikumpulkan.

### 7. 🎨 Estetika Desain & Multi-Tema
- **Shadcn UI Minimalism**: Garis border tipis elegan, tipografi modern, badge rapi, dan transisi halus.
- **Apple Floating Dock Navigation**: Navbar melayang di bagian bawah layar dengan efek glassmorphism dan responsif saat berpindah menu.
- **Pilihan Tema Warna**:
  - ⚔️ **Aether Genshin Theme**: Palet warna hangat terinspirasi karakter Aether (`#FAF0D9` Alabaster, `#DCB37B` Wheat Gold, `#5E4A4B` Cocoa, `#332829` Espresso Charcoal).
  - 🌙 **Dark Zinc Theme**: Tema gelap pekat (*Zinc-950*) dengan aksen Cyan & Sky Blue.
  - ☀️ **Clean Slate Light Theme**: Tema terang minimalis (*Slate-50*) yang bersih dan nyaman di mata.
- **Custom Minimalist Icon**: Ikon aplikasi kombinasi siluet dompet dan bintang emas 6 titik.

### 8. 🔒 100% Offline & Privasi Terjaga
- Semua data tersimpan secara lokal di perangkat menggunakan **SQLite Database**.
- Tanpa pelacakan, tanpa iklan, dan tidak memerlukan koneksi internet.
- Fitur **Export & Import Backup (JSON)** untuk mencadangkan atau memindahkan data ke perangkat lain dengan aman.

---

## 🛠️ Tech Stack & Arsitektur

| Komponen | Teknologi |
|---|---|
| **Bahasa Pemrograman** | Kotlin 2.x |
| **Android UI** | Jetpack Compose, Material 3, Compose Foundation |
| **Desktop UI** | Jetpack Compose Multiplatform (Desktop) |
| **Database** | SQLite (Android `SQLiteOpenHelper` & Desktop SQLite JDBC) |
| **Asynchronous & State** | Kotlin Coroutines & StateFlow (Reactive UI updates) |
| **Serialization** | Kotlinx Serialization JSON |
| **Build System** | Gradle (Kotlin DSL) |

---

## 🚀 Cara Menjalankan & Membangun Project

Pastikan Anda memiliki JDK 17 atau yang lebih baru dan Android SDK yang terpasang.

### 📱 Android Application

1. **Jalankan ke Perangkat / Emulator via Terminal:**
   ```powershell
   # Compile APK Debug
   .\gradlew.bat :app:assembleDebug

   # Install langsung ke perangkat yang terhubung (via ADB)
   adb install -r app\build\outputs\apk\debug\app-debug.apk

   # Buka aplikasi di HP
   adb shell am start -n com.example.moneymanagement/.MainActivity
   ```
2. **Atau buka project di Android Studio**:
   - Buka direktori `d:\MoneyManagementApp`.
   - Pilih target perangkat dan klik tombol **Run** (`Shift + F10`).

### 💻 Desktop Application (Windows)

1. **Jalankan Aplikasi Desktop Langsung:**
   ```powershell
   .\gradlew.bat run
   ```
2. **Jalankan Unit Test Otomatis:**
   ```powershell
   .\gradlew.bat desktopTest
   ```
3. **Build Binary Installer Desktop (.exe / .msi):**
   ```powershell
   .\gradlew.bat packageDistributionForCurrentOS
   ```
   *File installer akan dibuat di folder `build/compose/binaries/main/`.*

---

## 📁 Struktur Direktori Project

```
MoneyManagementApp/
├── app/                                       # Modul Aplikasi Android
│   ├── build.gradle.kts                       # Konfigurasi dependensi Android & Compose
│   └── src/main/
│       ├── AndroidManifest.xml                # Android Manifest & Application metadata
│       ├── java/com/example/moneymanagement/
│       │   ├── MainActivity.kt                # Activity utama & Navigation host
│       │   ├── data/
│       │   │   ├── database/DatabaseHelper.kt # SQLite OpenHelper & skema database
│       │   │   ├── model/                     # Data class: Account, Transaction, Budget, Goal
│       │   │   └── repository/                # FinanceRepository (StateFlows & Business Logic)
│       │   ├── ui/
│       │   │   ├── components/                # AccountCardsCarousel, AppleFloatingNavBar, AppDialogs, ShadcnComponents
│       │   │   ├── screens/                   # DashboardScreen, TransactionsScreen, BudgetingScreen, AnalyticsScreen, SavingsGoalsScreen, SettingsScreen
│       │   │   └── theme/                     # ShadcnTheme, AppThemeMode (Aether, Dark Zinc, Light Slate)
│       │   └── util/                          # CurrencyFormatter (₩ KRW) & ColorUtils
│       └── res/                               # Drawable icons, logo minimalist bintang & wallet, strings
├── src/                                       # Modul Desktop Compose Multiplatform
│   ├── desktopMain/kotlin/                    # Entry point & UI layar desktop
│   └── desktopTest/kotlin/                    # Unit testing suite (FinanceAppTest.kt)
├── build.gradle.kts                           # Root Gradle build configuration
├── settings.gradle.kts                        # Settings Gradle & module definitions
└── README.md                                  # Dokumentasi aplikasi
```

---

## 📄 Lisensi & Kontribusi

Dibuat dengan ❤️ untuk pengelolaan keuangan yang rapi, transparan, dan tenang. Bebas digunakan dan dikembangkan untuk kebutuhan personal.

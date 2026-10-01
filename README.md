# ₩ Korean Won Money Management App (스마트 가계부)

Aplikasi manajemen keuangan personal modern (*Personal Finance, Expense & Income Tracker, and Smart Budgeting*) berbasis **Kotlin Compose Multiplatform**, dirancang dengan estetika minimalis **Shadcn UI (Zinc/Slate)**, penyimpanan data lokal **100% Offline SQLite**, serta menggunakan mata uang **Korean Won (KRW - ₩)**.

---

## ✨ Fitur Utama (Features)

### 1. 🇰🇷 Korean Won Currency Engine (KRW - ₩)
- Seluruh kalkulasi nominal dan format tampilan dioptimalkan untuk mata uang Won Korea tanpa pecahan sen (contoh: `₩1,250,000`, `+₩3,500,000`, `-₩45,000`).
- **Quick-Add Buttons**: Tombol instan penambahan nominal Won (`+₩10,000`, `+₩50,000`, `+₩100,000`, `+₩500,000`, `+₩1,000,000`) untuk input super cepat.

### 2. 💰 Tracking Pemasukan & Pengeluaran (Income & Expense Tracking)
- Pencatatan transaksi lengkap: Kategori, Nominal (₩), Tanggal, Catatan/Deskripsi, dan Metode Pembayaran (`카드 Card`, `현금 Cash`, `계좌이체 Bank Transfer`, `간편결제 KakaoPay/Toss`).
- Filter canggih berdasarkan tipe (Semua / Pemasukan / Pengeluaran), Kategori, dan Pencarian teks secara live.
- Indikator ringkasan statistik langsung (Total Pemasukan, Total Pengeluaran, Saldo Bersih).

### 3. 📊 Smart Budgeting & Alokasi Income (Aturan 50/30/20 & Kategori)
- **Simulasi 50/30/20 Rule**: Membagi otomatis total income bulanan ke dalam 3 pilar:
  - **Needs (50%)**: Kebutuhan pokok (Sewa kamar/월세, makan, transportasi, utilitas).
  - **Wants (30%)**: Keinginan (Ngopi/카페, belanja/쇼핑, bioskop/문화, hobi).
  - **Savings & Investments (20%)**: Tabungan masa depan & dana darurat.
- **Category Monthly Limits**: Pengaturan batas maksimal belanja per kategori dengan status bar progres:
  - 🟢 **Aman** (< 75% terpakai)
  - 🟡 **Waspada** (75% - 99% terpakai)
  - 🔴 **Melebihi Batas** (≥ 100% terlampaui)

### 4. 📈 Visual Analytics & Donut Charts
- Diagram donat interaktif komposisi pengeluaran per kategori.
- Visualisasi perbandingan rasio Cashflow (Income vs Expense).
- Indikator **Savings Rate %** (*Rasio Tabungan*).
- Daftar peringkat kategori pengeluaran terbesar (*Top Expense Ranking*).

### 5. 🎯 Savings Goals (Celengan Digital Target)
- Pembuatan target tabungan khusus (contoh: *"Tiket Liburan ke Jeju"*, *"Dana Darurat 3 Bulan"*).
- Fitur setor tabungan (*Deposit Funds*) langsung menambah progres celengan.
- Progress bar pencapaian target dan estimasi sisa dana yang dibutuhkan.

### 6. 💾 100% Offline SQLite Database & JSON Backup
- Tersimpan aman di SQLite lokal komputer Anda (`~/.moneymanagement/moneymanagement.db`).
- Fitur ekspor (*Export*) seluruh data ke format JSON untuk cadangan.
- Fitur impor (*Import*) data cadangan JSON kapan saja.

### 7. 🎨 Shadcn UI Minimalist Design
- Mengadopsi filosofi desain Shadcn UI: Border halus 1px (`border-border`), palet warna Zinc yang elegan, pill badges, dan tombol hover yang halus.
- Dukungan **Dark Mode** (Zinc-950) dan **Light Mode** (Slate-50) yang dapat diganti kapan saja di menu Pengaturan.

---

## 🚀 Cara Menjalankan Aplikasi

Pastikan Anda berada di direktori project `d:\MoneyManagementApp`.

### 1. Menjalankan Langsung di Windows Desktop
Buka terminal PowerShell atau CMD, lalu jalankan:
```powershell
.\gradlew.bat run
```
*Aplikasi desktop Windows akan langsung terbuka dalam hitungan detik!*

### 2. Menjalankan Unit Test Otomatis
Untuk memvalidasi database SQLite, kalkulasi mata uang KRW, dan backup serialization:
```powershell
.\gradlew.bat desktopTest
```

### 3. Membuat Installer / Executable (.exe / .msi)
Untuk membungkus aplikasi menjadi binary distributable:
```powershell
.\gradlew.bat packageDistributionForCurrentOS
```
File output akan tersedia di `build/compose/binaries/main/`.

---

## 📁 Struktur Kode Project

```
d:/MoneyManagementApp/
├── build.gradle.kts                   # Konfigurasi Compose Multiplatform & dependencies
├── settings.gradle.kts                # Konfigurasi plugin management & repositories
├── src/
│   ├── desktopMain/
│   │   └── kotlin/
│   │       ├── Main.kt                # Window application entry point & navigation
│   │       ├── data/
│   │       │   ├── model/             # Transaction, Category, Budget, SavingsGoal, Enums
│   │       │   ├── database/          # SQLite JDBC manager, table schema & initial seeding
│   │       │   └── repository/        # FinanceRepository (Reactive StateFlows & calculations)
│   │       ├── ui/
│   │       │   ├── theme/             # Shadcn Zinc color palette & AppTheme
│   │       │   ├── components/        # ShadcnCard, ShadcnButton, Input, Badge, Progress, Charts, Dialogs
│   │       │   └── screens/           # Dashboard, Transactions, Budgeting, Analytics, Goals, Settings
│   │       └── util/                  # CurrencyFormatter (₩ KRW) & ColorParser
│   └── desktopTest/
│       └── kotlin/
│           └── FinanceAppTest.kt      # Automated unit tests for database, KRW & models
└── README.md
```

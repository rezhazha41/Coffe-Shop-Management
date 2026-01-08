# ☕ Sooya Coffee POS (Point of Sales)

**Aplikasi Kasir Modern & Manajemen Kedai Kopi Berbasis Android**

![Kotlin](https://img.shields.io/badge/Kotlin-1.9-purple) ![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-green) ![Android](https://img.shields.io/badge/Platform-Android-blue)

## 📱 Tentang Aplikasi
**Sooya Coffee POS** adalah aplikasi Android native yang dirancang untuk membantu operasional bisnis kedai kopi (Coffee Shop). Aplikasi ini memudahkan proses pemesanan, pembayaran, hingga pelaporan keuangan dengan antarmuka yang modern, cepat, dan mudah digunakan.

Aplikasi ini dibangun menggunakan **Kotlin** dan teknologi UI terbaru dari Google, **Jetpack Compose**, serta menerapkan arsitektur **MVVM** untuk performa yang optimal.

## ✨ Fitur Utama

### 🛒 Sistem Kasir (POS)
*   **Transaksi Cepat**: Tampilan grid produk yang responsif untuk tablet dan smartphone.
*   **Keranjang Belanja**: Tambah, kurangi, dan hapus item dengan mudah.
*   **Kalkulasi Otomatis**: Menghitung total belanja secara real-time.

### 🖨️ Cetak Struk & Pembayaran
*   **Thermal Printer Support**: Mencetak struk belanja ke printer thermal (via Print Service).
*   **Custom Logo Struk**: Logo toko otomatis dikonversi ke hitam-putih (grayscale) agar terlihat jelas di kertas struk.
*   **Export Struk**: Simpan struk digital sebagai gambar ke Galeri untuk dibagikan.

### 📊 Laporan & Analitik
*   **Dashboard Interaktif**: Grafik penjualan harian, mingguan, dan bulanan.
*   **Laporan Keuangan**: Ringkasan Pemasukan, Pengeluaran, dan Profit bersih.
*   **Ekspor PDF**: Unduh laporan keuangan profesional dalam format PDF lengkap dengan kop surat dan logo toko.

### 📦 Manajemen Inventaris
*   **CRUD Produk**: Tambah, edit, dan hapus menu makanan/minuman dengan foto.
*   **Kategori**: Kelola kategori (Kopi, Non-Kopi, Makanan, dll).
*   **Stok & Restock**: Pantau stok menipis dan catat pengeluaran belanja bahan baku.

### ⚙️ Pengaturan & Personalisasi
*   **Profil Toko**: Ganti nama toko, alamat, dan logo sesuai brand Anda ("Sooya Coffee").
*   **Dark Mode**: Mendukung mode gelap dan terang sesuai tema sistem.

## 🛠️ Teknologi & Libraries
Proyek ini dibuat menggunakan stack teknologi modern Android development:

*   **Language**: [Kotlin](https://kotlinlang.org/)
*   **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material Design 3)
*   **Architecture**: MVVM (Model-View-ViewModel)
*   **Local Database**: [Room Database](https://developer.android.com/training/data-storage/room) (SQLite)
*   **Image Loading**: [Coil](https://coil-kt.github.io/coil/)
*   **Charts**: [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)

## 🚀 Cara Menjalankan (Getting Started)

1.  **Clone Repository**
    ```bash
    git clone https://github.com/username-anda/sooya-coffee-pos.git
    cd sooya-coffee-pos
    ```

2.  **Buka di Android Studio**
    *   Buka Android Studio.
    *   Pilih `Open` dan arahkan ke folder project.
    *   Tunggu proses Gradle Sync selesai.

3.  **Run Aplikasi**
    *   Hubungkan perangkat Android atau jalankan Emulator.
    *   Klik tombol ▶️ **Run**.

## 🔑 Akun Default (Untuk Login Awal)
Aplikasi ini memiliki sistem login sederhana untuk proteksi akses admin. Gunakan kredensial berikut untuk percobaan pertama:

*   **Username**: `admin`
*   **Password**: `admin123`

> **Catatan**: Anda dapat mengubah password dan detail toko pada menu **Profil & Pengaturan**.

## 📸 Screenshots (Preview)
*(Tambahkan screenshot aplikasi Anda di sini. Letakkan file gambar di folder /screenshots dan link di bawah ini)*

| Login Screen | Dashboard | POS / Kasir |
| :---: | :---: | :---: |
| ![Login](screenshots/login.png) | ![Dashboard](screenshots/dashboard.png) | ![POS](screenshots/pos.png) |

| Laporan Keuangan | Cetak Struk |
| :---: | :---: |
| ![Report](screenshots/report.png) | ![Receipt](screenshots/receipt.png) |

---
Copyright © 2026 Sooya Coffee. Built with ❤️ using Android Jetpack Compose.
# Coffe-Shop-Management

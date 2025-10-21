# ReceiptTrackr - Complete Android Project

## 📦 What's Included

This ZIP contains a complete, production-ready Android app with:

- ✅ **32 source files** (Kotlin, XML, Gradle)
- ✅ **Clean Architecture** (UI → ViewModel → Repository → Domain)
- ✅ **ML Kit OCR** for text recognition
- ✅ **Room Database** for offline storage
- ✅ **Jetpack Compose** UI with Material 3
- ✅ **Hilt DI** for dependency injection
- ✅ **Debug Panel** with 4 tabs (Logs, OCR, Parse, DB)
- ✅ **Unit Tests** with 5 test cases
- ✅ Min SDK 24 (Android 7.0+)

## 🚀 Quick Start

### 1. Extract the ZIP
```bash
unzip ReceiptTrackr.zip
cd ReceiptTrackr
```

### 2. Open in Android Studio
1. Launch **Android Studio Hedgehog (2023.1.1)** or newer
2. Click **"Open an Existing Project"**
3. Navigate to the `ReceiptTrackr` folder
4. Click **OK**

### 3. Wait for Gradle Sync
- Android Studio will automatically sync Gradle (2-5 minutes)
- If you see "SDK location not found", create `local.properties`:
  ```properties
  sdk.dir=/path/to/your/Android/sdk
  ```

### 4. Run the App
**Option A: Physical Device (Recommended)**
1. Enable **Developer Options** on your Android phone
2. Enable **USB Debugging**
3. Connect via USB
4. Click the green **Run** button (▶️) or press `Shift+F10`

**Option B: Emulator**
1. In Android Studio: **Tools → Device Manager**
2. Click **Create Device**
3. Choose **Pixel 5** (or similar)
4. Download **Android 10+** system image
5. Start the emulator and click **Run**

## 🎯 Testing the App

### First Run Flow
1. **Home Screen** opens → You'll see "No receipts yet"
2. **Tap the "+" FAB** (bottom-right) → Gallery picker opens
3. **Select a receipt image** → OCR processing starts (5-15 seconds)
4. **Review Screen** appears → Edit merchant, items, categories
5. **Tap Save icon** (top-right) → Receipt saved to database
6. **View Details** → Tap the receipt card on Home screen

### Debug Panel (🐛 Icon)
Tap the **bug icon** (top-right on any screen) to open the debug panel:
- **Logs Tab**: View all app logs (200 most recent)
- **OCR Tab**: See processing time, text length, and image thumbnail
- **Parse Tab**: View parsed JSON data with validation warnings
- **DB Tab**: See the 5 most recent receipts in database

## 📁 Project Structure

```
ReceiptTrackr/
├─ app/
│  ├─ build.gradle.kts                   # App dependencies
│  ├─ src/main/
│  │  ├─ AndroidManifest.xml
│  │  ├─ java/com/kevin/receipttrackr/
│  │  │  ├─ MainActivity.kt              # Entry point
│  │  │  ├─ AppNav.kt                    # Navigation graph
│  │  │  ├─ ui/                          # 5 Compose screens
│  │  │  ├─ viewmodel/                   # 3 ViewModels
│  │  │  ├─ data/db/                     # Room entities & DAOs
│  │  │  ├─ data/repo/                   # Repository
│  │  │  ├─ domain/                      # Parser & Categorizer
│  │  │  ├─ ocr/                         # ML Kit wrapper
│  │  │  ├─ debug/                       # Debug panel & logger
│  │  │  ├─ util/                        # Formatters
│  │  │  ├─ settings/                    # DataStore
│  │  │  └─ di/                          # Hilt DI module
│  │  ├─ res/                            # Resources (strings, icons)
│  │  └─ assets/                         # Sample receipt fixture
│  └─ src/test/                          # Unit tests
├─ build.gradle.kts                      # Project config
├─ settings.gradle.kts                   # Gradle settings
└─ gradle.properties                     # Build properties
```

## 🔧 Build Commands

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Install on connected device
./gradlew installDebug

# Build release APK
./gradlew assembleRelease
```

## 🐛 Debug Panel Toggle

The debug panel is controlled by `BuildConfig.ENABLE_DEBUG_PANEL`:

- **Debug builds**: Enabled by default
- **Release builds**: Disabled by default

To change, edit `app/build.gradle.kts`:

```kotlin
buildTypes {
    release {
        buildConfigField("boolean", "ENABLE_DEBUG_PANEL", "false")  // or "true"
    }
    debug {
        buildConfigField("boolean", "ENABLE_DEBUG_PANEL", "true")   // or "false"
    }
}
```

## 🔍 Common Issues

### Issue: "SDK location not found"
**Fix:** Create `local.properties` in project root:
```properties
sdk.dir=/Users/YourName/Library/Android/sdk
# Windows: sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
```

### Issue: "Plugin 'com.google.dagger.hilt.android' not found"
**Fix:** Sync Gradle again or run:
```bash
./gradlew --refresh-dependencies
```

### Issue: OCR returns empty text
**Cause:** Image too blurry or handwritten text  
**Fix:** Use a clear photo with printed text

### Issue: Date not parsed correctly
**Cause:** Date format not in regex patterns  
**Fix:** Manually edit the date field in Review screen

### Issue: Items miscategorized
**Cause:** Keyword not in built-in rules  
**Fix:** Change category manually - app will learn from your edit

## 📝 Acceptance Criteria (All Met ✅)

- ✅ Import gallery image (Photo Picker)
- ✅ See OCR text and parsed fields
- ✅ Edit merchant/date/items/categories
- ✅ Save to database (Room)
- ✅ View on Home with monthly totals
- ✅ Debug panel opens with 4 tabs
- ✅ Unit tests pass (`./gradlew test`)
- ✅ Builds and runs (Min SDK 24)

## 🚀 Next Steps (Not Implemented)

These features are planned for future versions:

1. **CSV Export** - Export receipts to `.csv` file
2. **Monthly Charts** - Bar chart on Home screen
3. **CameraX Screen** - Direct camera capture
4. **Smarter NLP** - TensorFlow Lite for better categorization
5. **Duplicate Detection** - Prevent duplicate receipt imports
6. **Multi-currency** - Handle different currencies
7. **Offline Search** - Full-text search across receipts

## 📚 Tech Stack

- **Language**: Kotlin 1.9.22
- **UI**: Jetpack Compose + Material 3
- **Navigation**: Navigation Compose
- **DI**: Hilt 2.50
- **Database**: Room 2.6.1
- **Settings**: DataStore Preferences
- **OCR**: ML Kit Text Recognition 16.0.0
- **Images**: Coil 2.5.0
- **Async**: Coroutines + Flow
- **Testing**: JUnit 4 + Mockito

## 🎉 You're Ready!

1. Open the project in Android Studio
2. Wait for Gradle sync
3. Run on a device/emulator
4. Import a receipt photo
5. Watch the OCR magic happen!

Questions? Check the Debug Panel logs for diagnostics.

**Enjoy building with ReceiptTrackr!** 🧾✨

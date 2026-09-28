# Implementation Plan - Refactoring to ViewModels, Navigation Transitions, and Theme Styling

## Problem & Goal
Refactor the app to use:
1. **ViewModels** (`FinanceViewModel`) for clean separation of UI state and business logic.
2. **Jetpack Navigation Compose with Smooth Transitions** (animated slide/fade transitions between screens instead of basic state switches).
3. **Consistent Theme & Typography** across all screens leveraging Google Fonts (`MiFuenteGoogle` / Raleway) and `MaterialTheme.colorScheme`.

## User Review Required

> [!IMPORTANT]
> We will add Jetpack Navigation (`androidx.navigation:navigation-compose`) and Lifecycle ViewModel Compose (`androidx.lifecycle:lifecycle-viewmodel-compose`) dependencies to `app/build.gradle.kts`.

## Proposed Changes

### Build Configuration
#### [MODIFY] [build.gradle.kts](file:///C:/Users/johnn/AndroidStudioProjects/ProyectoFinal/app/build.gradle.kts)
- Add navigation-compose and lifecycle-viewmodel-compose dependencies.

### ViewModel & State Management
#### [NEW] [FinanceViewModel.kt](file:///C:/Users/johnn/AndroidStudioProjects/ProyectoFinal/app/src/main/java/com/example/proyectofinal/viewmodel/FinanceViewModel.kt)
- Manage `currentUser`, `financialDataMap`, and add/update entry actions.

### Navigation & Transitions
#### [NEW] [AppNavGraph.kt](file:///C:/Users/johnn/AndroidStudioProjects/ProyectoFinal/app/src/main/java/com/example/proyectofinal/navigation/AppNavGraph.kt)
- Define NavHost with animated transitions (`fadeIn`, `slideInHorizontally`, `fadeOut`, `slideOutHorizontally`).

#### [MODIFY] [MainActivity.kt](file:///C:/Users/johnn/AndroidStudioProjects/ProyectoFinal/app/src/main/java/com/example/proyectofinal/MainActivity.kt)
- Host `FinanceViewModel` and `AppNavGraph`.

### UI Screens (Theme & Typography alignment)
#### [MODIFY] [HomeScreen.kt](file:///C:/Users/johnn/AndroidStudioProjects/ProyectoFinal/app/src/main/java/com/example/proyectofinal/screens/HomeScreen.kt)
#### [MODIFY] [AddEntryScreen.kt](file:///C:/Users/johnn/AndroidStudioProjects/ProyectoFinal/app/src/main/java/com/example/proyectofinal/screens/AddEntryScreen.kt)
#### [MODIFY] [LoginScreen.kt](file:///C:/Users/johnn/AndroidStudioProjects/ProyectoFinal/app/src/main/java/com/example/proyectofinal/screens/LoginScreen.kt)
#### [MODIFY] [IncomeDetailScreen.kt](file:///C:/Users/johnn/AndroidStudioProjects/ProyectoFinal/app/src/main/java/com/example/proyectofinal/screens/IncomeDetailScreen.kt)
#### [MODIFY] [SavingDetailScreen.kt](file:///C:/Users/johnn/AndroidStudioProjects/ProyectoFinal/app/src/main/java/com/example/proyectofinal/screens/SavingDetailScreen.kt)
#### [MODIFY] [ExpenseDetailScreen.kt](file:///C:/Users/johnn/AndroidStudioProjects/ProyectoFinal/app/src/main/java/com/example/proyectofinal/screens/ExpenseDetailScreen.kt)
- Ensure all screens use `MiFuenteGoogle` / `MaterialTheme.typography` and `MaterialTheme.colorScheme`.

## Verification Plan

### Automated Tests
- Run Gradle build (`app:assembleDebug`) to verify zero compilation errors.

### Manual Verification
- Deploy to emulator/device and test smooth transitions between Login, Home, Add Entry, and Detail screens, and verify theme/typography styling.

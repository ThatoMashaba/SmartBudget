# 💰 SmartBudget — Android Budget Tracker App

## About
SmartBudget is an Android budget tracking application built with Kotlin and Room DB. 
It helps users track their daily expenses, set monthly spending goals, 
and visualise their spending habits through charts and reports.

---


---

## 📄 Part 1 Documents

The following documents were submitted as part of the research and design phase:

- [📋 Planning and Design Document](PLANNING_AND_DESIGN.pdf)
- [🔍 Research Document](RESEARCH_.pdf)

## 👨‍💻 Group Members

| Name | Student Number |
|---|---|
| Lefa Mashaba (Thato) | ST10451816 |
| Lindokuhle Maseko | ST10444841 |

- **Institution:** Rosebank College, Nelspruit
- **Module:** Mobile Application Development

---

## 📱 Features

### Core Features
- 🔐 **User Login & Registration** — Secure login with username and password
- 📁 **Categories** — Create and manage expense categories
- 💸 **Expense Tracking** — Log expenses with date, start/end time, description, amount, category and optional photo
- ✏️ **Edit & Delete Expenses** — Tap any expense to edit or delete it
- 🎯 **Monthly Goals** — Set minimum and maximum monthly spending goals using SeekBar
- 📊 **Reports** — View spending by category over a user-selectable period
- 📈 **Bar Chart** — Visual chart showing spending per category with min/max goal lines
- 🌙 **Dark Mode** — Toggle between light and dark themes, saved persistently
- 🏆 **Gamification Badges** — Earn badges for meeting goals and logging expenses consistently

### My Own Custom Features
1. **✏️ Edit Expenses** — Users can tap any existing expense to edit all its details 
   or delete it entirely. Implemented using a separate activity with pre-filled form fields.
2. **🌙 Dark Mode Toggle** — Users can switch between light and dark themes from the 
   Reports screen. The preference is saved using SharedPreferences and applied on app startup.

---

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Kotlin | Primary programming language |
| Room DB | Local SQLite database (offline storage) |
| ViewBinding | UI element binding |
| Coroutines & Flow | Async database operations |
| Glide | Image loading for expense photos |
| MPAndroidChart | Bar chart for spending visualisation |
| SharedPreferences | Saving user session and dark mode preference |
| Navigation Component | Fragment navigation |
| Material Components | UI design and theming |

---

## 🗄️ Database Design

The app uses **Room DB** with 4 tables:

- **users** — Stores login credentials
- **categories** — Expense categories linked to a user
- **expenses** — Individual expense entries with optional photo
- **monthly_goals** — Min/max spending goals per month per user

---

## 📐 Design Considerations

- **MVVM-inspired architecture** — Separation of data, UI, and logic layers
- **Dark/Light theming** — Uses `DayNight` Material theme for full dark mode support
- **Input validation** — All forms validate inputs and show clear error messages
- **Offline first** — All data stored locally using Room DB, no internet required
- **User-friendly UI** — Cards, bottom navigation, FAB, date/time pickers for ease of use

---

## 🏆 Gamification Badges

| Badge | How to Earn |
|---|---|
| 📝 Getting Started | Log your first expense |
| 📊 Consistent Tracker | Log at least 10 expenses |
| 🔥 Power User | Log at least 25 expenses |
| 🗂️ Organizer | Create at least 3 categories |
| 💰 Budget Master | Stay below maximum goal |
| 🏆 Goal Achiever | Stay within min and max goal range |

---

## ⚙️ GitHub Actions

This project uses GitHub Actions for automated building and testing.
The workflow runs on every push to `main` and:
- Builds the debug APK
- Runs unit tests
- Confirms the build passes on a clean environment

---

## 🎬 Demo Video

> [Click here to watch the demo video](#)
> *https://youtu.be/t3c5Kq3hz00?si=NGzXwU7VMU0BcCiG*

The demo video shows:
1. User registration and login
2. Creating categories
3. Adding expenses with photos
4. Editing and deleting expenses
5. Setting monthly goals with SeekBar
6. Viewing reports and bar chart
7. Earning gamification badges
8. Toggling dark mode

---

## 📦 APK Download

The built debug APK is available in the repository under:
app/build/outputs/apk/debug/app-debug.apk

---

## 🚀 How to Run

1. Clone the repository:
git clone https://github.com/ThatoMashaba/SmartBudget.git
2. Open in **Android Studio**
3. Sync Gradle
4. Run on an Android device or emulator (API 25+)

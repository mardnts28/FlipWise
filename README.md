# FlipWise 🎓🚀

**FlipWise** is a premium, AI-powered flashcard learning platform built for Android. It transforms your raw notes, PDFs, and presentations into interactive study materials using state-of-the-art Generative AI. 

Whether you're studying for finals or learning a new language, FlipWise makes the process smarter, faster, and more social.

---

## ✨ Key Features

### 🤖 AI Flashcard Generation
*   **Intelligent Extraction**: Transform text from multiple file formats into high-quality flashcards. 
*   **Multi-Format Support**: 
    *   ✅ **PDF** (Academic papers, textbooks)
    *   ✅ **DOCX** (Modern Word documents)
    *   ✅ **PPTX** (Lecture presentations/slides)
    *   ✅ **TXT** (Plain text notes)
*   **Precision Parsing**: Advanced multi-stage JSON recovery ensures you always get clean, usable results from the Gemini AI.

### ⚔️ Competitive & Social Learning
*   **Real-time Leaderboard**: See where you stand globally with a premium, live-updating ranking system.
*   **Interactive Global Events**: Platform-wide challenges are now clickable! Join community goals and track your contribution to collective milestones in real-time.
*   **Friend Social Circle**: Find classmates by their unique username and build your study network.
*   **Versus Battles**: Challenge friends directly. Track scores side-by-side as you both study.
*   **Team Challenges**: Join forces in community-wide Blue vs. Red team battles. 

### 💎 Premium User Experience
*   **Unified Design Language**: A sleek, modern "CoralZest" and "GrapePop" theme throughout the app.
*   **Two-Factor Admin Security**: The Admin Dashboard is hardened with mandatory OTP verification to prevent unauthorized access.
*   **Gamified Progress**: Earn XP and level up as you study. Your competitive wins contribute to your global standing.
*   **Privacy First**: Built-in screenshot and recording protection for your study content.
*   **Cloud Persistence**: Real-time sync with Firebase ensures your decks and friend lists are safe and available on any device.

---

## 🛠️ Technology Stack

*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose
*   **Backend**: Firebase (Authentication, Realtime Database, Firestore)
*   **AI Engine**: Google Gemini AI (1.5 Flash)
*   **Local Storage**: Room Database + SQLCipher (Encryption)
*   **File Analysis**: PDFBox-Android + Custom OpenXML Parser

---

## 🚀 Getting Started

1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/mary/FlipWise.git
    ```
2.  **API Key Setup**: 
    *   Obtain a Gemini API key from the Google AI Studio.
    *   Add your key to `local.properties`:
        ```properties
        GEMINI_API_KEY=your_api_key_here
        ```
3.  **Run the App**: Build and install on an Android device or emulator (minSDK 26).

---

## 📸 Design Preview
*   **Vibrant Themes**: Deep violets and energetic corals create a focused, high-energy environment.
*   **Dynamic Animations**: Smooth transitions and micro-interactions for a premium feel.

---

*“Study smarter, not harder. Turn your documents into knowledge with FlipWise.”* 🧠💡
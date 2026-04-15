# FlipWise Admin Dashboard Implementation Plan

This plan outlines the architecture and design for the FlipWise Admin Dashboard web application.

## 🎨 Design System
- **Colors:** 
  - `primary`: #7C3AED (GrapePop)
  - `secondary`: #1E1B4B (NavyInk)
  - `accent`: #F97316 (OrangeZest)
  - `background`: #FBFBFF
  - `text-dark`: #111827
- **Typography:** Inter (or fallback sans-serif) with bold weights for headers.
- **Components:** Glassmorphism cards, rounded corners (24px-32px), smooth transitions.

## 📁 Structure
1. **Layout:** Sidebar navigation with profile/session top bar.
2. **Setup:** `firebase.ts` for database connectivity.
3. **Pages:**
    - **Dashboard:** Recharts for metrics (DAU, Sessions).
    - **User Management:** Table with status controls (active/banned).
    - **Content Moderation:** Feed of global decks with 'Feature' button.
    - **Global Challenges:** Form to create community-wide events.
    - **Notifications:** Broadcast sender.
    - **Analytics:** Engagement heatmaps and subject trends.
    - **Audit Logs:** Feed of security events.
    - **Support:** Ticketing system interface.

## 🔧 Connectivity (Instructions)
The admin side will connect to the same Firebase Realtime Database and Auth used by the Android app. 
- **Database Rules:** Ensure that `/admin_configs` and sensitive operations require `auth.token.role == 'admin'`.
- **Firebase Config:** Requires a web app registration in the Firebase console.

## 🚀 Phase 1: Core Layout & Setup
1. Create `index.css`.
2. Configure `App.tsx` with `react-router-dom`.
3. Build the `Sidebar` and `Header` components.

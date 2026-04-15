import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getDatabase } from "firebase/database";

// TO CONNECT TO YOUR DATABASE:
// 1. Go to Firebase Console > Project Settings
// 2. Add a 'Web' application
// 3. Replace the placeholder values below with your actual Firebase config
const firebaseConfig = {
  apiKey: "REPLACE_WITH_YOUR_API_KEY",
  authDomain: "flipwise-dc052.firebaseapp.com",
  databaseURL: "https://flipwise-dc052-default-rtdb.asia-southeast1.firebasedatabase.app",
  projectId: "flipwise-dc052",
  storageBucket: "flipwise-dc052.firebasestorage.app",
  messagingSenderId: "REPLACE_WITH_ID",
  appId: "REPLACE_WITH_APP_ID"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const rtdb = getDatabase(app);

export default app;

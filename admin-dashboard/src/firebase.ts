import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getDatabase } from "firebase/database";

// Firebase configuration for FlipWise Web App
const firebaseConfig = {
  apiKey: "AIzaSyD52TRomk1F9geaEwM63CZCNAbyciTOqOE",
  authDomain: "flipwise-dc052.firebaseapp.com",
  databaseURL: "https://flipwise-dc052-default-rtdb.asia-southeast1.firebasedatabase.app",
  projectId: "flipwise-dc052",
  storageBucket: "flipwise-dc052.firebasestorage.app",
  messagingSenderId: "307290224469",
  appId: "1:307290224469:web:100dcb98dd4ac79e3f21dc",
  measurementId: "G-ZGF8CB7WNM"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const rtdb = getDatabase(app);

export default app;

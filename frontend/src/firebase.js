// eslint-disable-next-line import/extensions
import { initializeApp } from 'firebase/app';
// eslint-disable-next-line import/extensions
import { getAuth } from 'firebase/auth';
// eslint-disable-next-line import/extensions
import { getFirestore } from 'firebase/firestore';

// Your Firebase configuration
const firebaseConfig = {
  apiKey: 'AIzaSyCJNmrYjHN89oXHATjEn-ngYqtnQgbSP8g',
  authDomain: 'AgriBackup-19d2a.firebaseapp.com',
  projectId: 'AgriBackup-19d2a',
  storageBucket: 'AgriBackup-19d2a.firebasestorage.app',
  messagingSenderId: '463127818224',
  appId: '1:463127818224:web:69dea1e25a3ff3d7867735',
};

if (window.trustedTypes && window.trustedTypes.createPolicy) {
  window.trustedTypes.createPolicy('firebase-policy', {
    createScriptURL: (string) => string,
  });
}

const app = initializeApp(firebaseConfig);

// Initialize services
const auth = getAuth(app);
const db = getFirestore(app);

export { auth, db };

package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.StartupIdea
import com.example.data.model.StartupReport
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.moshi.Moshi

object FirebaseService {
    private const val TAG = "FirebaseService"
    private var isInitialized = false

    private var auth: FirebaseAuth? = null
    private var db: FirebaseFirestore? = null

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            // Check if Firebase is already initialized by default configuration files
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                auth = FirebaseAuth.getInstance()
                db = FirebaseFirestore.getInstance()
                isInitialized = true
                Log.d(TAG, "Firebase initialized successfully via standard google-services configuration.")
                return
            }

            // Attempt dynamic programmatic fallback setup using project BuildConfigs
            val apiKey = try { BuildConfig.FIREBASE_API_KEY } catch (e: Exception) { "" }
            val appId = try { BuildConfig.FIREBASE_APP_ID } catch (e: Exception) { "" }
            val projectId = try { BuildConfig.FIREBASE_PROJECT_ID } catch (e: Exception) { "" }

            if (apiKey.isNotEmpty() && appId.isNotEmpty() && projectId.isNotEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey(apiKey)
                    .setApplicationId(appId)
                    .setProjectId(projectId)
                    .build()

                FirebaseApp.initializeApp(context, options)
                auth = FirebaseAuth.getInstance()
                db = FirebaseFirestore.getInstance()
                isInitialized = true
                Log.d(TAG, "Firebase dynamically initialized with BuildConfig keys.")
            } else {
                Log.w(TAG, "No default or dynamic Firebase setup credentials found. Sandbox active.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase initialization error: ${e.localizedMessage}")
        }
    }

    fun isFirebaseReady(): Boolean {
        return isInitialized && auth != null && db != null
    }

    fun loginOrCreateUser(email: String, name: String, onResult: (Boolean, String?) -> Unit) {
        val authInstance = auth
        if (!isFirebaseReady() || authInstance == null) {
            // Gracefully succeed and let users sign in locally
            onResult(true, "Signed in via local offline profile sandbox")
            return
        }

        val fallbackPassword = "DefaultSecuredPassword9"
        authInstance.signInWithEmailAndPassword(email, fallbackPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    // Sign-up process
                    authInstance.createUserWithEmailAndPassword(email, fallbackPassword)
                        .addOnCompleteListener { signupTask ->
                            if (signupTask.isSuccessful) {
                                // Update displayName profile
                                val user = signupTask.result?.user
                                val updateRequest = com.google.firebase.auth.userProfileChangeRequest {
                                    displayName = name
                                }
                                user?.updateProfile(updateRequest)
                                onResult(true, null)
                            } else {
                                onResult(false, signupTask.exception?.localizedMessage ?: "Failed Firebase transaction")
                            }
                        }
                }
            }
    }

    fun logout() {
        if (isFirebaseReady()) {
            auth?.signOut()
        }
    }

    fun saveStartupIdeaToFirestore(idea: StartupIdea, onResult: (Boolean, String?) -> Unit) {
        val dbInstance = db
        val authInstance = auth
        if (!isFirebaseReady() || dbInstance == null || authInstance == null) {
            onResult(false, "Firebase service not initialized")
            return
        }

        val uid = authInstance.currentUser?.uid ?: "anonymous"
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(StartupReport::class.java)
        val reportJson = idea.report?.let { adapter.toJson(it) } ?: ""

        val data = hashMapOf(
            "id" to idea.id,
            "name" to idea.name,
            "description" to idea.description,
            "industry" to idea.industry,
            "targetAudience" to idea.targetAudience,
            "timestamp" to idea.timestamp,
            "reportJson" to reportJson,
            "userId" to uid
        )

        dbInstance.collection("users").document(uid)
            .collection("startup_ideas").document(idea.id.toString())
            .set(data)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.localizedMessage)
            }
    }

    fun syncAllLocalIdeasToCloud(ideas: List<StartupIdea>) {
        if (!isFirebaseReady()) return
        for (idea in ideas) {
            saveStartupIdeaToFirestore(idea) { success, err ->
                if (success) {
                    Log.d(TAG, "Secured idea upload success to cloud: ${idea.name}")
                } else {
                    Log.e(TAG, "Sync detail failure: $err")
                }
            }
        }
    }
}

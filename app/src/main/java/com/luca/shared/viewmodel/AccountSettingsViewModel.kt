package com.luca.shared.viewmodel
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luca.shared.model.BankAccountData
import com.luca.shared.model.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
class AccountSettingsViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()
    private val _selectedAvatarName = MutableStateFlow("avatar_1")
    val selectedAvatarName: StateFlow<String> = _selectedAvatarName.asStateFlow()

    private val _bankAccounts = MutableStateFlow<List<BankAccountData>>(emptyList())
    val bankAccounts: StateFlow<List<BankAccountData>> = _bankAccounts.asStateFlow()

    private val _isEditingUsername = MutableStateFlow(false)
    val isEditingUsername: StateFlow<Boolean> = _isEditingUsername.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadCurrentUserData()
    }

    fun setEditingUsername(isEditing: Boolean) {
        _isEditingUsername.value = isEditing
    }
    fun loadCurrentUserData() {
        val currentUserUid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val docSnapshot = firestore.collection("users")
                    .document(currentUserUid)
                    .get()
                    .await()
                if (docSnapshot.exists()) {
                    val user = docSnapshot.toObject(User::class.java)
                    if (user != null) {
                        _currentUser.value = user
                        _username.value = user.username
                        _selectedAvatarName.value = user.avatarName
                        _bankAccounts.value = user.bankAccounts
                    }
                }
            } catch (e: Exception) {
                Log.e("AccountSettingsViewModel", "Error loading user data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun updateAvatarName(newAvatarName: String) {
        val currentUserUid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                firestore.collection("users")
                    .document(currentUserUid)
                    .update("avatarName", newAvatarName)
                    .await()
                _selectedAvatarName.value = newAvatarName
            } catch (e: Exception) {
                Log.e("AccountSettingsViewModel", "Error updating avatar", e)
            }
        }
    }
    fun updateUsername(newUsername: String) {
        val currentUserUid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                if (newUsername.length >= 3) {
                    firestore.collection("users")
                        .document(currentUserUid)
                        .update("username", newUsername)
                        .await()
                    _username.value = newUsername
                    _isEditingUsername.value = false
                }
            } catch (e: Exception) {
                Log.e("AccountSettingsViewModel", "Error updating username", e)
            }
        }
    }

    fun updateBankAccounts(newBankAccounts: List<BankAccountData>) {
        val currentUserUid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                firestore.collection("users")
                    .document(currentUserUid)
                    .update("bankAccounts", newBankAccounts)
                    .await()
                _bankAccounts.value = newBankAccounts
            } catch (e: Exception) {
                Log.e("AccountSettingsViewModel", "Error updating bank accounts", e)
            }
        }
    }

    suspend fun updatePassword(oldPassword: String, newPassword: String): Boolean {
        return try {
            val currentUser = auth.currentUser ?: return false
            val userEmail = currentUser.email ?: return false
            val credential = EmailAuthProvider.getCredential(userEmail, oldPassword)
            currentUser.reauthenticate(credential).await()
            currentUser.updatePassword(newPassword).await()
            true
        } catch (e: Exception) {
            Log.e("AccountSettingsViewModel", "Error updating password", e)
            false
        }
    }
    // Check if user is signed in with Google OAuth
    fun isGoogleUser(): Boolean {
        val currentUser = auth.currentUser ?: return false
        return currentUser.providerData.any { it.providerId == "google.com" }
    }

    suspend fun deleteAccount(password: String?): Boolean {
        return try {
            val currentUser = auth.currentUser ?: return false
            val userUid = currentUser.uid

            // If not a Google user and password is required
            if (!isGoogleUser()) {
                if (password.isNullOrEmpty()) return false
                val userEmail = currentUser.email ?: return false
                val credential = EmailAuthProvider.getCredential(userEmail, password)
                currentUser.reauthenticate(credential).await()
            }
            // For Google users, no reauthentication needed (or can be added if required)

            firestore.collection("users").document(userUid).delete().await()
            currentUser.delete().await()
            true
        } catch (e: Exception) {
            Log.e("AccountSettingsViewModel", "Error deleting account", e)
            false
        }
    }
}

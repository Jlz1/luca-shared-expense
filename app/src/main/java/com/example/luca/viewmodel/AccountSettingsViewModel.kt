package com.example.luca.viewmodel
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luca.model.User
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
    private val _isEditingUsername = MutableStateFlow(false)
    val isEditingUsername: StateFlow<Boolean> = _isEditingUsername.asStateFlow()
    fun setEditingUsername(isEditing: Boolean) {
        _isEditingUsername.value = isEditing
    }
    fun loadCurrentUserData() {
        val currentUserUid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
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
                    }
                }
            } catch (e: Exception) {
                Log.e("AccountSettingsViewModel", "Error loading user data", e)
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
    suspend fun deleteAccount(password: String): Boolean {
        return try {
            val currentUser = auth.currentUser ?: return false
            val userEmail = currentUser.email ?: return false
            val userUid = currentUser.uid
            val credential = EmailAuthProvider.getCredential(userEmail, password)
            currentUser.reauthenticate(credential).await()
            firestore.collection("users").document(userUid).delete().await()
            currentUser.delete().await()
            true
        } catch (e: Exception) {
            Log.e("AccountSettingsViewModel", "Error deleting account", e)
            false
        }
    }
}

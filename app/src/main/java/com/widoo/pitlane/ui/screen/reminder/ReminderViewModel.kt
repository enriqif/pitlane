package com.widoo.pitlane.ui.screen.reminder

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.widoo.pitlane.data.local.entity.ReminderEntity
import com.widoo.pitlane.data.repository.ReminderRepository
import com.widoo.pitlane.worker.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class AddReminderUiState(
    val title: String = "",
    val description: String = "",
    val triggerDate: Long = run {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, 1)
        cal.timeInMillis
    },
    val notificationsEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

class ReminderViewModel(
    private val reminderRepository: ReminderRepository,
    private val context: Context
) : ViewModel() {

    val pendingReminders: StateFlow<List<ReminderEntity>> = reminderRepository
        .getPending()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedReminders: StateFlow<List<ReminderEntity>> = reminderRepository
        .getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _addState = MutableStateFlow(AddReminderUiState())
    val addState: StateFlow<AddReminderUiState> = _addState.asStateFlow()

    fun onTitleChange(v: String) {
        _addState.value = _addState.value.copy(title = v)
    }
    fun onDescriptionChange(v: String) {
        _addState.value = _addState.value.copy(description = v)
    }
    fun onDateChange(v: Long) {
        _addState.value = _addState.value.copy(triggerDate = v)
    }
    fun onNotificationsToggle(v: Boolean) {
        _addState.value = _addState.value.copy(notificationsEnabled = v)
    }

    fun isFormValid(): Boolean = _addState.value.title.isNotBlank()

    fun saveReminder(onDone: () -> Unit) {
        viewModelScope.launch {
            _addState.value = _addState.value.copy(isLoading = true)
            try {
                val s = _addState.value
                val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()

                val id = reminderRepository.insert(
                    ReminderEntity(
                        vehicleId = 0L,
                        title = s.title.trim(),
                        description = s.description.trim(),
                        triggerDate = s.triggerDate,
                        isCompleted = false,
                        notificationId = notificationId
                    )
                )

                // Schedule push notification if enabled
                if (s.notificationsEnabled) {
                    ReminderScheduler.schedule(
                        context = context,
                        notificationId = notificationId,
                        title = s.title.trim(),
                        description = s.description.trim(),
                        triggerDateMillis = s.triggerDate
                    )
                }

                _addState.value = AddReminderUiState()
                onDone()
            } catch (e: Exception) {
                _addState.value = _addState.value.copy(
                    isLoading = false,
                    error = "Error al guardar el recordatorio"
                )
            }
        }
    }

    fun markAsCompleted(reminder: ReminderEntity) {
        viewModelScope.launch {
            // Cancel the scheduled notification
            ReminderScheduler.cancel(context, reminder.notificationId)
            reminderRepository.update(reminder.copy(isCompleted = true))
        }
    }

    fun delete(reminder: ReminderEntity) {
        viewModelScope.launch {
            // Cancel the scheduled notification
            ReminderScheduler.cancel(context, reminder.notificationId)
            reminderRepository.delete(reminder)
        }
    }
}
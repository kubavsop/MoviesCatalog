package com.example.moviescatalog.presentation.feature_profile_screen.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.feature_profile_screen.usecase.ChangeProfileUseCase
import com.example.domain.feature_profile_screen.usecase.GetProfileUseCase
import com.example.domain.feature_profile_screen.usecase.LogoutUseCase
import com.example.domain.feature_user_auth.usecase.FormatDateUseCase
import com.example.domain.feature_user_auth.usecase.ValidateEmailUseCase
import com.example.domain.feature_user_auth.usecase.ValidateFirstNameUseCase
import com.example.domain.model.Profile
import com.example.moviescatalog.R
import com.example.moviescatalog.presentation.UiText
import com.example.moviescatalog.presentation.feature_profile_screen.event.ProfileEvent
import com.example.moviescatalog.presentation.feature_profile_screen.state.Gender
import com.example.moviescatalog.presentation.feature_profile_screen.state.ProfileSimilarity
import com.example.moviescatalog.presentation.feature_profile_screen.state.ProfileState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val changeProfileUseCase: ChangeProfileUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validateFirstNameUseCase: ValidateFirstNameUseCase,
    private val formatDateUseCase: FormatDateUseCase
) : ViewModel() {
    private var currentProfile: Profile? = null
    private var profileSimilarity = ProfileSimilarity()

    private var _state = MutableLiveData<ProfileState>(ProfileState.Initial)
    val state: LiveData<ProfileState> = _state

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.ShowProfile -> showProfile()
            is ProfileEvent.AvatarChanged -> avatarChanged(event.avatarLink)
            is ProfileEvent.BirthdayChanged -> birthdayChanged(
                event.year,
                event.monthOfYear,
                event.dayOfMonth
            )

            is ProfileEvent.EmailChanged -> emailChanged(event.email)
            is ProfileEvent.FirstNameChanged -> firstNameChanged(event.firstName)
            is ProfileEvent.GenderChanged -> genderChanged(event.gender)
            is ProfileEvent.ChangeProfile -> changeProfile(
                avatarLink = event.avatarLink,
                email = event.email,
                name = event.name,
            )

            is ProfileEvent.Cancel -> cancel()
        }
    }

    private fun cancel() {
        _state.value = ProfileState.Profile(
            avatarLink = currentProfile!!.avatarLink,
            birthDate = currentProfile!!.birthDate,
            email = currentProfile!!.email,
            gender = currentProfile!!.gender,
            id = currentProfile!!.id,
            name = currentProfile!!.name,
            nickName = currentProfile!!.nickName
        )
    }

    private fun changeProfile(
        avatarLink: String?,
        email: String,
        name: String,
    ) {
        val profileChanged = (_state.value as ProfileState.ProfileChanged)
        viewModelScope.launch {
            try {
                currentProfile = Profile(
                    avatarLink = avatarLink,
                    birthDate = profileChanged.birthDate,
                    email = email,
                    gender = profileChanged.gender.ordinal,
                    id = currentProfile!!.id,
                    name = name,
                    nickName = currentProfile!!.nickName
                )
                changeProfileUseCase(currentProfile!!)
                setProfileChanged()
                profileSimilarity = ProfileSimilarity()

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Unit
            }
        }
    }

    private fun showProfile() {
        try {
            viewModelScope.launch {
                _state.value = ProfileState.Loading

                currentProfile = getProfileUseCase()

                _state.value = ProfileState.Profile(
                    avatarLink = currentProfile!!.avatarLink,
                    birthDate = currentProfile!!.birthDate,
                    email = currentProfile!!.email,
                    gender = currentProfile!!.gender,
                    id = currentProfile!!.id,
                    name = currentProfile!!.name,
                    nickName = currentProfile!!.nickName
                )
                profileSimilarity = ProfileSimilarity()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Unit
        }
    }

    private fun genderChanged(gender: Gender) {
        if (_state.value !is ProfileState.ProfileChanged) setProfileChanged()

        profileSimilarity.gender = gender.ordinal == currentProfile?.gender

        _state.value = (_state.value as ProfileState.ProfileChanged).copy(
            gender = gender,
            isValid = false
        )

        if (!profileSimilarity.gender) checkError()
    }

    private fun avatarChanged(avatarLink: String) {
        profileSimilarity.avatarLink = avatarLink == currentProfile?.avatarLink
        checkError()
    }


    private fun birthdayChanged(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        if (_state.value !is ProfileState.ProfileChanged) setProfileChanged()
        val birthDate = formatDateUseCase(
            year,
            monthOfYear,
            dayOfMonth
        )
        profileSimilarity.birthDate = birthDate == currentProfile?.birthDate

        _state.value = (_state.value as ProfileState.ProfileChanged).copy(
            birthDate = birthDate,
            isValid = false
        )
        if (!profileSimilarity.birthDate) checkError()
    }

    private fun firstNameChanged(firstName: String) {
        if (_state.value !is ProfileState.ProfileChanged) setProfileChanged()

        profileSimilarity.name = firstName == currentProfile?.name

        val isSuccess = validateFirstNameUseCase(firstName)

        _state.value = (_state.value as ProfileState.ProfileChanged).copy(
            firstNameError = if (isSuccess) null else UiText(
                R.string.min_first_name_length_error,
                MIN_FIRST_NAME_LENGTH
            ),
            isValid = false
        )

        if (isSuccess) checkError()
    }

    private fun emailChanged(email: String) {
        if (_state.value !is ProfileState.ProfileChanged) setProfileChanged()

        profileSimilarity.email = email == currentProfile?.email

        val isSuccess = validateEmailUseCase(email)
        _state.value = (_state.value as ProfileState.ProfileChanged).copy(
            emailError = if (isSuccess) null else UiText(
                R.string.email_error
            ),
            isValid = false
        )

        if (isSuccess) checkError()
    }

    private fun checkError() {
        if (_state.value !is ProfileState.ProfileChanged) setProfileChanged()

        val hasError = listOf(
            (_state.value as ProfileState.ProfileChanged).emailError,
            (_state.value as ProfileState.ProfileChanged).firstNameError,
        ).any { it != null }


        val hasDifferences = listOf(
            profileSimilarity.avatarLink,
            profileSimilarity.birthDate,
            profileSimilarity.email,
            profileSimilarity.gender,
            profileSimilarity.id,
            profileSimilarity.name,
            profileSimilarity.nickName,
        ).any { !it }

        if (!hasError && hasDifferences) {
            _state.value = (_state.value as ProfileState.ProfileChanged).copy(
                isValid = true
            )
        }
    }

    private fun setProfileChanged() {
        _state.value = ProfileState.ProfileChanged(
            gender = if (currentProfile!!.gender == 0) Gender.MALE else Gender.FEMALE,
            birthDate = currentProfile!!.birthDate,
            isValid = false
        )
    }

    private companion object {
        const val MIN_FIRST_NAME_LENGTH = 2
    }
}
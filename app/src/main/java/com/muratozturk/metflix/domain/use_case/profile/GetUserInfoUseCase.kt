package com.muratozturk.metflix.domain.use_case.profile

import com.muratozturk.metflix.domain.repository.AuthRepository
import javax.inject.Inject

class GetUserInfoUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke() = repository.getUserInfo()
}

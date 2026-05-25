package com.depi.graduationproject.domain.usecase.checkin

import com.depi.graduationproject.core.utils.PlateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ValidatePlateUseCase @Inject constructor() {
    
    suspend operator fun invoke(plateText: String): Boolean = withContext(Dispatchers.Default) {
        PlateUtils.isValidV4Plate(plateText)
    }
}
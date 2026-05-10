package com.depi.graduationproject.data.local.converter

import androidx.room.TypeConverter

import com.depi.graduationproject.domain.model.SessionStatus
import com.depi.graduationproject.domain.model.SyncStatus


class Converters {
    @TypeConverter
    fun fromSessionStatus(status: SessionStatus): String = status.name

    @TypeConverter
    fun toSessionStatus(status: String): SessionStatus = SessionStatus.valueOf(status)

    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String = status.name

    @TypeConverter
    fun toSyncStatus(status: String): SyncStatus = SyncStatus.valueOf(status)

}
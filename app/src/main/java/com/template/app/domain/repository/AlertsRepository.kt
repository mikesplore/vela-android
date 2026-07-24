package com.template.app.domain.repository

import com.template.app.core.utils.Resource
import com.template.app.domain.model.AlertHistory

interface AlertsRepository {
    suspend fun getHistory(
        limit: Int = 50,
        offset: Int = 0,
        alertKind: String? = null,
        channel: String? = null,
        sinceMinutes: Int? = null
    ): Resource<AlertHistory>
}

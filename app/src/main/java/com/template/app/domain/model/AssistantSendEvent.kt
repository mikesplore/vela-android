package com.template.app.domain.model

import com.template.app.core.utils.Resource

sealed class AssistantSendEvent {
    data class Phase(val phase: AssistantSendPhase) : AssistantSendEvent()
    data class Finished(val result: Resource<Unit>) : AssistantSendEvent()
}

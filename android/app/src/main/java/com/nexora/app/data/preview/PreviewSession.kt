package com.nexora.app.data.preview

object PreviewSession {
    const val PreviewPhone = "+526681234567"
    const val PreviewCode = "123456"
    const val PreviewUid = "preview_ghost_developer"
    const val FriendUid = "preview_friend_akira"
    const val GroupId = "group_nexora_testers"

    @Volatile
    var active: Boolean = false
        private set

    val uid: String?
        get() = if (active) PreviewUid else null

    val phone: String?
        get() = if (active) PreviewPhone else null

    fun enable() {
        active = true
    }

    fun disable() {
        active = false
    }
}

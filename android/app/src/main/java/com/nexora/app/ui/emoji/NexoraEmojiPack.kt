package com.nexora.app.ui.emoji

object NexoraEmojiPack {
    data class Emoji(val code: String, val label: String)

    val default = listOf(
        Emoji("☺", "sonrisa"),
        Emoji("＾_＾", "feliz"),
        Emoji("｡◕‿◕｡", "tierno"),
        Emoji("╰(*´︶`*)╯", "abrazo"),
        Emoji("(๑˃̵ᴗ˂̵)", "emocionado"),
        Emoji("(｡•̀ᴗ-)✧", "guiño"),
        Emoji("♡", "corazón"),
        Emoji("✦", "brillo"),
    )
}

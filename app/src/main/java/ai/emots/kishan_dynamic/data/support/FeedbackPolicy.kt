package ai.emots.kishan_dynamic.data.support

/** Pure rules for preparing user feedback before handing it to a mail client. */
object FeedbackPolicy {
    const val maxCommentLength = 500

    fun canSubmit(rating: Int, comment: String): Boolean =
        rating in 1..5 && comment.isNotBlank()

    fun trimComment(comment: String): String = comment.take(maxCommentLength)
}

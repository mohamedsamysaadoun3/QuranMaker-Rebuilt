package hazem.nurmontage.videoquran.model

import hazem.nurmontage.videoquran.constant.IpadType

data class IpadItem(
    val imageRes: Int = 0,
    val ipadType: IpadType = IpadType.CLASSIC
) {
    /** Alias for Java compatibility - used by IpadAdabter */
    fun getImg(): Int = imageRes
}

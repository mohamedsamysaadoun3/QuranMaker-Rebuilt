package hazem.nurmontage.videoquran.model

class PhotoItem(
    val folder: String,
    val path: String,
    var isSelect: Boolean = false
) {
    var adabter_pos: Int = 0
    var number: Int = 0
    var gallerySelected: GallerySelected? = null
}

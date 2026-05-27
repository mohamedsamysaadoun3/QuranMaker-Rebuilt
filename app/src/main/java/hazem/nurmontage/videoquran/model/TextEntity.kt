package hazem.nurmontage.videoquran.model

import android.text.Layout

class TextEntity : EntityView() {

    var text: String = ""
    var color: Int = -1
    var fontSize: Float = 24f
    var fontName: String = "ReadexPro_Medium.ttf"
    var alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL
    var opacity: Int = 255

    override fun getType(): EntityType = EntityType.TEXT
}

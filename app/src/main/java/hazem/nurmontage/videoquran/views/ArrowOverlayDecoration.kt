package hazem.nurmontage.videoquran.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ArrowOverlayDecoration(context: Context, drawableRes: Int, sizeDp: Int) : RecyclerView.ItemDecoration() {

    private val arrowDrawable: Drawable = AppCompatResources.getDrawable(context, drawableRes)!!
    private val arrowSize: Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, sizeDp.toFloat(), context.resources.displayMetrics
    ).toInt()

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val layoutManager = parent.layoutManager as? LinearLayoutManager ?: return

        val firstVisible = layoutManager.findFirstCompletelyVisibleItemPosition()
        val lastVisible = layoutManager.findLastCompletelyVisibleItemPosition()
        val itemCount = layoutManager.itemCount
        val halfHeight = parent.height / 2

        // Draw right arrow if not at end
        if (lastVisible < itemCount - 1) {
            val top = halfHeight - arrowSize / 2
            arrowDrawable.setBounds(0, top, arrowSize, top + arrowSize)
            arrowDrawable.isAutoMirrored = false
            arrowDrawable.draw(canvas)
        }

        // Draw left arrow if not at start
        if (firstVisible > 0) {
            val width = parent.width
            val top = halfHeight - arrowSize / 2
            canvas.save()
            canvas.scale(-1f, 1f, width - arrowSize / 2f, 0f)
            arrowDrawable.setBounds(width - arrowSize, top, width, top + arrowSize)
            arrowDrawable.draw(canvas)
            canvas.restore()
        }
    }
}

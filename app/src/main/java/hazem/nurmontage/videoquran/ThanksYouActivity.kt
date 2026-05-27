package hazem.nurmontage.videoquran

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import hazem.nurmontage.videoquran.Utils.MyVibrationHelper
import hazem.nurmontage.videoquran.databinding.ActivityThanksYouBinding
import nl.dionsegijn.konfetti.core.PartyFactory
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.Relative
import nl.dionsegijn.konfetti.core.Spread
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.xml.KonfettiView
import nl.dionsegijn.konfetti.xml.image.ImageUtil
import java.util.Arrays
import java.util.concurrent.TimeUnit

/**
 * "Thank You" screen shown after a donation or support action.
 *
 * Displays confetti animation, the donation amount, a thank-you message,
 * and a vibration effect on resume.
 */
class ThanksYouActivity : BaseActivity() {

    private lateinit var binding: ActivityThanksYouBinding

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityThanksYouBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply window insets for edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, windowInsets ->
            val insets: Insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        setStatusBarColor(Color.WHITE)

        // Display price and thanks message from intent extras
        if (intent != null) {
            binding.tvPriceDonate.text = String.format(
                getString(R.string.donate_hint),
                intent.getStringExtra(EXTRA_PRICE)
            )
            binding.tvThnksDonate.text = getString(R.string.thanks_hint)
        }

        explode()

        binding.btnOnBack.setOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }
    }

    /**
     * Launch the confetti explosion animation.
     */
    private fun explode() {
        val konfettiView = binding.konfettiView
        konfettiView.start(
            PartyFactory(
                Emitter(2800L, TimeUnit.MILLISECONDS).max(MAX_PARTICLES)
            )
                .spread(Spread.ROUND)
                .shapes(
                    Arrays.asList(
                        Shape.Square,
                        Shape.Circle,
                        ImageUtil.loadDrawable(
                            ContextCompat.getDrawable(applicationContext, R.drawable.favorite_24px),
                            true,
                            true
                        )
                    )
                )
                .colors(Arrays.asList(0xFCDECA, 0xFF6EED, 0xF42EE5, 0xB4A0EF))
                .setSpeedBetween(0f, 30f)
                .position(Relative(0.5, 0.3))
                .getParty()
        )
    }

    override fun onResume() {
        super.onResume()
        playVibration()
    }

    override fun playVibration() {
        MyVibrationHelper.vibrate(this, 250L)
    }

    companion object {
        private const val EXTRA_PRICE = "price"
        // JADX decompilation resolved this to MaterialCardViewHelper.DEFAULT_FADE_ANIM_DURATION (150)
        private const val MAX_PARTICLES = 150
    }
}

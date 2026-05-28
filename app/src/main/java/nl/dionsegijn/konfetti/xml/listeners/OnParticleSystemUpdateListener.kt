package nl.dionsegijn.konfetti.xml.listeners

import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.xml.KonfettiView

interface OnParticleSystemUpdateListener {
    fun onParticleSystemEnded(view: KonfettiView, party: Party, activeSystems: Int)
    fun onParticleSystemStarted(view: KonfettiView, party: Party, activeSystems: Int)
}

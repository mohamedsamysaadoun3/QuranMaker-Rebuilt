package nl.dionsegijn.konfetti.core.models

data class ReferenceImage(val reference: Int, override val width: Int, override val height: Int) : CoreImage

package no.nav.dagpenger.beregning

import java.math.BigDecimal

data class Rapportering(
    val type: RapporteringDagType? = null,
    val timer: BigDecimal = BigDecimal.ZERO
) {
    fun harRettPåDagpenger() = when (type) {
        RapporteringDagType.Ferie, RapporteringDagType.Sykdom -> false
        else -> true
    }
}

enum class RapporteringDagType {
    Ledig,
    Sykdom,
    Ferie,
    Arbeid
}
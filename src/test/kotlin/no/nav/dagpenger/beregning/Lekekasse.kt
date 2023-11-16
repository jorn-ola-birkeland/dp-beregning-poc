package no.nav.dagpenger.beregning

import no.nav.familie.ba.sak.kjerne.tidslinje.Periode
import no.nav.familie.ba.sak.kjerne.tidslinje.tidslinje
import no.nav.familie.ba.sak.kjerne.tidslinje.util.aug
import no.nav.familie.ba.sak.kjerne.tidslinje.util.print

fun main(args: Array<String>) {
    val dagpengeBeregning = DagpengeBeregning(
        dagsatsTidslinje = tidslinje(
            Periode(7.aug(2023), 11.aug(2023), 1748),
            Periode(12.aug(2023), 13.aug(2023), 0),
            Periode(14.aug(2023), 18.aug(2023), 952),
            Periode(19.aug(2023), 20.aug(2023), 0)
        ),
        vanligArbeidstidPerDagTidslinje = tidslinje(
            Periode(7.aug(2023), 11.aug(2023), 6.0.toBigDecimal()),
            Periode(12.aug(2023), 13.aug(2023), 0.0.toBigDecimal()),
            Periode(14.aug(2023), 18.aug(2023), 6.0.toBigDecimal()),
            Periode(19.aug(2023), 20.aug(2023), 0.0.toBigDecimal())
        ),
        rapporteringTidslinje = tidslinje(
            7.aug(2023).arbeid(2.5),
            8.aug(2023).arbeid(3.5),
            9.aug(2023).syk(),
            10.aug(2023).arbeid(7.0),
            11.aug(2023).ledig(),
            12.aug(2023).arbeid(4.0),
            13.aug(2023).ledig(),
            14.aug(2023).ledig(),
            15.aug(2023).ledig(),
            16.aug(2023).ledig(),
            17.aug(2023).ledig(),
            18.aug(2023).ferie(),
            19.aug(2023).ferie(),
            20.aug(2023).ferie(),
        )
    )

    dagpengeBeregning.utbetalingTidslinje.print()
    println(dagpengeBeregning.utbetalingTidslinje.sumVerdi())
    println()
    dagpengeBeregning.avrundetUtbatalingTidslinje.print()
    println(dagpengeBeregning.avrundetUtbatalingTidslinje.sumVerdi())

}

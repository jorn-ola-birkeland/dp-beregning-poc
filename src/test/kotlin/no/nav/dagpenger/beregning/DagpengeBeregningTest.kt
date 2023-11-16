package no.nav.dagpenger.beregning

import no.nav.familie.ba.sak.kjerne.tidslinje.Periode
import no.nav.familie.ba.sak.kjerne.tidslinje.Tidslinje
import no.nav.familie.ba.sak.kjerne.tidslinje.tidslinje
import no.nav.familie.ba.sak.kjerne.tidslinje.tidspunkt.Dag
import no.nav.familie.ba.sak.kjerne.tidslinje.tidspunkt.Tidspunkt
import no.nav.familie.ba.sak.kjerne.tidslinje.tidsrom.TidspunktClosedRange
import no.nav.familie.ba.sak.kjerne.tidslinje.tidsrom.rangeTo
import no.nav.familie.ba.sak.kjerne.tidslinje.tilPeriodeMedInnhold
import no.nav.familie.ba.sak.kjerne.tidslinje.util.aug
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DagpengeBeregningTest {

    @Test
    fun `enkelt case`() {
        val dagpengeBeregning = DagpengeBeregning(
            dagsatsTidslinje = tidslinje(
                Periode(7.aug(2023), 11.aug(2023), 1748),
                Periode(12.aug(2023), 13.aug(2023), 0),
                Periode(14.aug(2023), 18.aug(2023), 1748),
                Periode(19.aug(2023), 20.aug(2023), 0)
            ),
            vanligArbeidstidPerDagTidslinje = tidslinje(
                Periode(7.aug(2023), 11.aug(2023), 6.0.toBigDecimal()),
                Periode(12.aug(2023), 13.aug(2023), 0.0.toBigDecimal()),
                Periode(14.aug(2023), 18.aug(2023), 6.0.toBigDecimal()),
                Periode(19.aug(2023), 20.aug(2023), 0.0.toBigDecimal())
            ),
            rapporteringTidslinje = tidslinje(
                ledig(7.aug(2023)..20.aug(2023)),
            )
        )

        val forventet = tidslinje(
            Periode(7.aug(2023), 11.aug(2023), 1748),
            Periode(12.aug(2023), 13.aug(2023), 0),
            Periode(14.aug(2023), 18.aug(2023), 1748),
            Periode(19.aug(2023), 20.aug(2023), 0),
        )

        assertEquals(forventet, dagpengeBeregning.avrundetUtbatalingTidslinje)
        assertEquals(17480, dagpengeBeregning.avrundetUtbatalingTidslinje.sumVerdi())
        assertEquals(14, dagpengeBeregning.avrundetUtbatalingTidslinje.dagerMedSkattetrekk())
    }

    @Test
    fun `komplisert case`() {
        val dagpengeBeregning = DagpengeBeregning(
            dagsatsTidslinje = tidslinje(
                Periode(7.aug(2023), 11.aug(2023), 1748),
                Periode(12.aug(2023), 13.aug(2023), 0),
                Periode(14.aug(2023), 18.aug(2023), 1748),
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

        val forventet = tidslinje(
            Periode(7.aug(2023), 7.aug(2023), 878),
            Periode(8.aug(2023), 8.aug(2023), 627),
            Periode(11.aug(2023), 11.aug(2023), 1506),
            Periode(13.aug(2023), 13.aug(2023), 0),
            Periode(14.aug(2023), 17.aug(2023), 1505),
        )

        assertEquals(forventet, dagpengeBeregning.avrundetUtbatalingTidslinje)
        assertEquals(9031, dagpengeBeregning.avrundetUtbatalingTidslinje.sumVerdi())
        assertEquals(8, dagpengeBeregning.avrundetUtbatalingTidslinje.dagerMedSkattetrekk())
    }

    @Test
    fun `endring i dagsats`() {
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

        val forventet = tidslinje(
            Periode(7.aug(2023), 7.aug(2023), 878),
            Periode(8.aug(2023), 8.aug(2023), 627),
            Periode(11.aug(2023), 11.aug(2023), 1505),
            Periode(13.aug(2023), 13.aug(2023), 0),
            Periode(14.aug(2023), 17.aug(2023), 820),
        )

        assertEquals(forventet, dagpengeBeregning.avrundetUtbatalingTidslinje)
        assertEquals(6290, dagpengeBeregning.avrundetUtbatalingTidslinje.sumVerdi())
        assertEquals(8, dagpengeBeregning.avrundetUtbatalingTidslinje.dagerMedSkattetrekk())
    }

    @Test
    fun `har ikke rett på dagpenger pga for mye jobb`() {
        val dagpengeBeregning = DagpengeBeregning(
            dagsatsTidslinje = tidslinje(
                Periode(7.aug(2023), 11.aug(2023), 1748),
                Periode(12.aug(2023), 13.aug(2023), 0),
                Periode(14.aug(2023), 18.aug(2023), 1748),
                Periode(19.aug(2023), 20.aug(2023), 0)
            ),
            vanligArbeidstidPerDagTidslinje = tidslinje(
                Periode(7.aug(2023), 11.aug(2023), 6.0.toBigDecimal()),
                Periode(12.aug(2023), 13.aug(2023), 0.0.toBigDecimal()),
                Periode(14.aug(2023), 18.aug(2023), 6.0.toBigDecimal()),
                Periode(19.aug(2023), 20.aug(2023), 0.0.toBigDecimal())
            ),
            rapporteringTidslinje = tidslinje(
                7.aug(2023).arbeid(6.0),
                8.aug(2023).arbeid(6.0),
                9.aug(2023).arbeid(6.0),
                10.aug(2023).arbeid(6.0),
                11.aug(2023).arbeid(7.0),
                ledig(12.aug(2023)..20.aug(2023)),
            )
        )

        assertEquals(tidslinje(), dagpengeBeregning.avrundetUtbatalingTidslinje)
        assertEquals(0, dagpengeBeregning.avrundetUtbatalingTidslinje.sumVerdi())
        assertEquals(0, dagpengeBeregning.avrundetUtbatalingTidslinje.dagerMedSkattetrekk())
    }
}

fun Tidspunkt<Dag>.ledig() = this.tilPeriodeMedInnhold(Rapportering(RapporteringDagType.Ledig))
fun Tidspunkt<Dag>.ferie() = this.tilPeriodeMedInnhold(Rapportering(RapporteringDagType.Ferie))
fun Tidspunkt<Dag>.syk() = this.tilPeriodeMedInnhold(Rapportering(RapporteringDagType.Sykdom))
fun Tidspunkt<Dag>.arbeid(timer: Double) =
    this.tilPeriodeMedInnhold(Rapportering(RapporteringDagType.Arbeid, timer.toBigDecimal()))

fun ledig(tidsrom: TidspunktClosedRange<Dag>) = Periode(tidsrom, Rapportering(RapporteringDagType.Ledig))
fun <I> Tidslinje<I, Dag>.dagerMedSkattetrekk() = this.foldVerdi(0) { acc, i -> acc + 1 }
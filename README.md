# Beregning av dagpenger

Proof-of-concept av løsning for å beregne utbetalinger for en dagpengeperiode. Den håndterer ulike prinsipper for beregning, som gjennomsnittsberegning og forholdsberegning. 

Utbetalingsdager med samme beløp blir slått sammen til utbetalingsperioder over flere dager.

Det skilles i utgangspunktet mellom _0-utbetalinger_ og _ingen utbetaling_. 0-utbetalinger regnes som en ubetaling, og utnyttes for å fastsette riktig antall dager med skattetrekk. 

Avrunding blir gjort på totalsum (til nærmeste hele krone), og utbetalingsdagene tilpasses slik at summen av dem gir totalutbetalingen 

Vi benytter oss av `tidslinje`-bibliotektet som finnes i [PO Familie/barnetrygd](https://github.com/navikt/familie-ba-sak/tree/main/src/main/kotlin/no/nav/familie/ba/sak/kjerne/tidslinje). 
Det er et sett av funksjoner som lar en utvikler operere samlet på en serie av tidsperioder, og der hvert tidspunkt i en tidsperiode kan ha en verdi knyttet til seg. 

## Input

For å gjøre beregning trengs tre tidslinjer for beregningsperioden (2 uker):
* Dagsats, der verdien er i hele kroner
* Fastsatt abeidstid, der verdien er i timer
* Rappotering, der dagpengemottaker angir ledighet, sykdom, ferie og utført arbeid (i timer)

### Dagsats

Dagsats fastsettes av saksbehandler. 
Det er mulig at dagsatsen er forskjellig i løpet av en periode. 
Dagsatsen er et heltall og gis man-fre. I helgene settes satsen til 0.

| Uke | Man  | Tir  | Ons  | Tor  | Fre  | Lør | Søn | 
|-----|------|------|------|------|------|-----|-----|
| 1   | 1748 | 1748 | 1748 | 1748 | 1748 | 0   | 0   |
| 2   | 1748 | 1748 | 1748 | 1748 | 1748 | 0   | 0   |

### Fastsatt arbeidstid

Saksbehandler fastsetter også vanlig arbeidstid.
Arbeidstid fastsettes for hele perioden (14 dager), f.eks 75 timer (full jobb) eller 60 timer (80% stilling).
Fastsatt arbeidstid fordeles vanligvis likt på de 10 hverdagene, f.eks 60/10 = 6 timer/dag. I helgene settes 0. 
Det er mulig at fastsatt arbeidstid er ulik i løpet av perioden, og da må input tilpasses tilsvarende.

| Uke | Man | Tir | Ons | Tor | Fre | Lør | Søn | 
|-----|-----|-----|-----|-----|-----|-----|-----|
| 1   | 6,0 | 6,0 | 6,0 | 6,0 | 6,0 | 0,0 | 0,0 |
| 2   | 6,0 | 6,0 | 6,0 | 6,0 | 6,0 | 0,0 | 0,0 |

### Rapportering

Dagpengemottaker sender inn rapportering etter hver periode. Der angis det:

* Dag med sykdom (S)
* Dag med ferie (F)
* Dag med arbeid (# timer)
* Dag med full ledighet (L)

| Uke | Man | Tir | Ons | Tor | Fre | Lør | Søn | 
|-----|-----|-----|-----|-----|-----|-----|-----|
| 1   | 2,5 | 3,5 | S   | 7,0 | L   | 4,0 | L   |
| 2   | L   | L   | L   | L   | F   | F   | F   |

## Beregning

### Gjennomsnittsberegning
Det er rettsgrunnlag for å gjennomsnittsberegne dagpengene for dager det er rett til dagpenger. 
Dager med rett til dagpenger blir i eksemplet 8 pga en dag med sykdom (onsdag uke 1) og en dag med ferie (fredag uke 2).
Dagpengemottakeren har jobbet til sammen 17 timer. Ved gjennomsnittsberegning fordeles de likt på dagene med rett til dagpenger. 
Timer per dag blir da 17 timer/8 =  2,125 timer. 

For å gjøre gjennomsnittsberegning tilpasses rapporteringen som sendes til beregning:

| Uke | Man | Tir | Ons | Tor | Fre | Lør | Søn | 
|-----|-----|-----|-----|-----|-----|-----|-----|
| 1   |2,125|2,125| S   |2,125|2,125| L   | L   |
| 2   |2,125|2,125|2,125|2,125| F   | F   | F   |

Resultatet blir dette:

| Uke | Man  | Tir  | Ons  | Tor  | Fre  | Lør | Søn | 
|-----|------|------|------|------|------|-----|-----|
| 1   | 1129 | 1129 |      | 1129 | 1129 |  0  |  0  |
| 2   | 1129 | 1129 | 1129 | 1128 |      |     |     |

Som returnes som følgende perioder hvis mandag i uke 1 er 7. august 2023:

    2023-08-07 - 2023-08-08: 1129
    2023-08-10 - 2023-08-11: 1129
    2023-08-12 - 2023-08-13: 0
    2023-08-14 - 2023-08-16: 1129
    2023-08-17 - 2023-08-17: 1128

Til sammen blir utbetalingen kr 9031. Det at torsdag i uke 2 får 1 krone mindre enn de øvrige dagene, skyldes avrunding. Det forklares i et eget avsnitt. 

Merk at det utbetales 0 kroner i helgen i uke 1, mens det ikke gjøres utbetalinger i uke 2. 
Årsaken er at helgen regnes med i trekkperioden for skatt, med mindre det er ferie, som det er her. 
Trekkperioden regnes som alle dager med utbetaling (inklusive 0-utbetalinger). 
Her vil dermed trekkperioden regnes som 10 dager. 

### Forholdsberegning
Forholdsberegning innebærer å redusere dagutbetalingene etter hvor mye som jobbes hver dag, og prøver å løse:

- Riktig brutto totalbeløp, også ved satsendring i perioden
- Riktig skattetrekk: Det skal trekkes skatt for dager der dagpengemottaker mottar utbetalinger
- Riktig rapportering: Dagpengeperioder kan avvike fra rapporteringsperioder, f.eks månedlige. Derfor bør hvert dagsbeløp være slik at en annen gruppering/oppdeling gir korrekt bilde. 
- Logisk for bruker: Det gis mindre dagpengeutbetaling dager hvor det jobbes

I tilfellet der det jobbes minst like mye som fastsatt arbeidsdag, utbetales det ikke dagpenger.
Overskytende arbeid, dvs arbeid utover fastsatt arbeidsdag, fører til forholdmessig reduksjon på øvrige utbetalingssdager. 

Resultatet blir:

| Uke | Man  | Tir  | Ons  | Tor  | Fre  | Lør | Søn | 
|-----|------|------|------|------|------|-----|-----|
| 1   | 878  | 627  |      |      | 1506 |     | 0   |
| 2   | 1505 | 1505 | 1505 | 1505 |      |     |     |

som returneres som følgende perioder:

    2023-08-07 - 2023-08-07: 878
    2023-08-08 - 2023-08-08: 627
    2023-08-11 - 2023-08-11: 1506
    2023-08-13 - 2023-08-13: 0
    2023-08-14 - 2023-08-17: 1505

Til sammen blir brutto dagpengebeløp kr 9 031, altså det samme som for gjennomsnittsberegning. 
Trekkperioden blir her 8 dager, 2 mindre enn gjennomsnittsberegningen førte til. 
Det skyldes at det er jobbet utover fastsatt arbeidstid på torsdag (7 timer mot fastsatt 6 timer) og lørdag (4 timer mot fastsatt 0 timer) i uke 1. De utbetales det ikke dagpenger for. 

### Avrunding
Nøyaktig totalbeløp til utbetaling (både gjennomsnitts- og forholdsmessig beregnet) er kr 9 031,33333... 
Det skal rundes til nærmeste hele krone, altså kr 9 031. 

For å oppnå dagbeløp som summerer til totalbeløpet, 
fjernes først desimaldelen av beløpet (i praksis rundes beløpet ned). For forholdsmessig beregning blir det slik:

    2023-08-07 - 2023-08-07: 878.0462962...->  878  
    2023-08-08 - 2023-08-08: 627.1759259...->  627  
    2023-08-11 - 2023-08-11: 1505.222222...-> 1505  
    2023-08-13 - 2023-08-13: 0             ->    0
    2023-08-14 - 2023-08-17: 1505.222222...-> 1505
                             --------------   ----
                        Sum: 9301.333333...   9300
                   Avrundet: 9301             9300 (mangler 1)

Summen av av dagbeløpene uten desimaldel blir kr 9 300. Da mangler det én krone fra riktig avrundet totalbeløp. 
Det gis til det første dagbeløpet med høyest desimaldel. Det er her 2023-08-11 med desimaldel 0,2222... . 
Heltallsbeløpet for den dagen blir derfor kr 1506. 

Med alt annet likt, men dagsatsen justert til kr 1 753, blir avviket større:

    2023-08-07 - 2023-08-07: 880.55787037... ->  880
    2023-08-08 - 2023-08-08: 628.96990740... ->  628
    2023-08-11 - 2023-08-11: 1509.5277777... -> 1509
    2023-08-13 - 2023-08-13: 0               ->    0
    2023-08-14 - 2023-08-17: 1509.5277777... -> 1509
                        Sum: 9057.1666666...    9053 
                             ---------------    ----
                   Avrundet: 9057               9053 (mangler 4)

Beløpet 2023-08-08 har største desimaldel (0.96990..),etterfulgt av 2023-08-08 (0.55787...).
Deretter er det lik desimaldel de øvrige datoene (0.52777...), så da velges de to tidligste datoene, 2023-08-11 og 2023-08-14. 
Disse 4 dagene får en krone ekstra lagt til på heltallsdelen. 

Resultatet blir:

    2023-08-07 - 2023-08-07:  881
    2023-08-08 - 2023-08-08:  629
    2023-08-11 - 2023-08-11: 1510
    2023-08-13 - 2023-08-13:    0
    2023-08-14 - 2023-08-14: 1510
    2023-08-15 - 2023-08-17: 1509
                             ----
                        Sum: 9057 

### Endring av dagsats i perioden
Vi skal se på et tilfelle der rapportering og arbeidstid er uendret, men der satsen i uke 2 reduseres:

| Uke | Man  | Tir  | Ons  | Tor  | Fre  | Lør | Søn | 
|-----|------|------|------|------|------|-----|-----|
| 1   | 1748 | 1748 | 1748 | 1748 | 1748 | 0   | 0   |
| 2   | 952  | 952  | 952  | 952  | 952  | 0   | 0   |

Gjennomsnittsberegning gir følgende resultat:

| Uke | Man  | Tir  | Ons  | Tor  | Fre  | Lør | Søn | 
|-----|------|------|------|------|------|-----|-----|
| 1   | 1129 | 1129 |      | 1129 | 1129 |  0  |  0  |
| 2   | 615  | 615  | 615  | 614  |      |     |     |

Til sammen kr 6 975. 

Forholdsberegnet resultat med utgangspunkt i samme rapportering og fastsatt arbeidstid blir i stedet:

| Uke | Man  | Tir  | Ons  | Tor  | Fre  | Lør | Søn | 
|-----|------|------|------|------|------|-----|-----|
| 1   | 878  | 627  |      |      | 1505 |     | 0   |
| 2   | 820  | 820  | 820  | 820  |      |     |     |

Til sammen kr 6 290.

Avviket på nesten kr 700 (k 6 6290 i stedet for kr 6 975) skyldes at gjennomsnittsberegning utbetaler for relativt flere timer med høy sats. 

Merk at for forholdsberegning reduseres også utbetalt dagsbeløp fra kr 952 til kr 820 i uke 2 pga det overskytende arbeidet i uke 1. Hele reduksjonen _burde_ strengt tatt ha kommet i uke 1, siden det var der ekstraarbeidet skjedde. Men det går ikke an å garantere det vil fungere generelt. F.eks vil følgende rapportering kreve at reduksjonen skjer i uke 2, selv om det jobbes utover fastsatt arbeidstid i uke 1: 

| Uke | Man | Tir | Ons | Tor | Fre | Lør | Søn | 
|-----|-----|-----|-----|-----|-----|-----|-----|
| 1   | 8,0 | 8,0 | S   | S   | S   | L   | L   |
| 2   | L   | L   | L   | L   | L   | L   | L   |

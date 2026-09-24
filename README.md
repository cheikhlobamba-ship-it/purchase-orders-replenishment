# Purchase orders for stock replenishment

Applicazione Java che, dato un articolo e una quantità, individua i fornitori con
stock sufficiente, calcola il totale d'ordine applicando gli sconti previsti da
ciascuno ed evidenzia il fornitore più economico, mostrando anche i giorni minimi
di spedizione.

Progetto realizzato come test di ingresso per Regesta.

---

## Come eseguire

Requisiti: **JDK 25** (o qualsiasi versione dalla 17 in su).

```
git clone https://github.com/cheikhlobamba-ship-it/purchase-orders-replenishment.git
cd purchase-orders-replenishment
java Server.java
```

Poi aprire il browser su **http://localhost:8080**.

Non sono richieste dipendenze esterne, build tool o database: il progetto usa
esclusivamente la libreria standard di Java.

---

## Guida all'uso

1. Aprire http://localhost:8080
2. Inserire la **quantità** da ordinare (default: 12)
3. Selezionare il **mese dell'ordine**, che determina l'applicabilità degli
   sconti stagionali
4. Premere **Calcola**

Il risultato mostra, per ogni fornitore in grado di evadere l'ordine, il totale
scontato e i giorni minimi di spedizione. Il fornitore più economico è
evidenziato. I fornitori senza stock sufficiente non compaiono nell'elenco.

### Caso di esempio 1 — ordine a settembre

Input: 12 pezzi, mese di settembre.

| Fornitore | Totale | Giorni | Note |
|---|---|---|---|
| Fornitore 1 | — | — | escluso: solo 8 pezzi disponibili |
| Fornitore 2 | 1.459,20 € | 7 | |
| Fornitore 3 | **1.441,19 €** | 4 | più economico |

### Caso di esempio 2 — ordine a novembre

Input: 12 pezzi, mese di novembre.

| Fornitore | Totale | Giorni | Note |
|---|---|---|---|
| Fornitore 1 | — | — | escluso: solo 8 pezzi disponibili |
| Fornitore 2 | **1.459,20 €** | 7 | più economico |
| Fornitore 3 | 1.470,60 € | 4 | perde lo sconto stagionale |

Il secondo caso mostra il punto centrale del problema: il fornitore più
conveniente cambia al variare della data, e il più economico non è
necessariamente il più veloce. La scelta finale resta all'utente, per questo
i giorni di spedizione sono sempre visibili accanto al prezzo.

---

## Architettura

```
Main.java             avvio da riga di comando (output su console)
Server.java           server HTTP, espone il form e la pagina dei risultati
Catalogo.java         sorgente dei dati: fornitori, prezzi, stock, sconti
Offerta.java          offerta di un fornitore per un articolo
Sconto.java           interfaccia comune a tutte le regole di sconto
ScontoSoglia.java     sconto sul valore totale dell'ordine
ScontoQuantita.java   sconto a fasce sulla quantità ordinata
ScontoMese.java       sconto stagionale legato al mese
index.html            interfaccia utente
style.css             foglio di stile
```

### Le regole di sconto

Il cuore del progetto è l'interfaccia `Sconto`:

```java
public interface Sconto {
    double calcola(double lordo, int quantita, int mese);
}
```

Ogni tipologia di sconto è una classe separata che implementa questa
interfaccia. Ogni offerta possiede una lista di sconti, che vengono applicati in
sequenza sul totale.

Questa impostazione fa sì che l'aggiunta di un nuovo tipo di
sconto — per esempio uno riservato a un cliente specifico — richieda solo una
nuova classe, senza modificare né il calcolo né le classi esistenti.

### Separazione dei livelli

La logica di calcolo non dipende né dall'interfaccia web né dall'origine dei
dati. `Server` si limita a leggere i parametri della richiesta e a formattare il
risultato; `Catalogo` è l'unico punto in cui i dati sono definiti. Sostituirlo
con una lettura da database richiederebbe di modificare solo quella classe.

---

## Scelte tecniche

**Java senza framework.** La consegna lasciava libertà di scelta. Ho preferito
la libreria standard per mantenere il progetto avviabile con un solo comando, 
senza installazioni preliminari da parte di chi lo valuta. 

**Dati definiti nel codice.** La consegna indica esplicitamente questa
possibilità. I dati sono isolati in `Catalogo`, che funge da unico punto di
accesso: l'introduzione di un database non toccherebbe il resto del codice.

**Interfaccia web con HTML e CSS**, come richiesto, servita direttamente
dall'applicazione.

---

## Regole di calcolo

Il testo del problema lascia alcuni punti aperti. Di seguito le interpretazioni
adottate, verificate contro i risultati attesi dagli esempi.

**Le fasce sulla quantità non sono cumulabili.** Il Fornitore 2 offre il 3% oltre
i 5 pezzi e il 5% oltre i 10. Su 12 pezzi si applica solo la fascia migliore:
1536 × 0,95 = 1.459,20 €. Sommando le due fasce il risultato non coincide con
quello atteso.

**Sconti di natura diversa si applicano in cascata.** Il Fornitore 3, a
settembre, applica prima il 5% sul valore e poi il 2% stagionale sul risultato:
1548 × 0,95 × 0,98 = 1.441,19 €. Applicando un 7% complessivo si otterrebbe
1.439,64 €, valore diverso da quello indicato.

**Le soglie in euro si valutano sull'imponibile lordo**, prima dell'applicazione
di qualsiasi sconto.

**"Minimum" e "over" sono condizioni distinte.** Il Fornitore 1 offre lo sconto a
partire da 1000€ inclusi (≥), il Fornitore 3 oltre i 1000€ (>). La distinzione è
gestita da un apposito parametro di `ScontoSoglia`.

**Il controllo sullo stock usa `>=`**: un fornitore con esattamente la quantità
richiesta è in grado di evadere l'ordine.

---

## Possibili miglioramenti

- `ScontoQuantita` usa due array paralleli per soglie e percentuali. Una lista di
  oggetti "fascia" sarebbe più robusta, rendendo impossibile per costruzione il
  disallineamento tra i due array.
  
- Il catalogo è attualmente limitato a un singolo articolo. L'estensione a più
  articoli richiederebbe l'aggiunta di un'entità `Articolo` e un filtro
  preliminare sulle offerte.
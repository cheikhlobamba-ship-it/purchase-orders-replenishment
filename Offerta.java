import java.util.List;

public class Offerta {
    String fornitore;
    double prezzo;
    int pezziDisponibili;
    int giorniSpedizione;
    List<Sconto> sconti; 

    public Offerta(String fornitore, double prezzo, int pezziDisponibili,
                   int giorniSpedizione, List<Sconto> sconti) {
        this.fornitore = fornitore;
        this.prezzo = prezzo;
        this.pezziDisponibili = pezziDisponibili;
        this.giorniSpedizione = giorniSpedizione;
        this.sconti = sconti;  
    }

    public double totaleLordo(int quantita) {
        return prezzo * quantita;
    }

    public double totaleNetto(int quantita, int mese) {
        double lordo = totaleLordo(quantita);
        double totale = lordo;
        for (Sconto sconto : sconti) {
            double scontoApplicato = sconto.calcola(lordo, quantita, mese);
            totale -= totale * scontoApplicato;
        }
        return totale;
    }
}
package ordini;
public class ScontoMese implements Sconto {
    int mese;
    double percentuale;

    public ScontoMese(int mese, double percentuale) {
        this.mese = mese;
        this.percentuale = percentuale;
    
    } 

    @Override
     public double calcola(double lordo, int quantita, int mese) {
        if (mese == this.mese) {
            return percentuale;
        }
        return 0;
    } 
}


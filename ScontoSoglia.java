public class ScontoSoglia implements Sconto {
    double soglia;
    double percentuale;
    boolean sogliaInclusa;

    public ScontoSoglia(double soglia, double percentuale, boolean sogliaInclusa) {
        this.soglia = soglia;
        this.percentuale = percentuale;
        this.sogliaInclusa = sogliaInclusa;
    }
    
    @Override
    public double calcola(double lordo, int quantita, int mese) {
        
        if (sogliaInclusa) {
            if (lordo >= soglia) {
                return percentuale;
            }
        } else {
            if (lordo > soglia) {
                return percentuale;
            }
        }
        return 0;
    }
}
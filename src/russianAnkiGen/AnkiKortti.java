package russianAnkiGen;

/**
 * Luokka Anki-sovelluksen sanakortille
 * @author Olli
 * @version 7.2.2020
 *
 */
public class AnkiKortti {
    
    private final char erotin = '|';
    private String etupuoli, takapuoli, tunniste;
    
    /**
     * Konstruktori
     * @param etupuoli Kortin etupuolen teksti eli kysymys
     * @param takapuoli Kortin takapuolen teksti eli vastaus
     * @param tunniste Kortin tunniste, jonka perusteella korttia voi hakea
     */
    public AnkiKortti(String etupuoli, String takapuoli, String tunniste) {
        this.etupuoli = etupuoli;
        this.takapuoli = takapuoli;
        this.tunniste = tunniste;
    }
    
    @Override
    public String toString() {
        return etupuoli + erotin + takapuoli + erotin + tunniste;
    }
    
    @Override
    public boolean equals(Object toinen) {
        if (toinen == null) {
           return false;
        }
        if (this.getClass() != toinen.getClass()) {
           return false;
        }
        if(toinen.toString().equals(this.toString())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
    
}

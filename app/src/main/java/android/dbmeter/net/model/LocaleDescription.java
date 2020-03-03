package android.dbmeter.net.model;

public class LocaleDescription {
    private String LocaleCode;
    private String LocaleName;

    public LocaleDescription(){
        LocaleCode="";
        LocaleName="";
    }

    public LocaleDescription(String localeCode, String localeName){
        LocaleCode = localeCode;
        LocaleName = localeName;
    }

    public String getLocaleCode() {
        return LocaleCode;
    }

    public void setLocaleCode(String localeCode) {
        this.LocaleCode = localeCode;
    }

    public String getLocaleName() {
        return LocaleName;
    }

    public void setLocaleName(String localeName) {
        this.LocaleName = localeName;
    }
}

package android.dbmeter.net.database;

import android.content.Context;
import android.dbmeter.net.R;
import android.dbmeter.net.model.LocaleDescription;

import java.util.ArrayList;

public class LocaleDescriptionDatabase {
    private static ArrayList<LocaleDescription> databases = new ArrayList<>();

    private static void init(Context context) {
        databases.add(new LocaleDescription(context.getString(R.string.locale_en),context.getString(R.string.lang_en)));
        databases.add(new LocaleDescription(context.getString(R.string.locale_vi),context.getString(R.string.lang_vi)));
    }

    public static ArrayList<LocaleDescription> get(Context context) {
        if (databases.size() != 0)
            databases.clear();
        init(context);

        return databases;
    }

    public static ArrayList<String> getLocaleNameArrayList(){
        ArrayList<String> result = new ArrayList<>();

        for (LocaleDescription locale : databases) {
            result.add(locale.getLocaleName());
        }

        return result;
    }

    public static String getLocaleNameFromCode(String localeCode){
        String result = "";

        for (LocaleDescription locale: databases) {
            if(locale.getLocaleCode().equals(localeCode)){
                result = locale.getLocaleName();
            }
        }

        return result;
    }

    public static String getLocaleCodeFromName(String localeName){
        String result = "";

        for (LocaleDescription locale: databases) {
            if(locale.getLocaleName().equals(localeName)){
                result = locale.getLocaleCode();
            }
        }

        return result;
    }
}

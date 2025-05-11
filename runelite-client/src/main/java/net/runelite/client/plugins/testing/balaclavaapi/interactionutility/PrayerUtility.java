package net.runelite.client.plugins.testing.balaclavaapi.interactionutility;

import net.runelite.client.plugins.testing.balaclavaapi.utility.ClickWidget;
import net.runelite.client.plugins.testing.ethanapi.collections.Widgets;
import net.runelite.api.Client;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.Varbits;
import net.runelite.client.RuneLite;

import java.util.ArrayList;
import java.util.List;

import static net.runelite.api.Varbits.QUICK_PRAYER;

public class PrayerUtility {
    static Client client = RuneLite.getInjector().getInstance(Client.class);

    public static boolean isQuickPrayerEnabled() {
        return client.getVarbitValue(QUICK_PRAYER) == 1;
    }


    public static void activateQuickPrayer(){
        if(isQuickPrayerEnabled()){
            return;
        }
        Widgets.search().withId(10485779).withAction("Activate").first().ifPresent(quickPrayer ->{
            ClickWidget.widgetAction(quickPrayer,"Activate");
        });
    }

    public static void deactivateQuickPrayer(){
        if(!isQuickPrayerEnabled()){
            return;
        }
        Widgets.search().withId(10485779).withAction("Deactivate").first().ifPresent(quickPrayer ->{
            ClickWidget.widgetAction(quickPrayer,"Deactivate");
        });
    }

    public static void toggleQuickPrayer(){
        if(client.getVarbitValue(Varbits.QUICK_PRAYER) == 1){
            deactivateQuickPrayer();
        } else {
            activateQuickPrayer();
        }
    }
    
    public static void activate(Prayer prayer){
        if(client.isPrayerActive(prayer)){
            return;
        }

        String input = prayer.name().toLowerCase();
        String output = input.replace("_", " ");
        
        Widgets.search().nameMatchesWildCardNoCase(output).first().ifPresent(prayWidget-> ClickWidget.widgetAction(prayWidget,"Activate"));

    }

    public static void activate(List<Prayer> prayer){
        for(Prayer p : prayer) {
           activate(p);
        }
    }
    public static void deactivate(Prayer prayer){
        if(!client.isPrayerActive(prayer)){
            return;
        }
        String input = prayer.name().toLowerCase();
        String output = input.replace("_", " ");

        Widgets.search().nameMatchesWildCardNoCase(output).first().ifPresent(prayWidget-> ClickWidget.widgetAction(prayWidget,"Deactivate"));

    }
    public static void deactivateAll(){
        for(Prayer prayer : Prayer.values()){
            if(client.isPrayerActive(prayer)){
                deactivate(prayer);
            }
        }
    }
    public static void deactivateDefensive(){
        for(Prayer prayer : Prayer.values()){
            if(client.isPrayerActive(prayer)){
                if(prayer.equals(Prayer.PROTECT_FROM_MAGIC) || prayer.equals(Prayer.PROTECT_FROM_MISSILES) || prayer.equals(Prayer.PROTECT_FROM_MELEE)) {
                    deactivate(prayer);
                }
            }
        }
    }
    public static void deactivateOffensive(){
        for(Prayer prayer : Prayer.values()){
            if(client.isPrayerActive(prayer)){
                if(prayer.equals(Prayer.PROTECT_FROM_MAGIC) || prayer.equals(Prayer.PROTECT_FROM_MISSILES) || prayer.equals(Prayer.PROTECT_FROM_MELEE)) {
                    continue;
                }
                deactivate(prayer);
            }
        }
    }

    public static List<Prayer> getMeleeOffensive(){
        List<Prayer> prayerList = new ArrayList<>();
        if(client.getRealSkillLevel(Skill.PRAYER) >= 70
                && client.getRealSkillLevel(Skill.DEFENCE) >= 70
                && client.getVarbitValue(3909) >=8){ // status for camelot training unlock
            prayerList.add(Prayer.PIETY);
        } else if (client.getRealSkillLevel(Skill.PRAYER) >= 60
                && client.getRealSkillLevel(Skill.DEFENCE) >= 65
                && client.getVarbitValue(3909) >=8) {
            prayerList.add(Prayer.CHIVALRY);
        } else if (client.getRealSkillLevel(Skill.PRAYER) > 34){
            prayerList.add(Prayer.ULTIMATE_STRENGTH);
            prayerList.add(Prayer.INCREDIBLE_REFLEXES);
        } else if (client.getRealSkillLevel(Skill.PRAYER) > 16){
            prayerList.add(Prayer.SUPERHUMAN_STRENGTH);
            prayerList.add(Prayer.IMPROVED_REFLEXES);
        } else if (client.getRealSkillLevel(Skill.PRAYER) > 7){
            prayerList.add(Prayer.BURST_OF_STRENGTH);
            prayerList.add(Prayer.CLARITY_OF_THOUGHT);
        }

        return prayerList;
    }
    public static List<Prayer> getRangedOffensive(){
        List<Prayer> prayerList = new ArrayList<>();
        if(client.getRealSkillLevel(Skill.PRAYER) >= 74 && client.getRealSkillLevel(Skill.DEFENCE) >= 70 && client.getVarbitValue(5451) == 1){
            prayerList.add(Prayer.RIGOUR);
        } else if (client.getRealSkillLevel(Skill.PRAYER) >= 44) {
            prayerList.add(Prayer.EAGLE_EYE);
        } else if (client.getRealSkillLevel(Skill.PRAYER) >= 26) {
            prayerList.add(Prayer.HAWK_EYE);
        } else if (client.getRealSkillLevel(Skill.PRAYER) >= 8) {
            prayerList.add(Prayer.SHARP_EYE);
        }

        return prayerList;
    }
    public static List<Prayer> getMageOffensive(){
        List<Prayer> prayerList = new ArrayList<>();
        if(client.getRealSkillLevel(Skill.PRAYER) >= 77 && client.getRealSkillLevel(Skill.DEFENCE) >= 70 && client.getVarbitValue(5452) == 1){
            prayerList.add(Prayer.AUGURY);
        } else if (client.getRealSkillLevel(Skill.PRAYER) >= 45) {
            prayerList.add(Prayer.MYSTIC_MIGHT);
        } else if (client.getRealSkillLevel(Skill.PRAYER) >= 27) {
            prayerList.add(Prayer.MYSTIC_LORE);
        } else if (client.getRealSkillLevel(Skill.PRAYER) >= 9) {
            prayerList.add(Prayer.MYSTIC_WILL);
        }

        return prayerList;
    }
}

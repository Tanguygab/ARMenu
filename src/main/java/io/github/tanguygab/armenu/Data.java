package io.github.tanguygab.armenu;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.api.config.YamlConfigurationFile;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.api.placeholder.ServerPlaceholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Data {

    private ConfigurationFile file;
    private final Map<String,Map<String,String>> tempdata = new HashMap<>();
    private final Map<String,Map<String,String>> data = new HashMap<>();

    public Data() {
        try {
            File f = new File(ARMenu.get().getDataFolder(), "data.yml");
            if (!f.exists()) f.createNewFile();
            file = new YamlConfigurationFile(null, f);}
        catch (Exception e) {e.printStackTrace();}

        file.getValues().forEach((name,map)-> data.put(name+"",(Map<String, String>) map));

        PlaceholderManager pm = TabAPI.getInstance().getPlaceholderManager();
        List<String> player = new ArrayList<>();
        List<String> global = new ArrayList<>();
        data.forEach((name,map)->{
            if (name.equals("global")) global.addAll(map.keySet());
            else player.addAll(map.keySet());
        });
        player.forEach(dataname->pm.registerPlayerPlaceholder("%data-"+dataname+"%",-1,p->getData(p.getName(),dataname)).enableTriggerMode());
        global.forEach(dataname->pm.registerServerPlaceholder("%global-data-"+dataname+"%",-1,()->getData("global",dataname)).enableTriggerMode());

    }

    public void unload() {
        data.forEach((str,map)->file.set(str,map));
    }

    public String getData(String name, String dataname) {
        if (tempdata.containsKey(name) && tempdata.get(name).containsKey(dataname))
            return tempdata.get(name).get(dataname);
        if (data.containsKey(name) && data.get(name).containsKey(dataname))
            return data.get(name).get(dataname);
        return "";
    }

    public void setData(String name, String dataname, String value) {
        if (!data.containsKey(name)) data.put(name,new HashMap<>());
        if (value == null) data.get(name).remove(dataname);
        else data.get(name).put(dataname,value);

        updatePlaceholder(name,dataname);
    }

    public void setTempData(String name, String dataname, String value) {
        if (!tempdata.containsKey(name)) tempdata.put(name,new HashMap<>());
        if (value == null) data.get(name).remove(dataname);
        else tempdata.get(name).put(dataname,value);

        updatePlaceholder(name,dataname);
    }

    public void removeData(String name, String dataname) {
        setData(name,dataname,null);
        setTempData(name,dataname,null);
    }

    private void updatePlaceholder(String name, String dataname) {
        PlaceholderManagerImpl pm = TAB.getInstance().getPlaceholderManager();
        TabPlayer p = TabAPI.getInstance().getPlayer(name);


        if (name.equals("global")) {
            if (pm.getPlaceholder("%global-data-"+dataname+"%") == null)
                pm.registerServerPlaceholder("%global-data-"+dataname+"%",-1,()->getData("global",dataname)).enableTriggerMode();
            ((ServerPlaceholder)pm.getPlaceholder("%global-data-"+dataname+"%")).updateValue(getData(name,dataname));
        }
        else {
            if (pm.getPlaceholder("%data-"+dataname+"%") == null)
                pm.registerPlayerPlaceholder("%data-"+dataname+"%",-1,player->getData(player.getName(),dataname)).enableTriggerMode();
            if (p != null) {
                ((PlayerPlaceholder)pm.getPlaceholder("%data-"+dataname+"%")).updateValue(p,getData(name,dataname));
            }
        }
    }

}

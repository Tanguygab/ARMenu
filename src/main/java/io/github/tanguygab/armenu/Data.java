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

    public void setData(String name, String data, String value, boolean temp) {
        Map<String,Map<String,String>> map = temp ? tempdata : this.data;
        if (!map.containsKey(name)) map.put(name,new HashMap<>());
        if (value == null) map.get(name).remove(data);
        else map.get(name).put(data,value);

        updatePlaceholder(name,data);
    }

    public void removeData(String name, String data) {
        setData(name,data,null,false);
        setData(name,data,null,true);
    }

    private void updatePlaceholder(String name, String data) {
        PlaceholderManagerImpl pm = TAB.getInstance().getPlaceholderManager();
        TabPlayer p = TabAPI.getInstance().getPlayer(name);


        if (name.equals("global")) {
            if (pm.getPlaceholder("%global-data-"+data+"%") == null)
                pm.registerServerPlaceholder("%global-data-"+data+"%",-1,()->getData("global",data)).enableTriggerMode();
            ((ServerPlaceholder)pm.getPlaceholder("%global-data-"+data+"%")).updateValue(getData(name,data));
        }
        else {
            if (pm.getPlaceholder("%data-"+data+"%") == null)
                pm.registerPlayerPlaceholder("%data-"+data+"%",-1,player->getData(player.getName(),data)).enableTriggerMode();
            if (p != null) {
                ((PlayerPlaceholder)pm.getPlaceholder("%data-"+data+"%")).updateValue(p,getData(name,data));
            }
        }
    }

}

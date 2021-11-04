package io.github.tanguygab.armenu.actions.commands;

import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import me.neznamy.tab.api.TabPlayer;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PermissionAction extends Action {

    private final Pattern pattern = Pattern.compile("(?i)(permission|perm):(?<permission>[a-zA-Z0-9.*_\\- \",]+):( )?");

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String getSuggestion() {
        return "permission:<permission>: <command>";
    }

    @Override
    public boolean replaceMatch() {
        return false;
    }

    @Override
    public void execute(String match, TabPlayer p) {
        if (p == null) return;
        Matcher matcher = getPattern().matcher(match);
        //noinspection ResultOfMethodCallIgnored
        matcher.find();
        String[] permission = matcher.group("permission").split(",");
        match = match.replaceAll(matcher.pattern().pattern(),"");

        UserManager um = LuckPermsProvider.get().getUserManager();
        User user = um.getUser(p.getUniqueId());
        if (user == null) return;
        List<Node> nodes = new ArrayList<>();
        for (String perm : permission) {
            if (user.getCachedData().getPermissionData().checkPermission(perm).asBoolean()) continue;
            Node node = Node.builder(perm).build();
            user.data().add(node);
            nodes.add(node);
        }
        try {
            um.saveUser(user).get();
            match = Utils.parsePlaceholders(match,p);
            String finalMatch = match;
            runSyncFuture(()->Bukkit.getServer().dispatchCommand((Player) p.getPlayer(), finalMatch)).get();
        } catch (Exception ignored) {}
        for (Node node : nodes)
            user.data().remove(node);
        um.saveUser(user);
    }
}

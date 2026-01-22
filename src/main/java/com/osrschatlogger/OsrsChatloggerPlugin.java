/*
 * Copyright (c) 2024, NullBot
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.osrschatlogger;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.clan.ClanChannel;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Set;

@Slf4j
@PluginDescriptor(
    name = "OSRS Chatlogger",
    description = "Sync your chat messages to osrschatlogger.com in real-time",
    tags = {"chat", "log", "logger", "sync", "chatlogger", "osrs"}
)
public class OsrsChatloggerPlugin extends Plugin
{
    private static final DateTimeFormatter ISO_FORMATTER = 
        DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

    @Inject
    private Client client;

    @Inject
    private OsrsChatloggerConfig config;

    @Inject
    private ChatSender chatSender;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private OsrsChatloggerOverlay overlay;

    @Inject
    private ClientToolbar clientToolbar;

    private NavigationButton navButton;
    private OsrsChatloggerPanel panel;

    // Chat types to capture
    private static final Set<ChatMessageType> LOGGED_TYPES = EnumSet.of(
        ChatMessageType.PUBLICCHAT,
        ChatMessageType.MODCHAT,
        ChatMessageType.PRIVATECHAT,
        ChatMessageType.PRIVATECHATOUT,
        ChatMessageType.CLAN_CHAT,
        ChatMessageType.CLAN_MESSAGE,
        ChatMessageType.CLAN_GIM_CHAT,
        ChatMessageType.CLAN_GIM_MESSAGE,
        ChatMessageType.CLAN_GUEST_CHAT,
        ChatMessageType.CLAN_GUEST_MESSAGE,
        ChatMessageType.FRIENDSCHAT,
        ChatMessageType.TRADEREQ,
        ChatMessageType.GAMEMESSAGE,
        ChatMessageType.ENGINE,
        ChatMessageType.BROADCAST,
        ChatMessageType.SPAM
    );

    private String currentRsn = null;
    private int currentWorld = -1;
    private String currentClanName = null;

    @Override
    protected void startUp() throws Exception
    {
        log.info("OSRS Chatlogger started!");
        overlayManager.add(overlay);
        
        // Create and register the side panel
        panel = new OsrsChatloggerPanel(chatSender);
        
        final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/icon.png");
        
        navButton = NavigationButton.builder()
            .tooltip("OSRS Chatlogger")
            .icon(icon)
            .priority(5)
            .panel(panel)
            .build();
        
        clientToolbar.addNavigation(navButton);
        
        // Validate API key on startup
        if (isConfigured())
        {
            chatSender.validateApiKey();
        }
    }

    @Override
    protected void shutDown() throws Exception
    {
        log.info("OSRS Chatlogger stopped!");
        overlayManager.remove(overlay);
        clientToolbar.removeNavigation(navButton);
        chatSender.shutdown();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGGED_IN)
        {
            // Get player info when logged in
            if (client.getLocalPlayer() != null)
            {
                currentRsn = client.getLocalPlayer().getName();
                currentWorld = client.getWorld();
                
                // Capture clan name (null-safe)
                ClanChannel clanChannel = client.getClanChannel();
                currentClanName = (clanChannel != null) ? clanChannel.getName() : null;
                
                log.debug("Logged in as {} on world {}, clan: {}", currentRsn, currentWorld, currentClanName);
                
                // Re-validate API key when logging into game
                if (isConfigured())
                {
                    chatSender.validateApiKey();
                }
            }
        }
        else if (event.getGameState() == GameState.LOGIN_SCREEN)
        {
            // Flush any remaining messages when logging out
            chatSender.flush();
            currentRsn = null;
            currentWorld = -1;
            currentClanName = null;
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        // Check if plugin is configured
        if (!isConfigured())
        {
            return;
        }

        // Check if this message type should be logged
        ChatMessageType type = event.getType();
        if (!shouldLogType(type))
        {
            return;
        }

        // Build message data
        ChatData chatData = new ChatData();
        chatData.setType(mapChatType(type));
        chatData.setIronmanType(detectIronmanType(event.getName()));
        chatData.setSender(cleanName(event.getName()));
        chatData.setMessage(cleanMessage(event.getMessage()));
        chatData.setTimestamp(ISO_FORMATTER.format(Instant.now()));
        chatData.setRsn(currentRsn);
        chatData.setWorld(currentWorld);
        
        // Populate clan name for clan-related message types
        if (isClanMessageType(type) && currentClanName != null)
        {
            chatData.setClan(currentClanName);
        }

        // Send to server
        chatSender.queueMessage(chatData);
    }
    
    private boolean isClanMessageType(ChatMessageType type)
    {
        return type == ChatMessageType.CLAN_CHAT 
            || type == ChatMessageType.CLAN_MESSAGE
            || type == ChatMessageType.CLAN_GUEST_CHAT
            || type == ChatMessageType.CLAN_GUEST_MESSAGE
            || type == ChatMessageType.CLAN_GIM_CHAT
            || type == ChatMessageType.CLAN_GIM_MESSAGE;
    }

    private boolean isConfigured()
    {
        String apiKey = config.apiKey();
        return apiKey != null && !apiKey.isEmpty() && apiKey.startsWith("oscl_");
    }

    private boolean shouldLogType(ChatMessageType type)
    {
        if (!LOGGED_TYPES.contains(type))
        {
            return false;
        }

        // Check user preferences
        switch (type)
        {
            case PUBLICCHAT:
            case MODCHAT:
                return config.logPublicChat();
            
            case PRIVATECHAT:
            case PRIVATECHATOUT:
                return config.logPrivateChat();
            
            case CLAN_CHAT:
            case CLAN_MESSAGE:
            case CLAN_GUEST_CHAT:
            case CLAN_GUEST_MESSAGE:
                return config.logClanChat();
            
            case CLAN_GIM_CHAT:
            case CLAN_GIM_MESSAGE:
                return config.logGroupChat();
            
            case FRIENDSCHAT:
                return config.logFriendsChat();
            
            case GAMEMESSAGE:
            case ENGINE:
            case BROADCAST:
            case SPAM:
                return config.logGameMessages();
            
            case TRADEREQ:
                return config.logTradeRequests();
            
            default:
                return true;
        }
    }

    private String mapChatType(ChatMessageType type)
    {
        switch (type)
        {
            case PUBLICCHAT:
            case MODCHAT:
                return "PUBLIC";
            case PRIVATECHAT:
                return "PRIVATE";
            case PRIVATECHATOUT:
                return "PRIVATE_OUT";
            case CLAN_CHAT:
            case CLAN_MESSAGE:
                return "CLAN_CHAT";
            case CLAN_GUEST_CHAT:
            case CLAN_GUEST_MESSAGE:
                return "CLAN_GUEST";
            case CLAN_GIM_CHAT:
            case CLAN_GIM_MESSAGE:
                return "CLAN_GIM_CHAT";
            case FRIENDSCHAT:
                return "FRIENDSCHAT";
            case TRADEREQ:
                return "TRADE";
            case GAMEMESSAGE:
            case SPAM:
                return "GAME";
            case ENGINE:
                return "ENGINE";
            case BROADCAST:
                return "BROADCAST";
            default:
                return type.name();
        }
    }
    private String detectIronmanType(String name)
    {
        if (name == null)
        {
            return "";
        }
        if (name.contains("<img=2>")) return "ironman";
        if (name.contains("<img=10>")) return "hardcore";
        if (name.contains("<img=3>")) return "ultimate";
        if (name.contains("<img=41>")) return "group";
        return "";
    }
    
    private String cleanName(String name)
    {
        if (name == null)
        {
            return "";
        }
        // Remove img tags (ironman icons, etc.)
        return name.replaceAll("<img=\\d+>", "").trim();
    }

    private String cleanMessage(String message)
    {
        if (message == null)
        {
            return "";
        }
        // Remove color tags and formatting
        return message
            .replaceAll("<col=[^>]+>", "")
            .replaceAll("</col>", "")
            .replaceAll("<br>", "\n")
            .trim();
    }

    @Provides
    OsrsChatloggerConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(OsrsChatloggerConfig.class);
    }

    // Getters for overlay
    public boolean isConnected()
    {
        return chatSender.isConnected();
    }

    public int getPendingCount()
    {
        return chatSender.getPendingCount();
    }

    public int getSentCount()
    {
        return chatSender.getSentCount();
    }
}

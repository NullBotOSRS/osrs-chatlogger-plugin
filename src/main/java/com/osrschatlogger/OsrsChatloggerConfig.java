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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("osrschatlogger")
public interface OsrsChatloggerConfig extends Config
{
    // =========================================================================
    // API Configuration
    // =========================================================================
    
    @ConfigSection(
        name = "API Settings",
        description = "Configure your OSRS Chatlogger API connection",
        position = 0
    )
    String apiSection = "apiSection";

    @ConfigItem(
        keyName = "apiKey",
        name = "API Key",
        description = "Your OSRS Chatlogger API key (get one at osrschatlogger.com/settings/api)",
        position = 1,
        section = apiSection,
        secret = true
    )
    default String apiKey()
    {
        return "";
    }

    @ConfigItem(
        keyName = "serverUrl",
        name = "Server URL",
        description = "OSRS Chatlogger server URL (default: https://osrschatlogger.com)",
        position = 2,
        section = apiSection
    )
    default String serverUrl()
    {
        return "https://osrschatlogger.com";
    }

    // =========================================================================
    // Chat Type Filters
    // =========================================================================
    
    @ConfigSection(
        name = "Chat Types",
        description = "Select which chat types to sync",
        position = 10
    )
    String chatTypesSection = "chatTypesSection";

    @ConfigItem(
        keyName = "logPublicChat",
        name = "Public Chat",
        description = "Log public chat messages",
        position = 11,
        section = chatTypesSection
    )
    default boolean logPublicChat()
    {
        return true;
    }

    @ConfigItem(
        keyName = "logPrivateChat",
        name = "Private Messages",
        description = "Log private messages (sent and received)",
        position = 12,
        section = chatTypesSection
    )
    default boolean logPrivateChat()
    {
        return true;
    }

    @ConfigItem(
        keyName = "logClanChat",
        name = "Clan Chat",
        description = "Log clan chat messages",
        position = 13,
        section = chatTypesSection
    )
    default boolean logClanChat()
    {
        return true;
    }

    @ConfigItem(
        keyName = "logGroupChat",
        name = "Group Ironman Chat",
        description = "Log Group Ironman chat messages",
        position = 14,
        section = chatTypesSection
    )
    default boolean logGroupChat()
    {
        return true;
    }

    @ConfigItem(
        keyName = "logFriendsChat",
        name = "Friends Chat",
        description = "Log friends chat (FC) messages",
        position = 15,
        section = chatTypesSection
    )
    default boolean logFriendsChat()
    {
        return true;
    }

    @ConfigItem(
        keyName = "logGameMessages",
        name = "Game Messages",
        description = "Log game messages (drops, level ups, etc.)",
        position = 16,
        section = chatTypesSection
    )
    default boolean logGameMessages()
    {
        return true;
    }

    @ConfigItem(
        keyName = "logTradeRequests",
        name = "Trade Requests",
        description = "Log trade requests",
        position = 17,
        section = chatTypesSection
    )
    default boolean logTradeRequests()
    {
        return false;
    }

    // =========================================================================
    // Display Options
    // =========================================================================
    
    @ConfigSection(
        name = "Display",
        description = "Overlay and display settings",
        position = 20
    )
    String displaySection = "displaySection";

    @ConfigItem(
        keyName = "showOverlay",
        name = "Show Status Overlay",
        description = "Show connection status overlay in-game",
        position = 21,
        section = displaySection
    )
    default boolean showOverlay()
    {
        return true;
    }

    @ConfigItem(
        keyName = "showMessageCount",
        name = "Show Message Count",
        description = "Show number of messages synced in overlay",
        position = 22,
        section = displaySection
    )
    default boolean showMessageCount()
    {
        return true;
    }

    // =========================================================================
    // Advanced Options
    // =========================================================================
    
    @ConfigSection(
        name = "Advanced",
        description = "Advanced configuration options",
        position = 30,
        closedByDefault = true
    )
    String advancedSection = "advancedSection";

    @ConfigItem(
        keyName = "batchSize",
        name = "Batch Size",
        description = "Number of messages to batch before sending (1-50)",
        position = 31,
        section = advancedSection
    )
    @Range(min = 1, max = 50)
    default int batchSize()
    {
        return 10;
    }

    @ConfigItem(
        keyName = "batchDelayMs",
        name = "Batch Delay (ms)",
        description = "Max time to wait before sending batch (500-10000ms)",
        position = 32,
        section = advancedSection
    )
    @Range(min = 500, max = 10000)
    default int batchDelayMs()
    {
        return 2000;
    }

    @ConfigItem(
        keyName = "retryAttempts",
        name = "Retry Attempts",
        description = "Number of retry attempts on failure (0-5)",
        position = 33,
        section = advancedSection
    )
    @Range(min = 0, max = 5)
    default int retryAttempts()
    {
        return 3;
    }

    @ConfigItem(
        keyName = "debugMode",
        name = "Debug Mode",
        description = "Enable debug logging (check RuneLite logs)",
        position = 34,
        section = advancedSection
    )
    default boolean debugMode()
    {
        return false;
    }
}

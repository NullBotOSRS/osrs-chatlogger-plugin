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

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

/**
 * Overlay showing OSRS Chatlogger connection status.
 */
public class OsrsChatloggerOverlay extends Overlay
{
    private static final Color COLOR_CONNECTED = new Color(0, 200, 83);
    private static final Color COLOR_DISCONNECTED = new Color(255, 82, 82);
    private static final Color COLOR_PENDING = new Color(255, 193, 7);
    private static final Color COLOR_INFO = new Color(100, 181, 246);

    private final Client client;
    private final OsrsChatloggerConfig config;
    private final ChatSender chatSender;
    private final PanelComponent panelComponent = new PanelComponent();

    @Inject
    public OsrsChatloggerOverlay(Client client, OsrsChatloggerConfig config, ChatSender chatSender)
    {
        this.client = client;
        this.config = config;
        this.chatSender = chatSender;

        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(OverlayPriority.LOW);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        // Check if overlay should be shown
        if (!config.showOverlay())
        {
            return null;
        }

        // Check if API key is configured
        String apiKey = config.apiKey();
        if (apiKey == null || apiKey.isEmpty())
        {
            return null;
        }

        panelComponent.getChildren().clear();

        // Title
        panelComponent.getChildren().add(TitleComponent.builder()
            .text("Chatlogger")
            .color(Color.WHITE)
            .build());

        // Connection status
        boolean connected = chatSender.isConnected();
        panelComponent.getChildren().add(LineComponent.builder()
            .left("Status:")
            .right(connected ? "Connected" : "Disconnected")
            .rightColor(connected ? COLOR_CONNECTED : COLOR_DISCONNECTED)
            .build());

        // Message counts
        if (config.showMessageCount())
        {
            int sent = chatSender.getSentCount();
            int pending = chatSender.getPendingCount();

            panelComponent.getChildren().add(LineComponent.builder()
                .left("Sent:")
                .right(formatNumber(sent))
                .rightColor(COLOR_INFO)
                .build());

            if (pending > 0)
            {
                panelComponent.getChildren().add(LineComponent.builder()
                    .left("Pending:")
                    .right(String.valueOf(pending))
                    .rightColor(COLOR_PENDING)
                    .build());
            }
        }

        // Show username if connected
        String username = chatSender.getUsername();
        if (connected && username != null && !username.isEmpty())
        {
            panelComponent.getChildren().add(LineComponent.builder()
                .left("User:")
                .right(username)
                .rightColor(Color.WHITE)
                .build());
        }

        return panelComponent.render(graphics);
    }

    /**
     * Format large numbers with K/M suffix.
     */
    private String formatNumber(int number)
    {
        if (number >= 1_000_000)
        {
            return String.format("%.1fM", number / 1_000_000.0);
        }
        else if (number >= 1_000)
        {
            return String.format("%.1fK", number / 1_000.0);
        }
        return String.valueOf(number);
    }
}
